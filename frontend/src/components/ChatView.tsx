import { useRef, useState } from 'react';
import { ArrowLeft, Bell, Brain, CalendarDays, Check, CheckCheck, ChevronDown, FileText, Image as ImageIcon, Link2, Mic, MicOff, MoreHorizontal, Paperclip, Phone, Plus, Send, Smile, Users, Video, X } from 'lucide-react';
import type { Conversation, Message } from '../types/chat';
import { Avatar, IconButton } from './Primitives';

type SpeechRecognitionResultLike = { [index: number]: { [index: number]: { transcript: string } }; length: number };
type SpeechRecognitionLike = { continuous: boolean; interimResults: boolean; lang: string; onresult: ((event: { results: SpeechRecognitionResultLike }) => void) | null; onend: (() => void) | null; start: () => void; stop: () => void };
type SpeechRecognitionConstructor = new () => SpeechRecognitionLike;

declare global {
  interface Window { SpeechRecognition?: SpeechRecognitionConstructor; webkitSpeechRecognition?: SpeechRecognitionConstructor; }
}

export function ChatHeader({ conversation, onBack, onInfo, onAction, onMemory }: { conversation: Conversation; onBack: () => void; onInfo: () => void; onAction: (action: string) => void; onMemory: () => void }) {
  const [menuOpen, setMenuOpen] = useState(false);
  const runAction = (action: string) => { setMenuOpen(false); onAction(action); };
  const shareConversation = async () => {
    const shareLink = `${window.location.origin}/chat/${encodeURIComponent(conversation.id)}`;
    try {
      await navigator.clipboard.writeText(shareLink);
      runAction(`Share link copied: ${shareLink}`);
    } catch {
      runAction(`Share link ready: ${shareLink}`);
    }
  };
  return <header className="chat-header"><IconButton label="Back to conversations" className="mobile-only" onClick={onBack}><ArrowLeft size={19} /></IconButton><Avatar label={conversation.avatar} tone={conversation.avatarTone} online={conversation.online} /><div className="chat-heading"><strong>{conversation.name}</strong><span>{conversation.online ? 'online now' : conversation.subtitle}</span></div><div className="header-actions"><IconButton label="Open AI Memory" onClick={onMemory}><Brain size={17} /></IconButton><IconButton label="Start voice call" onClick={() => onAction('Starting voice call')}><Phone size={17} /></IconButton><IconButton label="Start video call" onClick={() => onAction('Starting video call')}><Video size={18} /></IconButton><div className="menu-anchor"><IconButton label="Open chat menu" onClick={() => setMenuOpen((open) => !open)}><MoreHorizontal size={19} /></IconButton>{menuOpen ? <div className="chat-menu"><button type="button" onClick={() => { setMenuOpen(false); onInfo(); }}>Conversation details</button><button type="button" onClick={() => runAction('Notifications muted for this conversation')}>Mute notifications</button><button type="button" onClick={shareConversation}>Share conversation</button></div> : null}</div></div></header>;
}

function MessageBubble({ message }: { message: Message }) {
  return <div className={message.isMine ? 'message-row mine' : 'message-row'}>{!message.isMine ? <Avatar label={message.senderAvatar} tone="blue" size="small" /> : null}<div className="message-stack">{message.dateLabel ? <div className="date-divider"><span>{message.dateLabel}</span></div> : null}<article className={message.isMine ? 'message-bubble mine' : 'message-bubble'}>{message.replyTo ? <div className="reply-preview"><span></span><div><b>{message.replyTo.senderName}</b><small>{message.replyTo.content}</small></div></div> : null}<p>{message.content}</p>{message.attachments?.length ? <div className="message-attachments">{message.attachments.map((attachment) => <span key={`${message.id}-${attachment.name}`} className="message-attachment"><Paperclip size={12} />{attachment.name}</span>)}</div> : null}<footer><time>{message.time}</time>{message.isMine ? message.status === 'read' ? <CheckCheck size={14} className="read-check" /> : <Check size={14} /> : null}</footer></article>{message.reactions?.length ? <div className="reactions">{message.reactions.map((reaction) => <span key={reaction.emoji} className={reaction.reacted ? 'reaction reacted' : 'reaction'}>{reaction.emoji} {reaction.count}</span>)}</div> : null}</div></div>;
}

export function MessageList({ messages }: { messages: Message[] }) { return <div className="message-list">{messages.map((message) => <MessageBubble key={message.id} message={message} />)}</div>; }

interface ComposerProps { value: string; attachments: File[]; onChange: (value: string) => void; onAddFiles: (files: File[]) => void; onRemoveFile: (name: string) => void; onSend: () => void; onNotice: (message: string) => void; }
const emojiOptions = ['😀', '😂', '😍', '🔥', '✨', '👍', '🎉', '💡', '🙌', '💙', '🚀', '🙂'];

export function Composer({ value, attachments, onChange, onAddFiles, onRemoveFile, onSend, onNotice }: ComposerProps) {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const recognitionRef = useRef<SpeechRecognitionLike | null>(null);
  const [showEmojiPicker, setShowEmojiPicker] = useState(false);
  const [isListening, setIsListening] = useState(false);
  const canSend = Boolean(value.trim() || attachments.length);
  const toggleVoiceInput = () => {
    if (isListening) { recognitionRef.current?.stop(); setIsListening(false); return; }
    const Recognition = window.SpeechRecognition ?? window.webkitSpeechRecognition;
    if (!Recognition) { onNotice('Voice typing is not supported in this browser'); return; }
    const recognition = new Recognition();
    recognition.continuous = true;
    recognition.interimResults = false;
    recognition.lang = 'en-IN';
    recognition.onresult = (event) => {
      const transcript = Array.from({ length: event.results.length }, (_, index) => event.results[index][0].transcript).join(' ');
      onChange(`${value}${value && !value.endsWith(' ') ? ' ' : ''}${transcript}`);
    };
    recognition.onend = () => { recognitionRef.current = null; setIsListening(false); };
    recognitionRef.current = recognition;
    recognition.start();
    setIsListening(true);
    onNotice('Listening… speak now');
  };
  return <div className="composer">{attachments.length ? <div className="attachment-strip">{attachments.map((file) => <span className="attachment-chip" key={`${file.name}-${file.size}`}><Paperclip size={12} /><span title={file.name}>{file.name}</span><button type="button" aria-label={`Remove ${file.name}`} onClick={() => onRemoveFile(file.name)}><X size={12} /></button></span>)}</div> : null}<div className="composer-box"><input ref={fileInputRef} className="visually-hidden" type="file" multiple onChange={(event) => { onAddFiles(Array.from(event.target.files ?? [])); event.target.value = ''; }} /><IconButton label="Add attachment" onClick={() => fileInputRef.current?.click()}><Paperclip size={19} /></IconButton><input value={value} onChange={(event) => onChange(event.target.value)} onKeyDown={(event) => { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); onSend(); } }} placeholder={isListening ? 'Listening…' : 'Write a message...'} /><div className="emoji-anchor"><IconButton label="Add emoji" onClick={() => setShowEmojiPicker((open) => !open)}><Smile size={19} /></IconButton>{showEmojiPicker ? <div className="emoji-picker" role="dialog" aria-label="Emoji picker">{emojiOptions.map((emoji) => <button type="button" key={emoji} onClick={() => { onChange(`${value}${emoji}`); setShowEmojiPicker(false); }}>{emoji}</button>)}</div> : null}</div><IconButton label={isListening ? 'Stop voice typing' : 'Start voice typing'} className={isListening ? 'listening-button' : ''} onClick={toggleVoiceInput}>{isListening ? <MicOff size={18} /> : <Mic size={18} />}</IconButton><button className="send-button" aria-label="Send message" onClick={onSend} disabled={!canSend}><Send size={17} /></button></div><span className="composer-hint"><span><Plus size={13} /> Attach anything</span><span>{isListening ? 'Listening for speech' : 'Press Enter to send'}</span></span></div>;
}

export function InfoPanel({ conversation, onClose, onAction }: { conversation: Conversation; onClose: () => void; onAction: (action: string) => void }) {
  const [notificationsOn, setNotificationsOn] = useState(true);
  return <aside className="info-panel panel-surface"><div className="info-top"><span>Conversation details</span><IconButton label="Close details" onClick={onClose}><X size={18} /></IconButton></div><div className="profile-block"><Avatar label={conversation.avatar} tone={conversation.avatarTone} size="large" online={conversation.online} /><h2>{conversation.name}</h2><p>{conversation.kind === 'channel' ? 'Public channel' : conversation.subtitle}</p><button type="button" className="profile-action" onClick={() => onAction(`Viewing ${conversation.name} profile`)}><Users size={15} /> {conversation.members ? `${conversation.members} members` : 'View profile'}</button></div><div className="info-rule" /><div className="info-section"><div className="info-section-title"><span>Shared space</span><ChevronDown size={15} /></div><div className="info-grid"><button type="button" onClick={() => onAction('Shared media opened')}><ImageIcon size={18} /><span>Media</span><b>24</b></button><button type="button" onClick={() => onAction('Shared files opened')}><FileText size={18} /><span>Files</span><b>8</b></button><button type="button" onClick={() => onAction('Shared links opened')}><Link2 size={18} /><span>Links</span><b>12</b></button></div></div><div className="info-section"><div className="info-section-title"><span>Quick actions</span></div><button type="button" className="detail-row" onClick={() => { setNotificationsOn((value) => !value); onAction(notificationsOn ? 'Notifications muted' : 'Notifications enabled'); }}><Bell size={17} /><span>Notifications</span><span className={notificationsOn ? 'toggle on' : 'toggle'}><i /></span></button><button type="button" className="detail-row" onClick={() => onAction('Reminder picker opened')}><CalendarDays size={17} /><span>Set a reminder</span><ChevronDown size={15} /></button></div></aside>;
}
