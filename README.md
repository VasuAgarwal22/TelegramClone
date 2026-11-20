# ChatNova

ChatNova is a Telegram-inspired messaging clone with a React/Vite frontend and a Spring Boot backend foundation.

## Run the frontend

```bash
pnpm install
pnpm --dir frontend typecheck
pnpm --dir frontend dev
```

The frontend is available on `http://localhost:3000`. If the API is not running, it uses the local demo conversations so the interface remains usable.

### Troubleshooting missing React or JSX modules

If TypeScript reports `Cannot find module 'react'`, `Cannot find module 'lucide-react'`, missing `react/jsx-runtime`, or missing `JSX.IntrinsicElements`, dependencies have not been installed in the project directory. Run `pnpm install` from the repository root, then run `pnpm --dir frontend typecheck` again. Do not open the `.tsx` files as standalone scripts; start the Vite project through the commands above.

## Run the backend

The backend requires Java 21 and Maven. From `backend/`:

```bash
mvn spring-boot:run
```

The API listens on `http://localhost:8080`. Useful endpoints are:

- `GET /api/health`
- `GET /api/conversations`
- `GET /api/conversations/{conversationId}`
- `GET /api/conversations/{conversationId}/messages`
- `POST /api/conversations/{conversationId}/messages`

Real-time chat uses the raw WebSocket endpoint `ws://localhost:8080/ws/chat`. The browser subscribes with `{"type":"subscribe","conversationId":"maya"}` and sends messages with `{"type":"message","conversationId":"maya","content":"Hello","clientMessageId":"..."}`. The server broadcasts the canonical saved message to every subscribed client. The React client reconnects and resubscribes automatically; if the socket is unavailable, it falls back to `POST /api/conversations/{conversationId}/messages`. Vite proxies `/ws` during local development and managed preview.

Smart Conversation Memory is available at `GET /api/memory/settings`, `PUT /api/memory/settings`, `POST /api/memory/index/{conversationId}`, `GET /api/memory/search?q=...`, `GET /api/memory/summary/{conversationId}`, and `DELETE /api/memory/{id}`. The service extracts dates, names, tasks, locations, links, and commitments from authorized chat history and returns source conversation/message references.

Disappearing Digital Vault is available at `/api/vault`. It supports settings, PIN setup/unlock/lock, biometric capability preference, folders, encrypted item creation/listing, restore, and permanent delete. Vault expiration accepts `1 hour`, `24 hours`, `7 days`, or `never`; a scheduled job permanently removes expired items.

The default development database is an in-memory H2 database. For a MySQL-compatible database, set `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, and `SPRING_DATASOURCE_PASSWORD`. Set `CHATNOVA_CORS_ORIGINS` to a comma-separated list of allowed frontend origins. Set `CHATNOVA_VAULT_ENCRYPTION_KEY` to a long, stable production secret; the default key is for local development only. `CHATNOVA_CURRENT_USER_ID` is the temporary current-user seam until the existing app receives JWT/session authentication. Every Memory and Vault query is scoped to that owner ID and Vault PINs are stored as PBKDF2 hashes.

## Current milestone

The product foundation includes the responsive chat shell, conversation filters, chat details panel, optimistic message composer, typed API client, health contract, validated message creation, demo service, and persistence-ready JPA entities. Production authentication, realtime transport, media storage, deployment, and the three differentiating features will be added in later milestones.
