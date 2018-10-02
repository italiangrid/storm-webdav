*** Variables ***

## Endpoints

${dav.host}   localhost

${dav.port}   8085
${davs.port}   8443

${dav.endpoint}   http://${dav.host}:${dav.port}
${davs.endpoint}  https://${dav.host}:${davs.port}

${token.endpoint}  https://${dav.host}:${davs.port}/oauth/token

*** Keywords ***

DAVS URL  [Arguments]  ${path}  ${sa}=${sa.default}
    ${sa_path}  Normalize Path  /${sa}/${path}
    [Return]  ${davs.endpoint}${sa_path}

DAV URL  [Arguments]  ${path}  ${sa}=${sa.noauth}
    ${sa_path}  Normalize Path  /${sa}/${path}
    [Return]  ${dav.endpoint}${sa_path}