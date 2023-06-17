#!/bin/bash

# docker-compose up
docker-compose up

# checking exit status of the previous command (docker-compose up)
if [ $? -ne 0 ]; then
  echo "Docker Compose failed to start. Cleaning up containers..."
  docker-compose down
fi