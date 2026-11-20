# ChatNova — Smart Memory and Disappearing Digital Vault

## Implementation approach

ChatNova keeps the existing React/Vite frontend and Spring Boot/JPA backend. The current conversation API remains intact; the two new features are additive modules with typed REST contracts. A deterministic `CurrentUser` seam (`user-vasu`) scopes every memory and vault query until production JWT/session authentication is connected. The backend persists memory records, vault folders/items, and vault settings in JPA with H2 development fallback and MySQL compatibility. A scheduled cleanup job permanently deletes expired vault items. Sensitive vault payloads are encrypted at rest using AES-GCM with a deployment-provided key; the local fallback key is explicitly development-only.

Smart Conversation Memory extracts searchable facts using a deterministic local extractor (dates, URLs, people-like names, tasks, locations, and commitments) from authorized chat messages. It persists extracted memory records with source conversation/message references, supports natural-language retrieval and conversation summaries, and allows individual deletion or global disablement. This creates a real backend-backed feature without silently sending private messages to an external AI provider; an LLM provider can later replace the extractor behind the same service interface.

The Digital Vault adds a dedicated sidebar destination and a responsive vault workspace. Users can create folders, save text/links/media/document metadata, choose expiration (1 hour, 24 hours, 7 days, never), lock/unlock with a PIN, restore items, and permanently delete them. Browser biometric authentication is exposed through WebAuthn capability detection and a clear unsupported fallback; server-side PIN verification remains authoritative. The frontend uses the existing API client and Telegram-inspired light design language.

## Product scope

- Smart Memory REST endpoints for settings, indexing, natural-language search, summary generation, and per-memory deletion.
- Smart Memory frontend panel with question input, answer cards, source references, extracted-fact chips, summaries, delete controls, and disable toggle.
- Digital Vault REST endpoints for settings/lock, folders, item listing, item creation, restore, and permanent deletion.
- Digital Vault frontend panel with lock screen, PIN setup/unlock, folders, expiration selector, add-item form, restore/delete actions, and WebAuthn capability messaging.
- Scheduled server cleanup for expired encrypted vault items.
- Existing chat routes, composer, and backend contracts preserved.

## Project structure additions

```text
backend/src/main/java/com/chatnova/api/
├── config/CurrentUser.java, VaultCryptoConfig.java
├── model/MemoryEntities.java, VaultEntities.java
├── dto/FeatureDtos.java
├── repository/MemoryRepositories.java, VaultRepositories.java
├── service/SmartMemoryService.java, DigitalVaultService.java
└── controller/FeatureControllers.java
frontend/src/
├── components/SmartMemoryPanel.tsx, DigitalVaultPanel.tsx
├── lib/api.ts (feature clients)
└── types/chat.ts (feature contracts)
```

## Design direction

The chosen direction remains the existing Telegram-inspired light workspace: white navigation surfaces, pale blue-gray chat canvas, Telegram blue `#2AABEE`, compact cards, and clear states. Smart Memory uses blue evidence cards and source chips. Digital Vault uses a secure but approachable lock-card motif, folder tabs, expiration badges, and destructive actions with explicit confirmation.

**Interaction philosophy:** feature entry points change the main workspace rather than merely showing toasts. Every mutation returns persisted server state; unauthorized or locked vault requests fail closed. Loading, empty, disabled, expired, and error states are visible.

## Security boundaries

The current-user seam is centralized and must be replaced by authenticated identity before production. Every memory and vault service method filters by owner ID. Vault item content is encrypted before persistence and decrypted only after authorization and unlock checks. PINs are stored as one-way PBKDF2 hashes with per-user salts, never plaintext. The development encryption key and user identity are configuration defaults only for local demos.

## Real-time chat transport

The chat uses a raw Spring WebSocket endpoint at `/ws/chat`. Clients subscribe to a conversation, send text messages with a client message ID, and receive conversation-scoped broadcasts. The handler validates that a session subscribed before it can send, persists through the existing chat service, and broadcasts the canonical server message to every subscribed session. The React client reconnects automatically, resubscribes after reconnect, reconciles optimistic messages, and falls back to the existing HTTP message endpoint when WebSocket is unavailable. Vite proxies `/ws` with upgrade support during local and managed preview development.
