#!/bin/bash 
set -ex

DAV_HOST=${DAV_HOST:-storm.test.example}

IAM_CLIENT_ID=${IAM_CLIENT_ID:-34d7ff8a-469e-4051-b95a-64b1fa6f7024}
IAM_TOKEN_ENDPOINT=${IAM_TOKEN_ENDPOINT:-https://wlcg.cloud.cnaf.infn.it/token}

export IAM_ACCESS_TOKEN=$(curl -d grant_type=client_credentials \
    -d client_id=${IAM_CLIENT_ID} ${IAM_TOKEN_ENDPOINT} \
    | jq .access_token | tr -d '"')

sudo chmod 600 /tmp/usercerts/test0.p12
echo "pass123" | voms-proxy-init --cert /tmp/usercerts/test0.p12 -voms test.vo --pwstdin

cp -r /code/robot .

pushd robot

sh run-testsuite.sh
