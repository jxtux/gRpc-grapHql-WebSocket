import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { Subscription } from 'rxjs';
import { GraphqlService } from '../graphql.service';
import { ChatWebSocketService } from '../chat-websocket.service';
import { ChatMessage, User, UserRelationDetail } from '../models';

@Component({
  selector: 'app-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './panel.component.html',
  styleUrl: './panel.component.css'
})
export class PanelComponent implements OnInit, OnDestroy {
  user?: User;
  relations: UserRelationDetail[] = [];
  selectedRelation?: UserRelationDetail;
  chatMessages: ChatMessage[] = [];
  newMessage = '';
  otherUserOnline = false;
  websocketConnected = false;
  loading = true;
  error = '';
  private sub = new Subscription();

  constructor(private graphql: GraphqlService, private chat: ChatWebSocketService, private router: Router) {}

  ngOnInit(): void {
    const token = localStorage.getItem('token');
    const userId = localStorage.getItem('userId');
    if (!token || !userId) {
      this.router.navigateByUrl('/login');
      return;
    }
    this.chat.connect(userId, token);
    this.sub.add(this.chat.connected$.subscribe(v => this.websocketConnected = v));
    this.sub.add(this.chat.messages$.subscribe(message => this.onChatEvent(message)));

    this.graphql.loadPanel(userId, token).subscribe({
      next: data => {
        this.user = data.me;
        this.relations = data.userRelations;
        this.loading = false;
      },
      error: () => {
        this.error = 'No se pudo cargar el panel. El token puede ser inválido o el Gateway no responde.';
        this.loading = false;
      }
    });
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
    this.chat.disconnect();
  }

  logout(): void {
    this.chat.disconnect();
    localStorage.clear();
    this.router.navigateByUrl('/login');
  }

  seleccionarRelacion(relation: UserRelationDetail): void {
    const userId = localStorage.getItem('userId');
    if (!userId) return;
    this.selectedRelation = relation;
    this.chatMessages = [];
    this.otherUserOnline = false;
    this.chat.loadHistory(userId, relation.user.id);
    this.chat.getPresence(userId, relation.user.id);
  }

  enviarMensaje(): void {
    const userId = localStorage.getItem('userId');
    const content = this.newMessage.trim();
    if (!userId || !this.selectedRelation || !content) return;
    this.chat.sendMessage(userId, this.selectedRelation.user.id, content);
    this.newMessage = '';
  }

  private onChatEvent(message: ChatMessage): void {
    if (message.type === 'PRESENCE' && this.selectedRelation && String(message.fromUserId) === this.selectedRelation.user.id) {
      this.otherUserOnline = message.online;
      return;
    }
    if (message.type === 'USER_ONLINE' && this.selectedRelation && String(message.fromUserId) === this.selectedRelation.user.id) {
      this.otherUserOnline = true;
      return;
    }
    if (message.type === 'USER_OFFLINE' && this.selectedRelation && String(message.fromUserId) === this.selectedRelation.user.id) {
      this.otherUserOnline = false;
      return;
    }
    if (message.type === 'MESSAGE_RECEIVED' && this.selectedRelation && this.user) {
      const selectedId = Number(this.selectedRelation.user.id);
      const currentId = Number(this.user.id);
      const belongsToConversation =
        (message.fromUserId === currentId && message.toUserId === selectedId) ||
        (message.fromUserId === selectedId && message.toUserId === currentId);
      if (belongsToConversation && !this.chatMessages.some(m => m.eventId === message.eventId)) {
        this.chatMessages = [...this.chatMessages, message];
      }
    }
  }
}
