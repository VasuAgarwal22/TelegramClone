import type { MemorySearchResult, MemorySettings, MemorySummary, VaultFolder, VaultItem, VaultSettings } from '../types/chat';

const API_BASE = (import.meta.env.VITE_API_BASE_URL as string | undefined)?.replace(/\/$/, '') ?? '/api';
async function request<T>(path: string, options?: RequestInit): Promise<T> { const response = await fetch(`${API_BASE}${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...(options?.headers ?? {}) } }); if (!response.ok) throw new Error(`Feature API request failed: ${response.status}`); if (response.status === 204) return undefined as T; return response.json() as Promise<T>; }
const json = (body: unknown): RequestInit => ({ method: 'POST', body: JSON.stringify(body) });
export const memoryApi = {
  settings: () => request<MemorySettings>('/memory/settings'),
  setEnabled: (enabled: boolean) => request<MemorySettings>('/memory/settings', { method: 'PUT', body: JSON.stringify({ enabled }) }),
  index: (conversationId: string) => request<{ indexed: number }>(`/memory/index/${encodeURIComponent(conversationId)}`, json({})),
  search: (question: string) => request<MemorySearchResult>(`/memory/search?q=${encodeURIComponent(question)}`),
  summary: (conversationId: string) => request<MemorySummary>(`/memory/summary/${encodeURIComponent(conversationId)}`),
  delete: (id: number) => request<void>(`/memory/${id}`, { method: 'DELETE' }),
};
export const vaultApi = {
  settings: () => request<VaultSettings>('/vault/settings'),
  configurePin: (pin: string) => request<void>('/vault/pin', json({ pin })),
  unlock: (pin: string) => request<{ unlocked: boolean }>('/vault/unlock', json({ pin })),
  lock: () => request<void>('/vault/lock', json({})),
  setBiometric: (enabled: boolean) => request<void>('/vault/biometric', { method: 'PUT', body: JSON.stringify({ enabled }) }),
  folders: () => request<VaultFolder[]>('/vault/folders'),
  createFolder: (name: string) => request<VaultFolder>('/vault/folders', json({ name })),
  items: (archived = false) => request<VaultItem[]>(`/vault/items?archived=${archived}`),
  createItem: (item: { folderId?: number; type: string; title: string; content?: string; metadata?: string; expiration: string }) => request<VaultItem>('/vault/items', json(item)),
  archive: (id: number) => request<void>(`/vault/items/${id}/archive`, json({})),
  restore: (id: number) => request<void>(`/vault/items/${id}/restore`, json({})),
  delete: (id: number) => request<void>(`/vault/items/${id}`, { method: 'DELETE' }),
};
