#!/bin/sh

# SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
#
# SPDX-License-Identifier: Apache-2.0

set -ex

STORAGE_DIR=${STORAGE_DIR:-/storage}

for f in /etc/storm/webdav/sa.d/*.properties; do
  filename=$(basename -- $f)
  sa_name=${filename%.*}
  mkdir -p ${STORAGE_DIR}/${sa_name}
done

chown -R storm:storm ${STORAGE_DIR}
