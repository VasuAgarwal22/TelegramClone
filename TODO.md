# ChatNova feature checklist

## Existing foundation

- [x] Preserve the React/Vite Telegram-style chat shell and the Spring Boot conversation/message API.
- [x] Preserve light Telegram-inspired styling, composer attachments/emoji/voice typing, share links, and workspace navigation.

## Smart Conversation Memory

- [x] Create a backend-owned memory system that indexes authorized chat messages and retrieves information from previous conversations.
- [x] Add a dedicated AI Memory button in the chat header and an AI Memory workspace in the sidebar.
- [x] Allow natural-language questions about chat history and return answer text with source conversation/message references.
- [x] Extract and persist dates, names, tasks, locations, links, and commitments using the local extractor service seam.
- [x] Generate summaries for a conversation and return indexed source references.
- [x] Allow individual memory deletion and disabling memory for the current user.
- [x] Scope memory reads and writes to the configured current-user owner ID.

## Disappearing Digital Vault

- [x] Add a dedicated Digital Vault destination in the sidebar that changes the main workspace.
- [x] Persist saved text/notes and extensible image, video, and document metadata fields in folders/categories.
- [x] Support expiration choices of 1 hour, 24 hours, 7 days, and never.
- [x] Support secure PIN setup/unlock and browser biometric capability detection with a fallback message.
- [x] Encrypt vault content before persistence with AES-GCM and enforce owner plus lock-state checks in the backend.
- [x] Automatically delete expired items through a scheduled backend cleanup.
- [x] Allow restore and permanent deletion endpoints; the connected UI provides permanent deletion and item lifecycle controls.
- [x] Provide responsive Telegram-style locked, empty, loading, error, and success states.

## Quality and handoff

- [x] Add persistent JPA entities, repositories, REST controllers, configuration, and typed frontend API clients for both features.
- [x] Run frontend typecheck/build and backend tests successfully.
- [x] Smoke-test health, memory indexing/search/summary, vault PIN setup, encrypted item creation/listing, and live preview navigation.
- [x] Update README with feature setup, encryption-key, expiration, and local-auth seam instructions.

## Real-time chat

- [x] Add a Spring raw WebSocket endpoint at `/ws/chat` with conversation subscriptions and server broadcasts.
- [x] Deliver outgoing text messages over WebSocket, reconcile optimistic messages by client ID, and retain HTTP fallback when the socket is unavailable.
- [x] Reconnect automatically, resubscribe to active conversations, and show live WebSocket versus HTTP fallback status in the chat UI.

## Production follow-up

- [ ] Replace the configured current-user seam with verified JWT/session authentication before exposing the API publicly.
- [ ] Add object-storage upload handling for binary vault files; the current item model securely stores text and file metadata.
- [ ] Replace the deterministic extractor with a provider-selected LLM behind the same SmartMemoryService interface if generative answers are desired.
