*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

*** Keywords ***

Default Setup
    Default VOMS credential

Default Teardown
    Unset VOMS credential

Pull copy works Setup
    Default Setup
    Create Test File With Size   tpc_test  size=10485760  sa=${sa.noauth}

Pull copy works Teardown
    Default Teardown
    Remove Test File   tpc_test
    Remove Test File   tpc_test  sa=${sa.noauth}

Pull copy works https Setup
    Default Setup
    Create Test File   tpc_test_https  size=10485760   sa=${sa.noauth}

Pull copy works https Teardown
    Default Teardown
    Remove Test File   tpc_test_https
    Remove Test File   tpc_test_https  ${sa.noauth}

Overwrite header recognized Setup
    Default Setup
    Create Test File   tpc_test

Overwrite header recognized Teardown
    Default Teardown
    Remove Test File   tpc_test

Pull copy works oauth and https Setup
    Default Setup
    Create Test File With Size   tpc_test_oauth_https  size=10485760  sa=${sa.oauth}

Pull copy works oauth and https Teardown
    Default Teardown
    Remove Test File   tpc_test_oauth_https
    Remove Test File   tpc_test_oauth_https  ${sa.oauth}

Push copy works Setup
    Default Setup
    Create Test File With Size   tpc_test_push  size=10485760

Push copy works Teardown
    Default Teardown
    Remove Test File  tpc_test_push
    Remove Test File  tpc_test_push  ${sa.oauth}

Oauth pull copy works Setup
    Default Setup
    Create Test File With Size   oauth_pull_copy_works  size=10485760  sa=${sa.oauth}

Oauth pull copy works Teardown
    Default Teardown
    Remove Test File   oauth_pull_copy_works  ${sa.oauth}
    Remove Test File   oauth_pull_copy_works.copy  ${sa.oauth}

Oauth push copy works Setup
    Default Setup
    Create Test File With Size   oauth_push_copy_works  size=10485760  sa=${sa.oauth}

Oauth push copy works Teardown
    Default Teardown
    Remove Test File   oauth_push_copy_works  ${sa.oauth}
    Remove Test File   oauth_push_copy_works.copy  ${sa.oauth}

*** Test cases ***

Pull copy works
    [Tags]   voms  tpc
    [Setup]  Pull copy works Setup
    ${dest}  DAVS URL  tpc_test
    ${src}  Remote DAV URL  tpc_test
    ${rc}  ${out}  Curl Voms Pull COPY Success  ${dest}  ${src}
    Davix Get Success   ${dest}
    [Teardown]  Pull copy works Teardown

Pull copy works https
    [Tags]   voms  tpc
    [Setup]  Pull copy works https Setup
    ${dest}  DAVS URL  tpc_test_https
    ${src}  Remote DAVS URL  tpc_test_https   sa=${sa.noauth}
    ${rc}  ${out}  Curl Voms Pull COPY Success  ${dest}  ${src}
    Davix Get Success   ${dest}
    [Teardown]  Pull copy works https Teardown

Overwrite header recognized
    [Tags]   voms  tpc
    [Setup]  Overwrite header recognized Setup
    ${dest}  DAVS URL  tpc_test
    ${src}  Remote DAV URL  tpc_test
    ${opts}  Set Variable  -H "Overwrite: F" ${curl.opts.default} 
    ${rc}  ${out}  Curl Voms Pull COPY Failure  ${dest}  ${src}  ${opts}
    Should Contain  ${out}   412
    [Teardown]  Overwrite header recognized Teardown

Pull copy works oauth and https
    [Tags]  oauth  tpc
    [Setup]  Pull copy works oauth and https Setup
    ${dest}  DAVS URL  tpc_test_oauth_https
    ${src}   Remote DAVS URL  tpc_test_oauth_https  sa=${sa.oauth}
    ${opts}  Set Variable  -H "TransferHeaderAuthorization: Bearer %{${cred.oauth.env_var_name}}" ${curl.opts.default}
    ${opts}  Set Variable  -H "ClientInfo: job-id=123; file-id=454; retry=0" ${opts}
    ${rc}  ${out}  Curl Voms Pull COPY Success  ${dest}  ${src}  ${opts}
    Curl Success   ${dest}   ${davix.opts.oauth}
    [Teardown]  Pull copy works oauth and https Teardown

Push copy works
    [Tags]  voms  oauth  tpc  push
    [Setup]  Push copy works Setup
    ${dst}  Remote DAVS URL  tpc_test_push  sa=${sa.oauth}
    ${src}  DAVS URL  tpc_test_push
    ${opts}  Set Variable  -H "TransferHeaderAuthorization: Bearer %{${cred.oauth.env_var_name}}" ${curl.opts.default} 
    ${opts}  Set Variable  -H "ClientInfo: job-id=123; file-id=455; retry=0" ${opts}
    ${rc}  ${out}  Curl Voms Push COPY Success  ${dst}  ${src}  ${opts}
    Curl Success   ${dst}   ${davix.opts.oauth}
    [Teardown]  Push copy works Teardown

Oauth pull copy works
    [Tags]   oauth  tpc  pull
    [Setup]   Oauth pull copy works Setup 
    ${src}  Remote DAVS URL  oauth_pull_copy_works  sa=${sa.oauth}
    ${dst}   DAVS URL   oauth_pull_copy_works.copy  sa=${sa.oauth}
    ${opts}  Set Variable  -H "TransferHeaderAuthorization: Bearer %{${cred.oauth.env_var_name}}" ${curl.opts.default}
    ${opts}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}" ${opts}
    ${opts}  Set Variable  -H "ClientInfo: job-id=123; file-id=456; retry=0" ${opts}
    ${rc}  ${out}  Curl Pull COPY Success  ${dst}  ${src}  ${opts}
    Curl Success   ${dst}   ${davix.opts.oauth}
    [Teardown]  Oauth pull copy works Teardown

Oauth push copy works
    [Tags]   oauth  tpc  push
    [Setup]   Oauth push copy works Setup 
    ${src}  DAVS URL  oauth_push_copy_works  sa=${sa.oauth}
    ${dst}   Remote DAVS URL  oauth_push_copy_works.copy   sa=${sa.oauth}
    ${opts}  Set Variable  -H "TransferHeaderAuthorization: Bearer %{${cred.oauth.env_var_name}}" ${curl.opts.default}
    ${opts}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}" ${opts}
    ${opts}  Set Variable  -H "ClientInfo: job-id=123; file-id=457; retry=0" ${opts}
    ${rc}  ${out}  Curl Push COPY Success  ${dst}  ${src}  ${opts}
    Curl Success   ${dst}   ${davix.opts.oauth}
    [Teardown]  Oauth push copy works Teardown