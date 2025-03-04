# SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
#
# SPDX-License-Identifier: Apache-2.0

*** Settings ***

Resource   common/credentials.robot

*** Variables ***

${oidc-agent.scope.default}   -s openid
${oidc-agent.alias}  dev-wlcg


*** Keywords ***

Get token   [Arguments]   ${scope}=${oidc-agent.scope.default}  ${issuer}=${oidc-agent.alias}  ${opts}=${EMPTY}
    ${rc}  ${out}   Execute and Check Success   oidc-token ${scope} ${opts} ${issuer} 
    Set Environment Variable   ${cred.oauth.env_var_name}   ${out}
    RETURN   ${out}