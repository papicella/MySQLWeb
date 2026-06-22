#!/usr/bin/env bash
set -euo pipefail

port=8080
stopped=false

if pids=$(lsof -ti ":$port" 2>/dev/null); then
  echo "$pids" | xargs kill -TERM 2>/dev/null || true
  sleep 2
  if pids=$(lsof -ti ":$port" 2>/dev/null); then
    echo "$pids" | xargs kill -KILL 2>/dev/null || true
  fi
  stopped=true
fi

if pgrep -f "spring-boot:run" >/dev/null 2>&1; then
  pkill -TERM -f "spring-boot:run" 2>/dev/null || true
  stopped=true
fi

if pgrep -f "PivotalMySqlWebApplication" >/dev/null 2>&1; then
  pkill -TERM -f "PivotalMySqlWebApplication" 2>/dev/null || true
  stopped=true
fi

if $stopped; then
  echo "Stopped MySQLWeb"
else
  echo "No running MySQLWeb process found"
fi
