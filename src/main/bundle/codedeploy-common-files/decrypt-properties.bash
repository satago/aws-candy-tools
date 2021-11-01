#!/bin/bash
set -e
SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

cd ${SCRIPT_PATH}

for file in $(find ./data -name '*.properties')
do
  if [ ! -f ${file}.encrypted ]; then
    cat ${file} | python decrypt-file.py > ${file}.decrypted
    mv ${file} ${file}.encrypted
    mv ${file}.decrypted ${file}
  fi
done