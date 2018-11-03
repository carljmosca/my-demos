#!/bin/bash

if ! whoami &> /dev/null; then
  if [ -w /etc/passwd ]; then
    echo "${USER_NAME:-default}:x:$(id -u):0:${USER_NAME:-default} user:/home/default:/sbin/nologin" >> /etc/passwd
  fi
fi

java $JAVA_OPTIONS -Djava.security.egd=file:/dev/./urandom -jar /app.jar

tail -f /dev/null