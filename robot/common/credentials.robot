*** Settings ***

Library  VOMSHelperLibrary

*** Variables ***

## Where the testsuite should look for an OAuth 
## access token
${cred.oauth.env_var_name}  IAM_ACCESS_TOKEN

${cred.voms.use_os}   True

## Embedded VOMS proxies
${cred.voms.default}   assets/certs/voms.1

*** Keywords ***
Default Proxy Path
    ${user_id}  Run  id -u
    [Return]  /tmp/x509up_u${user_id}

Set VOMS Credential   [Arguments]   ${proxy_path}=${cred.voms.default}
    Set Environment Variable  X509_USER_PROXY  ${proxy_path}

Unset VOMS Credential
    Remove Environment Variable  X509_USER_PROXY

Os VOMS Credential
    ${os_proxy}   Default Proxy Path
    Set VOMS Credential  ${os_proxy}   

Default VOMS Credential
    ${proxy_path}  Run Keyword If  ${cred.voms.use_os}  Default Proxy Path  ELSE  ${cred.voms.default}
    Set VOMS Credential  ${proxy_path}