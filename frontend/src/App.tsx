import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Archive, Bell, Bookmark, Check, Info, Inbox, PanelLeftClose, PanelRightOpen, Settings2, Sparkles } from 'lucide-react';
import { ChatHeader, Composer, InfoPanel, MessageList } from './components/ChatView';
import { DigitalVaultPanel } from './components/DigitalVaultPanel';
import { Sidebar } from './components/Sidebar';
import { SmartMemoryPanel } from './components/SmartMemoryPanel';
import { demoConversations } from './data/demo';
import { loadConversations, sendMessage } from './lib/api';
import { ChatSocket } from './lib/chatSocket';
import type { ChatFilter, Conversation } from './types/chat';

type SidebarSection = 'inbox' | 'saved' | 'archive' | 'settings' | 'memory' | 'vault';

function WorkspaceView({ section, onNotice }: { section: 'saved' | 'archive' | 'settings'; onNotice: (message: string) => void }) {
  const [notifications, setNotifications] = useState(true);
  const [compactMode, setCompactMode] = useState(false);
  const content = {
    saved: { icon: Bookmark, eyebrow: 'Your space', title: 'Saved messages', description: 'Keep important messages, links, and files in one place.', items: ['The new orbit looks perfect.', 'Draft is ready for review.', 'Product Signals — launch checklist'] },
    archive: { icon: Archive, eyebrow: 'Out of sight', title: 'Archive', description: 'Archived conversations stay here until you bring them back.', items: ['No archived conversations yet'] },
    settings: { icon: Settings2, eyebrow: 'Preferences', title: 'Settings', description: 'Shape ChatNova around the way you communicate.', items: [] },
  }[section];
  const Icon = content.icon;
  return <section className="workspace-view"><div className="workspace-header"><div className="workspace-icon"><Icon size={23} /></div><div><span>{content.eyebrow}</span><h1>{content.title}</h1><p>{content.description}</p></div></div>{section === 'settings' ? <div className="settings-list"><button type="button" onClick={() => { setNotifications((value) => !value); onNotice(notifications ? 'Notifications disabled' : 'Notifications enabled'); }}><span><Bell size={18} /><b>Notifications</b><small>Show desktop alerts for new messages</small></span><span className={notifications ? 'toggle on' : 'toggle'}><i /></span></button><button type="button" onClick={() => { setCompactMode((value) => !value); onNotice(compactMode ? 'Comfortable spacing enabled' : 'Compact spacing enabled'); }}><span><Inbox size={18} /><b>Compact chat list</b><small>Fit more conversations on screen</small></span><span className={compactMode ? 'toggle on' : 'toggle'}><i /></span></button><button type="button" onClick={() => onNotice('Light theme is active')}><span><Check size={18} /><b>Light theme</b><small>Telegram-inspired light appearance</small></span><span className="setting-status">Active</span></button></div> : <div className="workspace-card-list">{content.items.map((item) => <button type="button" key={item} onClick={() => onNotice(section === 'saved' ? 'Saved message opened' : 'There are no archived chats to restore')}><span>{item}</span><span className="workspace-arrow">›</span></button>)}</div>}</section>;
}

export default function App() {
  const [conversations, setConversations] = useState<Conversation[]>(() => demoConversations.map((conversation) => conversation.id === 'maya' ? { ...conversation, unreadCount: 0 } : conversation));
  const [selectedId, setSelectedId] = useState('maya');
  const [query, setQuery] = useState('');
  const [filter, setFilter] = useState<ChatFilter>('all');
  const [activeSection, setActiveSection] = useState<SidebarSection>('inbox');
  const [draft, setDraft] = useState('');
  const [attachments, setAttachments] = useState<File[]>([]);
  const [showInfo, setShowInfo] = useState(false);
  const [showSidebar, setShowSidebar] = useState(true);
  const [isSyncing, setIsSyncing] = useState(false);
  const [notice, setNotice] = useState('');
  const [isRealtime, setIsRealtime] = useState(false);
  const socketRef = useRef<ChatSocket | null>(null);
  useEffect(() => { let mounted = true; loadConversations().then((data) => { if (mounted && data.length) setConversations(data.map((conversation) => conversation.id === 'maya' ? { ...conversation, unreadCount: 0 } : conversation)); }).catch(() => undefined); return () => { mounted = false; }; }, []);
  useEffect(() => { if (!notice) return; const timer = window.setTimeout(() => setNotice(''), 3200); return () => window.clearTimeout(timer); }, [notice]);
  useEffect(() => {
    const socket = new ChatSocket({
      onStatus: setIsRealtime,
      onNotice: setNotice,
      onMessage: (conversationId, message, clientMessageId) => {
        setConversations((items) => items.map((conversation) => {
          if (conversation.id !== conversationId) return conversation;
          const optimisticId = clientMessageId ? `local-${clientMessageId}` : '';
          const hasOptimistic = optimisticId && conversation.messages.some((item) => item.id === optimisticId);
          const hasMessage = conversation.messages.some((item) => item.id === message.id);
          const messages = hasOptimistic ? conversation.messages.map((item) => item.id === optimisticId ? message : item) : hasMessage ? conversation.messages : [...conversation.messages, message];
          return { ...conversation, messages, lastMessage: message.content, lastTime: message.time };
        }));
        if (clientMessageId) setIsSyncing(false);
      },
    });
    socketRef.current = socket;
    socket.connect();
    return () => { socket.close(); socketRef.current = null; };
  }, []);
  const selected = useMemo(() => conversations.find((conversation) => conversation.id === selectedId) ?? conversations[0], [conversations, selectedId]);
  const showNotice = useCallback((message: string) => setNotice(message), []);
  useEffect(() => { if (selected) socketRef.current?.subscribe(selected.id); }, [selected?.id]);
  const handleSelectConversation = (id: string) => { setSelectedId(id); setActiveSection('inbox'); setConversations((items) => items.map((conversation) => conversation.id === id ? { ...conversation, unreadCount: 0 } : conversation)); };
  const handleNavigate = (section: SidebarSection) => { setActiveSection(section); if (section === 'inbox') setFilter('all'); showNotice(`${section === 'memory' ? 'Smart Conversation Memory' : section === 'vault' ? 'Digital Vault' : `${section[0].toUpperCase()}${section.slice(1)}`} opened`); };
  const handleSend = async () => {
    const text = draft.trim(); if ((!text && !attachments.length) || !selected) return;
    const selectedAttachments = attachments.map((file) => ({ name: file.name, size: file.size, kind: file.type.startsWith('image/') ? 'image' as const : 'file' as const }));
    const content = text || `Sent ${selectedAttachments.length} attachment${selectedAttachments.length > 1 ? 's' : ''}`;
    const clientMessageId = `${Date.now()}`;
    const optimistic = { id: `local-${clientMessageId}`, senderId: 'me', senderName: 'Vasu Agarwal', senderAvatar: 'VA', content, time: 'now', isMine: true as const, status: 'read' as const, attachments: selectedAttachments };
    setDraft(''); setAttachments([]); setConversations((items) => items.map((conversation) => conversation.id === selected.id ? { ...conversation, lastMessage: content, lastTime: 'now', messages: [...conversation.messages, optimistic] } : conversation)); if (!text) return;
    setIsSyncing(true);
    const deliveredRealtime = isRealtime && (socketRef.current?.sendMessage(selected.id, text, clientMessageId) ?? false);
    if (deliveredRealtime) return;
    try { const saved = await sendMessage(selected.id, content); setConversations((items) => items.map((conversation) => conversation.id === selected.id ? { ...conversation, messages: conversation.messages.map((message) => message.id === optimistic.id ? { ...saved, attachments: selectedAttachments } : message) } : conversation)); } catch { /* Keep the optimistic message in preview-only mode. */ } finally { setIsSyncing(false); }
  };
  if (!selected) return <main className="empty-app"><Sparkles size={26} /><h1>Nothing in orbit yet</h1><p>Start a new conversation to see it here.</p></main>;
  const workspace = activeSection === 'memory' ? <SmartMemoryPanel conversationId={selected.id} onNotice={showNotice} /> : activeSection === 'vault' ? <DigitalVaultPanel onNotice={showNotice} /> : <WorkspaceView section={activeSection as 'saved' | 'archive' | 'settings'} onNotice={showNotice} />;
  return <main className="app-shell"><div className={showSidebar ? 'sidebar-slot' : 'sidebar-slot collapsed'}><Sidebar conversations={conversations} selectedId={selected.id} query={query} filter={filter} activeSection={activeSection} onQueryChange={setQuery} onFilterChange={setFilter} onSelect={handleSelectConversation} onCompose={() => { setActiveSection('inbox'); setDraft(''); showNotice('New message ready'); }} onNavigate={handleNavigate} /></div>{activeSection === 'inbox' ? <><section className="chat-panel"><ChatHeader conversation={selected} onBack={() => setShowSidebar(true)} onInfo={() => setShowInfo((value) => !value)} onAction={showNotice} onMemory={() => { setActiveSection('memory'); setShowInfo(false); }} /><div className="chat-canvas"><div className="ambient-glow glow-left" /><div className="ambient-glow glow-right" /><div className="thread-meta"><span className="secure-badge"><span className="pulse-dot" /> {isSyncing ? 'Syncing' : isRealtime ? 'Live WebSocket' : 'HTTP fallback'}</span><button type="button" onClick={() => setShowInfo((value) => !value)}><Info size={14} /> Details</button></div><MessageList messages={selected.messages} /></div><Composer value={draft} attachments={attachments} onChange={setDraft} onAddFiles={(files) => setAttachments((current) => [...current, ...files])} onRemoveFile={(name) => setAttachments((current) => current.filter((file) => file.name !== name))} onSend={handleSend} onNotice={showNotice} /><button className="collapse-sidebar" aria-label="Toggle conversations" onClick={() => setShowSidebar((value) => !value)}><PanelLeftClose size={16} /></button><button className="toggle-info" aria-label="Toggle details" onClick={() => setShowInfo((value) => !value)}>{showInfo ? <PanelRightOpen size={16} /> : <Info size={16} />}</button></section>{showInfo ? <InfoPanel conversation={selected} onClose={() => setShowInfo(false)} onAction={showNotice} /> : null}</> : workspace}{notice ? <div className="toast" role="status">{notice}</div> : null}</main>;
}
