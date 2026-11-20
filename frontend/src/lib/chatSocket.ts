import type { Message } from '../types/chat';

type ServerEvent = { type: 'connected' | 'subscribed' | 'message' | 'error'; conversationId?: string; clientMessageId?: string; message?: Message; messageText?: string; errorMessage?: string };

type ChatSocketOptions = {
  onMessage: (conversationId: string, message: Message, clientMessageId?: string) => void;
  onStatus: (connected: boolean) => void;
  onNotice: (message: string) => void;
};

export class ChatSocket {
  private socket: WebSocket | null = null;
  private reconnectTimer: number | undefined;
  private manuallyClosed = false;
  private subscriptions = new Set<string>();
  private readonly options: ChatSocketOptions;

  constructor(options: ChatSocketOptions) { this.options = options; }

  connect() {
    if (this.socket?.readyState === WebSocket.OPEN || this.socket?.readyState === WebSocket.CONNECTING) return;
    this.manuallyClosed = false;
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    this.socket = new WebSocket(`${protocol}//${window.location.host}/ws/chat`);
    this.socket.onopen = () => { this.options.onStatus(true); this.subscriptions.forEach((conversationId) => this.send({ type: 'subscribe', conversationId })); };
    this.socket.onmessage = (event) => { const payload = JSON.parse(event.data) as ServerEvent & { message?: Message | string }; if (payload.type === 'message' && payload.conversationId && payload.message && typeof payload.message !== 'string') this.options.onMessage(payload.conversationId, payload.message, payload.clientMessageId); if (payload.type === 'error') this.options.onNotice(typeof payload.message === 'string' ? payload.message : payload.messageText ?? 'Realtime chat error'); };
    this.socket.onclose = () => { this.options.onStatus(false); if (!this.manuallyClosed) { window.clearTimeout(this.reconnectTimer); this.reconnectTimer = window.setTimeout(() => this.connect(), 1800); } };
    this.socket.onerror = () => this.options.onStatus(false);
  }

  subscribe(conversationId: string) { this.subscriptions.add(conversationId); if (this.socket?.readyState === WebSocket.OPEN) this.send({ type: 'subscribe', conversationId }); }
  unsubscribe(conversationId: string) { this.subscriptions.delete(conversationId); if (this.socket?.readyState === WebSocket.OPEN) this.send({ type: 'unsubscribe', conversationId }); }
  sendMessage(conversationId: string, content: string, clientMessageId: string): boolean { if (this.socket?.readyState !== WebSocket.OPEN) return false; this.send({ type: 'message', conversationId, content, clientMessageId }); return true; }
  close() { this.manuallyClosed = true; window.clearTimeout(this.reconnectTimer); this.socket?.close(); this.socket = null; }
  private send(payload: object) { this.socket?.send(JSON.stringify(payload)); }
}
