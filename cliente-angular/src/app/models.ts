export interface User {
  id: string;
  username: string;
  firstName: string;
  lastName: string;
  email: string;
  phone: string;
  profession: string;
  description: string;
  avatarUrl: string;
}

export interface AuthPayload {
  authenticated: boolean;
  token: string;
  message: string;
  user: User;
}

export interface UserRelationDetail {
  type: 'FRIEND' | 'FOLLOWER';
  status: 'PENDING' | 'ACCEPTED' | 'BLOCKED';
  user: User;
}

export interface ChatMessage {
  eventId: string;
  type: string;
  fromUserId: number;
  toUserId: number;
  content: string;
  createdAt: string;
  online: boolean;
}
