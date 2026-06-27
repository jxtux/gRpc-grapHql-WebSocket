import { Injectable } from '@angular/core';
import { BehaviorSubject, Subject } from 'rxjs';
import { ChatMessage } from './models';

@Injectable({ providedIn: 'root' })
export class ChatWebSocketService {
  private socket?: WebSocket;
  private currentUserId?: string;
  readonly messages$ = new Subject<ChatMessage>();
  readonly connected$ = new BehaviorSubject<boolean>(false);

  connect(userId: string, token: string): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN && this.currentUserId === userId) return;
    this.disconnect();
    this.currentUserId = userId;
    const url = `ws://localhost:8086/ws/chat?userId=${encodeURIComponent(userId)}&token=${encodeURIComponent(token)}`;
    this.socket = new WebSocket(url);
    this.socket.onopen = () => this.connected$.next(true);
    this.socket.onmessage = event => this.messages$.next(JSON.parse(event.data));
    this.socket.onclose = () => this.connected$.next(false);
    this.socket.onerror = () => this.connected$.next(false);
  }

  loadHistory(fromUserId: string, toUserId: string): void {
    this.sendRaw({ type: 'HISTORY', fromUserId: Number(fromUserId), toUserId: Number(toUserId) });
  }

  getPresence(fromUserId: string, toUserId: string): void {
    this.sendRaw({ type: 'PRESENCE', fromUserId: Number(fromUserId), toUserId: Number(toUserId) });
  }

  sendMessage(fromUserId: string, toUserId: string, content: string): void {
    this.sendRaw({ type: 'MESSAGE', fromUserId: Number(fromUserId), toUserId: Number(toUserId), content });
  }

  disconnect(): void {
    if (this.socket) {
      this.socket.close();
      this.socket = undefined;
    }
    this.connected$.next(false);
  }

  private sendRaw(payload: unknown): void {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(JSON.stringify(payload));
    }
  }
}
