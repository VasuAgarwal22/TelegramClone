export type ConversationKind = 'direct' | 'group' | 'channel';
export type MessageStatus = 'sent' | 'delivered' | 'read';
export type ChatFilter = 'all' | 'unread' | 'groups' | 'channels';

export interface Reaction {
  emoji: string;
  count: number;
  reacted: boolean;
}

export interface ReplyPreview {
  senderName: string;
  content: string;
}

export interface Message {
  id: string;
  senderId: string;
  senderName: string;
  senderAvatar: string;
  content: string;
  time: string;
  dateLabel?: string;
  isMine?: boolean;
  status?: MessageStatus;
  reactions?: Reaction[];
  replyTo?: ReplyPreview;
  attachments?: Array<{ name: string; size: number; kind: 'image' | 'file' }>;
}

export interface Conversation {
  id: string;
  kind: ConversationKind;
  name: string;
  subtitle: string;
  avatar: string;
  avatarTone: string;
  online?: boolean;
  pinned?: boolean;
  unreadCount?: number;
  lastMessage: string;
  lastTime: string;
  muted?: boolean;
  members?: number;
  messages: Message[];
}

export interface UserProfile {
  name: string;
  handle: string;
  status: string;
  avatar: string;
}

export interface MemorySettings { enabled: boolean; memoryCount: number; }
export interface MemoryReference { id: number; kind: string; title: string; value: string; conversationId: string; messageId: string; sourceSnippet: string; }
export interface MemorySearchResult { answer: string; references: MemoryReference[]; }
export interface MemorySummary { conversationId: string; summary: string; references: MemoryReference[]; }
export interface VaultSettings { pinConfigured: boolean; locked: boolean; biometricEnabled: boolean; }
export interface VaultFolder { id: number; name: string; }
export interface VaultItem { id: number; folderId?: number; type: string; title: string; content: string; metadata?: string; expiration: string; expiresAt?: string; createdAt: string; archived: boolean; }
