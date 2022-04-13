#!/bin/bash 
set -ex

export X509_USER_PROXY=${X509_USER_PROXY:-/tmp/x509up_u$(id -u)}
DAV_HOST=${DAV_HOST:-storm.example}
WAIT_TIMEOUT=${WAIT_TIMEOUT:-200}

/scripts/init-usercerts.sh
echo "pass" | voms-proxy-init --cert /tmp/usercerts/test0.p12 -voms test.vo --pwstdin

/scripts/wait-for-it.sh ${DAV_HOST}:8085 --timeout=${WAIT_TIMEOUT}

cp -r /code/robot .

pushd robot

sh run-testsuite.sh
