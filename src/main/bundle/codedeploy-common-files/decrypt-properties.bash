#!/bin/bash
set -e
SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

cd ${SCRIPT_PATH}

for encrypted_dir in $(find ./data -name 'encrypted' -type d)
do
    for file in $(find ${encrypted_dir} -name '*.properties' -type f)
    do
        cat ${file} | python decrypt-file.py > ${encrypted_dir}/../$(basename ${file})
   done
done