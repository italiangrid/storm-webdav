*** Settings ***

Resource   common/credentials.robot

*** Variables ***

${oidc-agent.scope.default}   -s openid
${oidc-agent.client-name}  dev-wlcg


*** Keywords ***

Get token   [Arguments]   ${scope}=${oidc-agent.scope.default}  ${issuer}=${oidc-agent.client-name}  ${opts}=${EMPTY}
    ${rc}  ${out}   Execute and Check Success   oidc-token ${scope} ${opts} ${issuer} 
    Set Environment Variable   ${cred.oauth.env_var_name}   ${out}
    [Return]   ${out}