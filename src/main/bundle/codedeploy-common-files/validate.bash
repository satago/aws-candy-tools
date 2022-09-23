#!/bin/bash
set -e
SCRIPT_PATH=$( cd "$(dirname "$0")" ; pwd -P )

source ${SCRIPT_PATH}/compose.bash

CONTAINER_IDS=$(compose ps -q)

CONTAINER_COUNT=$(echo ${CONTAINER_IDS} | wc -w)

COUNT_STATUS=$(docker inspect --format='{{.State.Status}}' ${CONTAINER_IDS} \
                 | sort \
                 | uniq -c \
                 | xargs)       # trim spaces

test "${COUNT_STATUS}" = "${CONTAINER_COUNT} running"
