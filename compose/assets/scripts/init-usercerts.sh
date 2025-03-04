#!/usr/bin/env bash
set -ex

USER_CERTS_DIR=${USER_CERTS_DIR:-/usercerts}

sudo cp ${USER_CERTS_DIR}/test0.* /home/test/.globus
sudo chown test:test /home/test/.globus/test0.*
sudo chmod 600 /home/test/.globus/test0.cert.pem
sudo chmod 400 /home/test/.globus/test0.key.pem

sudo cp ${USER_CERTS_DIR}/x509up_u1000 /tmp/x509up_u1000
sudo chown test:test /tmp/x509up_u1000
sudo chmod 600 /tmp/x509up_u1000
