#!/bin/bash
set -e
SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

cd ${SCRIPT_PATH}

echo "Finding pre-${LIFECYCLE_EVENT} hooks..."

for file in $(find ./data -name "*pre-${LIFECYCLE_EVENT}*")
do
    echo "Executing ${file}..."
    exec "${file}"
done