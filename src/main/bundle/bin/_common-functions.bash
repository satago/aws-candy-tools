#!/bin/bash

function check_version {
    local installed=$1
    local required=$2

    local installed_major=$(echo ${installed} | cut -f1 -d.)
    local installed_minor=$(echo ${installed} | cut -f2 -d.)
    local installed_patch=$(echo ${installed} | cut -f3 -d.)

    local required_major=$(echo ${required} | cut -f1 -d.)
    local required_minor=$(echo ${required} | cut -f2 -d.)
    local required_patch=$(echo ${required} | cut -f3 -d.)

    if [[ "${installed_major}" < "${required_major}" ]]; then

        return 1

    elif [[ "${installed_major}" = "${required_major}" ]]; then

        if [[ -z "${required_minor}" ]]; then
            return 0
        fi

        if [[ "${installed_minor}" < "${required_minor}" ]]; then
            return 1
        fi

        if [[ "${installed_minor}" = "${required_minor}" ]]; then

            if [[ -z "${required_patch}" ]]; then
                return 0
            fi

            if [[ "${installed_patch}" < "${required_patch}" ]]; then
                return 1
            fi

            # installed_patch >= required_patch
            return 0
        fi

        # installed_minor >= required_minor
        return 0
    fi

    # installed_major >= required_major
    return 0
}

# `aws ecr get-login` + `docker login`
function docker_login_options {

    local DOCKER_CLIENT_VERSION=$(docker version --format '{{json .Client.Version}}' | sed 's/"//g')

    if check_version ${DOCKER_CLIENT_VERSION} "17.06"; then
        echo "--no-include-email"
    else
        echo ""
    fi
}