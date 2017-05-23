#!/bin/bash
set -e
SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

source ${SCRIPT_PATH}/compose.bash

#TODO Implement graceful shutdown for workers & maybe increase the timeout to give them some time to complete
compose stop --timeout 10
