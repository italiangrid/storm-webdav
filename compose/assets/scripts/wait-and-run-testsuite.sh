#!/bin/bash 
set -ex

export X509_USER_PROXY=${X509_USER_PROXY:-/tmp/x509up_u$(id -u)}
DAV_HOST=${DAV_HOST:-storm.example}
WAIT_TIMEOUT=${WAIT_TIMEOUT:-200}

IAM_CLIENT_ID=${IAM_CLIENT_ID:-34d7ff8a-469e-4051-b95a-64b1fa6f7024}
IAM_TOKEN_ENDPOINT=${IAM_TOKEN_ENDPOINT:-https://wlcg.cloud.cnaf.infn.it/token}

export IAM_ACCESS_TOKEN=$(curl -d grant_type=client_credentials \
    -d client_id=${IAM_CLIENT_ID} ${IAM_TOKEN_ENDPOINT} \
    | jq .access_token | tr -d '"')

/scripts/init-usercerts.sh
echo "pass" | voms-proxy-init --cert /tmp/usercerts/test0.p12 -voms test.vo --pwstdin

/scripts/wait-for-it.sh ${DAV_HOST}:8085 --timeout=${WAIT_TIMEOUT}

cp -r /code/robot .

pushd robot

sh run-testsuite.sh
