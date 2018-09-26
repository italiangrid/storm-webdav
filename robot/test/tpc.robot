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

*** Test cases ***

Local pull copy works
    [Tags]   voms  tpc
    [Setup]  Local pull copy works Setup
    ${dest}  DAVS URL  tpc_test
    ${src}  DAV URL  tpc_test
    ${rc}  ${out}  Curl Voms Pull COPY Success  ${dest}  ${src}
    Davix Get Success   ${dest}
    [Teardown]  Local pull copy works Teardown

Local pull copy works https
    [Tags]   voms  tpc
    [Setup]  Local pull copy works https Setup
    ${dest}  DAVS URL  tpc_test_https
    ${src}  DAVS URL  tpc_test_https  sa=${sa.auth}
    ${rc}  ${out}  Curl Voms Pull COPY Success  ${dest}  ${src}
    Davix Get Success   ${dest}
    [Teardown]  Local pull copy works https Teardown

Overwrite header recognized
    [Tags]   voms  tpc
    [Setup]  Overwrite header recognized Setup
    ${dest}  DAVS URL  tpc_test
    ${src}  DAV URL  tpc_test
    ${opts}  Set Variable  -H "Overwrite: F" ${curl.opts.default} 
    ${rc}  ${out}  Curl Voms Pull COPY Failure  ${dest}  ${src}  ${opts}
    Should Contain  ${out}   412
    [Teardown]  Overwrite header recognized Teardown