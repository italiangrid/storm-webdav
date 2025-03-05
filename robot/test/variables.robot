*** Variables ***

## Endpoints

${dav.host}   storm.test.example

${dav.port}   8085
${davs.port}   8443

${dav.endpoint}   http://${dav.host}:${dav.port}
${davs.endpoint}  https://${dav.host}:${davs.port}

${token.endpoint}  https://${dav.host}:${davs.port}/oauth/token

${remote.dav.host}             storm-alias.test.example
${remote.dav.port}             80
${remote.davs.port}            443
${remote.davs.endpoint}        https://${remote.dav.host}:${remote.davs.port}
${remote.dav.endpoint}         http://${remote.dav.host}:${remote.dav.port}

*** Keywords ***

DAVS URL  [Arguments]  ${path}  ${sa}=${sa.default}
    ${sa_path}  Normalize Path  /${sa}/${path}
    RETURN  ${davs.endpoint}${sa_path}

DAV URL  [Arguments]  ${path}  ${sa}=${sa.noauth}
    ${sa_path}  Normalize Path  /${sa}/${path}
    RETURN  ${dav.endpoint}${sa_path}

Remote DAVS URL  [Arguments]  ${path}  ${sa}=${sa.default}
    ${sa_path}  Normalize Path  /${sa}/${path}
    RETURN  ${remote.davs.endpoint}${sa_path}

Remote DAV URL  [Arguments]  ${path}  ${sa}=${sa.noauth}
    ${sa_path}  Normalize Path  /${sa}/${path}
    RETURN  ${remote.dav.endpoint}${sa_path}
