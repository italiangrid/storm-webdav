*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

*** Variables ***

${remote.dav.host}             ${dav.host}
${remote.dav.port}             ${dav.port}
${remote.davs.port}            ${davs.port}
${remote.davs.endpoint}        https://${remote.dav.host}:${remote.davs.port}
${remote.dav.endpoint}         http://${remote.dav.host}:${remote.dav.port}

*** Keywords ***

Remote DAVS URL  [Arguments]  ${path}  ${sa}=${sa.default}
    ${sa_path}  Normalize Path  /${sa}/${path}
    [Return]  ${remote.davs.endpoint}${sa_path}

Remote DAV URL  [Arguments]  ${path}  ${sa}=${sa.noauth}
    ${sa_path}  Normalize Path  /${sa}/${path}
    [Return]  ${remote.dav.endpoint}${sa_path}

Default Setup
    Default VOMS credential

Default Teardown
    Unset VOMS credential

Local pull copy works Setup
    Default Setup
    Create Test File   tpc_test  content=Hello World!  sa=${sa.noauth}

Local pull copy works Teardown
    Default Teardown
    Remove Test File   tpc_test
    Remove Test File   tpc_test  sa=${sa.noauth}

Local pull copy works https Setup
    Default Setup
    Create Test File   tpc_test_https  content=Hello World!  sa=${sa.auth}

Local pull copy works https Teardown
    Default Teardown
    Remove Test File   tpc_test_https
    Remove Test File   tpc_test_https  ${sa.auth}

Overwrite header recognized Setup
    Default Setup
    Create Test File   tpc_test

Overwrite header recognized Teardown
    Default Teardown
    Remove Test File   tpc_test

Local pull copy works oauth and https Setup
    Default Setup
    Create Test File   tpc_test_oauth_https  content=Hello World!  sa=${sa.oauth}

Local pull copy works oauth and https Teardown
    Default Teardown
    Remove Test File   tpc_test_oauth_https
    Remove Test File   tpc_test_oauth_https  ${sa.oauth}

Local push copy works Setup
    Default Setup
    Create Test File   tpc_test_push  content=Hello World!

Local push copy works Teardown
    Default Teardown
    Remove Test File  tpc_test_push
    Remove Test File  tpc_test_push  ${sa.oauth}

Oauth pull copy works Setup
    Default Setup
    Create Test File   oauth_pull_copy_works  content=Hello World!  sa=${sa.oauth}

Oauth pull copy works Teardown
    Default Teardown
    Remove Test File   oauth_pull_copy_works  ${sa.oauth}
    Remove Test File   oauth_pull_copy_works.copy  ${sa.oauth}

Oauth push copy works Setup
    Default Setup
    Create Test File   oauth_push_copy_works  content=Hello World!  sa=${sa.oauth}

Oauth push copy works Teardown
    Default Teardown
    Remove Test File   oauth_push_copy_works  ${sa.oauth}
    Remove Test File   oauth_push_copy_works.copy  ${sa.oauth}

*** Test cases ***

Local pull copy works
    [Tags]   voms  tpc
    [Setup]  Local pull copy works Setup
    ${dest}  DAVS URL  tpc_test
    ${src}  Remote DAV URL  tpc_test
    ${rc}  ${out}  Curl Voms Pull COPY Success  ${dest}  ${src}
    Davix Get Success   ${dest}
    [Teardown]  Local pull copy works Teardown

Local pull copy works https
    [Tags]   voms  tpc
    [Setup]  Local pull copy works https Setup
    ${dest}  DAVS URL  tpc_test_https
    ${src}  Remote DAVS URL  tpc_test_https  sa=${sa.auth}
    ${rc}  ${out}  Curl Voms Pull COPY Success  ${dest}  ${src}
    Davix Get Success   ${dest}
    [Teardown]  Local pull copy works https Teardown

Overwrite header recognized
    [Tags]   voms  tpc
    [Setup]  Overwrite header recognized Setup
    ${dest}  DAVS URL  tpc_test
    ${src}  Remote DAV URL  tpc_test
    ${opts}  Set Variable  -H "Overwrite: F" ${curl.opts.default} 
    ${rc}  ${out}  Curl Voms Pull COPY Failure  ${dest}  ${src}  ${opts}
    Should Contain  ${out}   412
    [Teardown]  Overwrite header recognized Teardown

Local pull copy works oauth and https
    [Tags]  oauth  tpc
    [Setup]  Local pull copy works oauth and https Setup
    ${dest}  DAVS URL  tpc_test_oauth_https
    ${src}   Remote DAVS URL  tpc_test_oauth_https  sa=${sa.oauth}
    ${opts}  Set Variable  -H "TransferHeaderAuthorization: Bearer %{${cred.oauth.env_var_name}}" ${curl.opts.default} 
    ${rc}  ${out}  Curl Voms Pull COPY Success  ${dest}  ${src}  ${opts}
    Davix Get Success   ${dest}
    [Teardown]  Local pull copy works oauth and https Teardown

Local push copy works
    [Tags]  voms  oauth  tpc  push  kkk
    [Setup]  Local push copy works Setup
    ${dst}  Remote DAVS URL  tpc_test_push  sa=${sa.oauth}
    ${src}  DAVS URL  tpc_test_push
    ${opts}  Set Variable  -H "TransferHeaderAuthorization: Bearer %{${cred.oauth.env_var_name}}" ${curl.opts.default} 
    ${rc}  ${out}  Curl Voms Push COPY Success  ${dst}  ${src}  ${opts}
    Davix Get Success   ${dst}  ${davix.opts.oauth}
    [Teardown]  Local push copy works Teardown

Oauth pull copy works
    [Tags]   oauth  tpc  pull
    [Setup]   Oauth pull copy works Setup 
    ${src}  Remote DAVS URL  oauth_pull_copy_works  sa=${sa.oauth}
    ${dst}   DAVS URL   oauth_pull_copy_works.copy  sa=${sa.oauth}
    ${opts}  Set Variable  -H "TransferHeaderAuthorization: Bearer %{${cred.oauth.env_var_name}}" ${curl.opts.default}
    ${opts}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}" ${opts}
    ${rc}  ${out}  Curl Pull COPY Success  ${dst}  ${src}  ${opts}
    Davix Get Success   ${dst}  ${davix.opts.oauth}
    [Teardown]  Oauth pull copy works Teardown

Oauth push copy works
    [Tags]   oauth  tpc  push
    [Setup]   Oauth push copy works Setup 
    ${src}  DAVS URL  oauth_push_copy_works  sa=${sa.oauth}
    ${dst}   Remote DAVS URL  oauth_push_copy_works.copy   sa=${sa.oauth}
    ${opts}  Set Variable  -H "TransferHeaderAuthorization: Bearer %{${cred.oauth.env_var_name}}" ${curl.opts.default}
    ${opts}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}" ${opts}
    ${rc}  ${out}  Curl Push COPY Success  ${dst}  ${src}  ${opts}
    Davix Get Success   ${dst}  ${davix.opts.oauth}
    [Teardown]  Oauth push copy works Teardown