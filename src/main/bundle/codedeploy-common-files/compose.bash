#!/bin/bash
set -e
SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

# Path to docker-compose
PATH=$PATH:/usr/local/bin/

# We're not using Compose's `.env` file, because it applies the same rules as per `env_file`:

# Namely:
# > The value of VAL is used as is and not modified at all.
# > For example if the value is surrounded by quotes (as is often the case of shell variables),
# > the quotes will be included in the value passed to Compose.
# See https://docs.docker.com/compose/compose-file/#envfile

# That's why we export all variables declared in `compose.env` using `set -a`,
# instead of relying to Compose's `.env`

set -a
source ${SCRIPT_PATH}/compose.env
set +a

function docker_compose_files_arg {
    local REPLACEMENT=$(echo "$1" | sed 's/\//\\\//g')
    echo ${DOCKER_COMPOSE_FILES} | sed "s/[^ ]* */$REPLACEMENT&/g"
}

function compose {
    docker-compose \
        $(docker_compose_files_arg "-f ${SCRIPT_PATH}/") \
        "$@"
}

if [[ "$0" == "$BASH_SOURCE" ]]; then
    # If the script was not sourced, but executed directly,
    # then pass all arguments straight to the `docker-compose`
    compose "$@"
fi