#!/bin/bash
set -ex

USER_CERTS_DIR=${USER_CERTS_DIR:-/usercerts}
mkdir -p /tmp/usercerts
cp ${USER_CERTS_DIR}/* /tmp/usercerts
chmod 600 /tmp/usercerts/*
