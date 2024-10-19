#!/usr/bin/env bash
set -ex

OIDC_AGENT_ALIAS=${OIDC_AGENT_ALIAS:-dev-wlcg}

eval $(oidc-agent --no-autoload)
oidc-add --pw-cmd='echo ${OIDC_AGENT_SECRET}' ${OIDC_AGENT_ALIAS}
export IAM_ACCESS_TOKEN=$(oidc-token ${OIDC_AGENT_ALIAS})

/scripts/init-usercerts.sh
echo "pass123" | voms-proxy-init --cert /tmp/usercerts/test0.p12 -voms test.vo --pwstdin

cp -r /code/robot .

pushd robot

sh run-testsuite.sh
