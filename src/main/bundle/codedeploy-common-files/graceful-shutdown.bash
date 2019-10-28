#!/bin/bash
set -e

SERVICE=$1
RESTROOT=$2
TIMEOUT=$3

./compose.bash exec -T ${SERVICE} curl -s -o /dev/null -X POST ${RESTROOT}/request

count=$TIMEOUT
while [[ "$STATUS" != "200" ]] && [[ $count -gt 0 ]]
do
  STATUS=`./compose.bash exec -T ${SERVICE} curl -s -o /dev/null -w "%{http_code}" ${RESTROOT}/isready`
  sleep 1
  (( count-- ))
done

if [[ "$STATUS" != "200" ]]; then
  printf "Gracefull shutdown of ${SERVICE} failed.\n"
  sudo ./compose.bash exec -T ${SERVICE} curl -s -o /dev/null -X POST ${RESTROOT}/cancel
  exit 1
fi
printf "Gracefull shutdown ${SERVICE} succeeded\n"