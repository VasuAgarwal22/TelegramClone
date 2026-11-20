import { Archive, BellOff, Bookmark, Brain, Edit3, Inbox, Search, Settings2, Shield } from 'lucide-react';
import type { ChatFilter, Conversation } from '../types/chat';
import { Avatar, IconButton } from './Primitives';

type SidebarSection = 'inbox' | 'saved' | 'archive' | 'settings' | 'memory' | 'vault';

interface SidebarProps {
  conversations: Conversation[];
  selectedId: string;
  query: string;
  filter: ChatFilter;
  activeSection: SidebarSection;
  onQueryChange: (query: string) => void;
  onFilterChange: (filter: ChatFilter) => void;
  onSelect: (id: string) => void;
  onCompose: () => void;
  onNavigate: (section: SidebarSection) => void;
}

export function Sidebar({ conversations, selectedId, query, filter, activeSection, onQueryChange, onFilterChange, onSelect, onCompose, onNavigate }: SidebarProps) {
  const filtered = conversations.filter((conversation) => {
    const matchesQuery = `${conversation.name} ${conversation.lastMessage}`.toLowerCase().includes(query.toLowerCase());
    const matchesFilter = filter === 'all' || (filter === 'unread' && Boolean(conversation.unreadCount)) || (filter === 'groups' && conversation.kind === 'group') || (filter === 'channels' && conversation.kind === 'channel');
    return matchesQuery && matchesFilter;
  });

  const navItems: Array<{ id: SidebarSection; label: string; icon: typeof Inbox }> = [
    { id: 'inbox', label: 'Inbox', icon: Inbox },
    { id: 'saved', label: 'Saved', icon: Bookmark },
    { id: 'archive', label: 'Archive', icon: Archive },
    { id: 'memory', label: 'AI Memory', icon: Brain },
    { id: 'vault', label: 'Digital Vault', icon: Shield },
    { id: 'settings', label: 'Settings', icon: Settings2 },
  ];

  return <aside className="sidebar panel-surface">
    <div className="brand-row"><div className="brand-lockup"><span className="brand-mark">✦</span><span>ChatNova</span></div><IconButton label="New message" onClick={onCompose}><Edit3 size={17} /></IconButton></div>
    <div className="search-wrap"><Search size={16} /><input value={query} onChange={(event) => onQueryChange(event.target.value)} placeholder="Search" /></div>
    <div className="filter-row" role="tablist" aria-label="Conversation filters">{([['all', 'All'], ['unread', 'Unread'], ['groups', 'Groups'], ['channels', 'Channels']] as const).map(([value, label]) => <button key={value} className={filter === value ? 'filter-chip active' : 'filter-chip'} onClick={() => onFilterChange(value)}>{label}{value === 'unread' && conversations.some((chat) => chat.unreadCount) ? <span className="chip-count">{conversations.reduce((sum, chat) => sum + (chat.unreadCount ?? 0), 0)}</span> : null}</button>)}</div>
    <div className="section-label"><span>{activeSection === 'inbox' ? 'All conversations' : activeSection}</span><span>{filtered.length}</span></div>
    <div className="conversation-list">{filtered.map((conversation) => <button key={conversation.id} className={conversation.id === selectedId ? 'conversation-item selected' : 'conversation-item'} onClick={() => onSelect(conversation.id)}><Avatar label={conversation.avatar} tone={conversation.avatarTone} online={conversation.online} /><span className="conversation-copy"><span className="conversation-title"><strong>{conversation.name}</strong><time>{conversation.lastTime}</time></span><span className="conversation-preview"><span>{conversation.muted ? <BellOff size={13} /> : null}{conversation.lastMessage}</span>{conversation.unreadCount ? <b>{conversation.unreadCount}</b> : null}</span></span></button>)}</div>
    <nav className="sidebar-footer" aria-label="Workspace navigation">{navItems.map(({ id, label, icon: Icon }) => <button type="button" key={id} className={activeSection === id ? 'active' : ''} onClick={() => onNavigate(id)}><Icon size={16} /><span>{label}</span></button>)}</nav>
  </aside>;
}
