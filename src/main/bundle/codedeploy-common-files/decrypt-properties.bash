#!/bin/bash
set -e
SCRIPT_PATH=$( cd $(dirname $0) ; pwd -P )

cd ${SCRIPT_PATH}

for file in $(find ./data -name '*.properties')
do
  mv ${file} ${file}.encrypted
  cat ${file}.encrypted | python decrypt-file.py > ${file}
done
