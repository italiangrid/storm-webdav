#!/usr/bin/env bash

# SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
#
# SPDX-License-Identifier: Apache-2.0

set -ex

OIDC_AGENT_ALIAS=${OIDC_AGENT_ALIAS:-dev-wlcg}

eval $(oidc-agent-service use)
oidc-add --pw-env=OIDC_AGENT_SECRET ${OIDC_AGENT_ALIAS}
export IAM_ACCESS_TOKEN=$(oidc-token -s openid ${OIDC_AGENT_ALIAS})

/scripts/init-usercerts.sh

cp -r /code/robot .

pushd robot

sh run-testsuite.sh
