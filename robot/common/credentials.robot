*** Settings ***

Library  VOMSHelperLibrary

*** Variables ***

${cred.voms.use_os}   True

## Where the testsuite should look for an OAuth 
## access token
${cred.oauth.env_var_name}  IAM_ACCESS_TOKEN

## Embedded VOMS proxies
${cred.voms.1}   assets/certs/voms.1 
${cred.voms.2}   assets/certs/voms.2

${cred.voms.default}   ${cred.voms.1}

## Embedded GRID proxies
${cred.grid.1}   assets/certs/grid.1 
${cred.grid.default}   ${cred.grid.1}

## Embedded X.509 certs
${cred.cert.1.p12}   assets/certs/test0.p12
${cred.cert.1.cert}   assets/certs/test0.pem
${cred.cert.1.password}   pass   

${cred.cert.2.p12}   assets/certs/test1.p12
${cred.cert.2.cert}   assets/certs/test1.pem
${cred.cert.2.password}   pass   

${cred.cert.default.p12}   ${cred.cert.1.p12}
${cred.cert.default.cert}   ${cred.cert.1.cert}
${cred.cert.default.password}   ${cred.cert.1.password}

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