import type { Conversation, Message } from '../types/chat';

const API_BASE = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, '') ?? '/api';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(options?.headers ?? {}) },
  });
  if (!response.ok) throw new Error(`ChatNova API request failed: ${response.status}`);
  return response.json() as Promise<T>;
}

export function loadConversations(): Promise<Conversation[]> {
  return request<Conversation[]>('/conversations');
}

export function loadMessages(conversationId: string): Promise<Message[]> {
  return request<Message[]>(`/conversations/${conversationId}/messages`);
}

export function sendMessage(conversationId: string, content: string): Promise<Message> {
  return request<Message>(`/conversations/${conversationId}/messages`, {
    method: 'POST',
    body: JSON.stringify({ content }),
  });
}
