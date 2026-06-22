---
name: spring-boot-app
description: Starts and stops the MySQLWeb Spring Boot app with mvn spring-boot:run. Use when the user asks to start, run, launch, stop, or kill the app, dev server, or local server.
---

# MySQLWeb App Lifecycle

## Defaults

| Item | Value |
|------|-------|
| Project root | Repository root (`MySQLWeb/`) |
| Start command | `mvn spring-boot:run` |
| App URL | http://localhost:8080/ |
| Log file | `.cursor/spring-boot-app.log` |

## Start the app

**Preferred:** run the start script from the project root:

```bash
.cursor/skills/spring-boot-app/scripts/start-app.sh
```

**Alternative (agent shell):** background Maven from the project root:

```bash
mkdir -p .cursor
nohup mvn spring-boot:run > .cursor/spring-boot-app.log 2>&1 &
```

When using the Shell tool, set `block_until_ms: 0` so the server stays running.

### Verify startup

Wait for Tomcat on port 8080, then confirm:

```bash
lsof -ti :8080
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/
```

A `200` or `302` response means the app is up. First startup can take 30–60 seconds.

If port 8080 is already in use, report that the app is likely already running — do not start a second instance.

## Stop the app

**Preferred:** run the stop script from the project root:

```bash
.cursor/skills/spring-boot-app/scripts/stop-app.sh
```

**Alternative (manual):**

```bash
lsof -ti :8080 | xargs kill -TERM 2>/dev/null
pkill -TERM -f "spring-boot:run" 2>/dev/null
pkill -TERM -f "PivotalMySqlWebApplication" 2>/dev/null
```

Verify nothing is listening on 8080:

```bash
lsof -ti :8080 || echo "App stopped"
```

## Agent workflow

```
Task Progress:
- [ ] Check if app is already running (lsof -ti :8080)
- [ ] Start or stop as requested
- [ ] Verify result (port check or HTTP probe)
- [ ] Report URL or stop confirmation to the user
```

## Notes

- Use `mvn spring-boot:run`, not `./mvnw`, unless the user explicitly asks for the wrapper.
- `mvn spring-boot:run` spawns a child JVM — stop by port or process name, not the shell background PID alone.
- Tail logs when debugging startup: `tail -f .cursor/spring-boot-app.log`
