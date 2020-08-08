*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

Default Tags   copy

*** Keywords ***

Default Setup
    Default VOMS credential

Default Teardown
    Unset VOMS credential

Setup copy file  [Arguments]  ${file_name}
    Default Setup
    Create Test File   ${file_name}  content=Hello World!

Teardown copy file  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File   ${file_name}
    Remove Test File   ${file_name}.copied

Teardown copy file cross sa  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File   ${file_name}
    Remove Test File   ${file_name}.copied  sa=${sa.oauth}

*** Test cases ***

Local copy works
    [Tags]  voms
    [Setup]  Setup copy file  copy_works
    ${dest}  DAVS URL  copy_works.copied
    ${source}  DAVS URL  copy_works
    ${rc}  ${out}  Curl Voms Push COPY Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown copy file  copy_works

Local copy across storage areas fails
    [Tags]  voms
    [Setup]  Setup copy file  copy_x_sa_works
    ${dest}  DAVS URL  copy_x_sa_works.copied  sa=${sa.oauth}
    ${source}  DAVS URL  copy_x_sa_works
    ${rc}  ${out}  Curl Voms Push COPY  ${dest}  ${source}
    Should Contain  ${out}   400
    Should Contain  ${out}   Local copy across storage areas is not supported
    [Teardown]   Teardown copy file cross sa   copy_x_sa_works
