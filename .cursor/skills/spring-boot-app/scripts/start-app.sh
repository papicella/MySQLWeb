#!/usr/bin/env bash
set -euo pipefail

projectRoot="$(cd "$(dirname "$0")/../../../.." && pwd)"
logFile="$projectRoot/.cursor/spring-boot-app.log"
port=8080

if lsof -ti ":$port" >/dev/null 2>&1; then
  echo "App already running on port $port"
  exit 0
fi

mkdir -p "$projectRoot/.cursor"
cd "$projectRoot"

nohup mvn spring-boot:run > "$logFile" 2>&1 &
echo "Started mvn spring-boot:run (PID $!)"
echo "Log: $logFile"
echo "URL: http://localhost:$port/"
