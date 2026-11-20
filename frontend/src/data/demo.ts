import type { Conversation, UserProfile } from '../types/chat';

export const currentUser: UserProfile = {
  name: 'Vasu Agarwal',
  handle: '@vasu',
  status: 'Building in public',
  avatar: 'VA',
};

export const demoConversations: Conversation[] = [
  {
    id: 'maya', kind: 'direct', name: 'Maya Chen', subtitle: 'online', avatar: 'MC', avatarTone: 'violet', online: true, pinned: true,
    lastMessage: 'The new orbit looks perfect.', lastTime: '10:42', unreadCount: 2,
    messages: [
      { id: 'm1', senderId: 'maya', senderName: 'Maya Chen', senderAvatar: 'MC', content: 'Morning! I pushed the latest onboarding notes. The new orbit looks perfect.', time: '10:38', dateLabel: 'Today', reactions: [{ emoji: '✨', count: 2, reacted: false }] },
      { id: 'm2', senderId: 'me', senderName: 'Vasu Agarwal', senderAvatar: 'VA', content: 'That is such a relief. I’ll read through them before lunch and leave comments.', time: '10:40', isMine: true, status: 'read', replyTo: { senderName: 'Maya Chen', content: 'The new orbit looks perfect.' } },
      { id: 'm3', senderId: 'maya', senderName: 'Maya Chen', senderAvatar: 'MC', content: 'Amazing. I also added a tiny checklist for the empty states.', time: '10:42', reactions: [{ emoji: '💡', count: 1, reacted: true }] },
    ],
  },
  {
    id: 'studio', kind: 'group', name: 'Studio North', subtitle: '8 members', avatar: 'SN', avatarTone: 'mint', online: true,
    lastMessage: 'Nikhil: Let’s keep the blue accent.', lastTime: '09:18', unreadCount: 5, members: 8,
    messages: [
      { id: 's1', senderId: 'nikhil', senderName: 'Nikhil Shah', senderAvatar: 'NS', content: 'The new dashboard is feeling much lighter.', time: '09:12', dateLabel: 'Today', reactions: [{ emoji: '🔥', count: 4, reacted: false }] },
      { id: 's2', senderId: 'me', senderName: 'Vasu Agarwal', senderAvatar: 'VA', content: 'Let’s keep the blue accent. It gives the navigation a good anchor.', time: '09:18', isMine: true, status: 'delivered' },
    ],
  },
  {
    id: 'launch', kind: 'group', name: 'Launch Crew', subtitle: '14 members', avatar: 'LC', avatarTone: 'coral',
    lastMessage: 'You: Draft is ready for review', lastTime: 'Yesterday', members: 14,
    messages: [
      { id: 'l1', senderId: 'rhea', senderName: 'Rhea Kapoor', senderAvatar: 'RK', content: 'We have a clean draft for the launch note.', time: '18:04', dateLabel: 'Yesterday' },
      { id: 'l2', senderId: 'me', senderName: 'Vasu Agarwal', senderAvatar: 'VA', content: 'Draft is ready for review. Dropping the link here next.', time: '18:10', isMine: true, status: 'read' },
    ],
  },
  {
    id: 'arjun', kind: 'direct', name: 'Arjun Mehta', subtitle: 'last seen recently', avatar: 'AM', avatarTone: 'amber',
    lastMessage: 'That makes sense — thank you!', lastTime: 'Tue',
    messages: [
      { id: 'a1', senderId: 'me', senderName: 'Vasu Agarwal', senderAvatar: 'VA', content: 'Try the compact layout on mobile; it keeps the message thread focused.', time: '16:22', dateLabel: 'Tuesday', isMine: true, status: 'read' },
      { id: 'a2', senderId: 'arjun', senderName: 'Arjun Mehta', senderAvatar: 'AM', content: 'That makes sense — thank you!', time: '16:28' },
    ],
  },
  {
    id: 'product', kind: 'channel', name: 'Product Signals', subtitle: '12.4K subscribers', avatar: 'PS', avatarTone: 'blue',
    lastMessage: 'A calmer inbox is a better inbox.', lastTime: 'Mon',
    messages: [
      { id: 'p1', senderId: 'product', senderName: 'Product Signals', senderAvatar: 'PS', content: 'A calmer inbox is a better inbox. This week: fewer tabs, better context.', time: '11:00', dateLabel: 'Monday', reactions: [{ emoji: '💙', count: 28, reacted: false }] },
    ],
  },
  {
    id: 'family', kind: 'group', name: 'Weekend Plans', subtitle: '6 members', avatar: 'WP', avatarTone: 'pink',
    lastMessage: 'Priya: I can bring dessert.', lastTime: 'Sun', unreadCount: 1, members: 6,
    messages: [
      { id: 'f1', senderId: 'priya', senderName: 'Priya Rao', senderAvatar: 'PR', content: 'I can bring dessert. Any requests?', time: '14:05', dateLabel: 'Sunday' },
    ],
  },
];
