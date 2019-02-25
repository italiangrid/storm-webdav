#!/bin/bash 
set -ex

STORAGE_DIR=${STORAGE_DIR:-/storage}

for f in /etc/storm/webdav/sa.d/*.properties; do
  filename=$(basename -- $f)
  sa_name=${filename%.*}
  mkdir -p ${STORAGE_DIR}/${sa_name}
done

chown -R storm:storm ${STORAGE_DIR}
