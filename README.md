# Least Score (Spring Boot)

Prototype multiplayer “Least Score” game server + minimal UI.

## Local dev

Start Postgres:

```bash
docker compose up -d
```

Run the app:

```bash
./gradlew bootRun
```

Open `http://localhost:8080/`.

## Notes

- **Identity**: set a username first (stored in your browser session cookie).
- **Shareable room link**: `/r/{ROOMCODE}`.
- **Realtime**: clients subscribe to `/topic/room.{ROOMCODE}.state` over STOMP at `/ws`.
