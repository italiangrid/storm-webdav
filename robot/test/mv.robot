*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

Default Tags   mv

*** Keywords ***

Default Setup
    Default VOMS credential

Default Teardown
    Unset VOMS credential

Setup mv file  [Arguments]  ${file_name}
    Default Setup
    Create Test File   ${file_name}  content=Hello World!

Teardown mv file  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File   ${file_name}
    Remove Test File   ${file_name}.moved

Teardown mv file cross sa  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File   ${file_name}
    Remove Test File   ${file_name}.moved  sa=${sa.oauth}

*** Test cases ***

Local mv works
    [Tags]  voms  mv
    [Setup]  Setup mv file  mv_works
    ${dest}  DAVS URL  mv_works.moved
    ${source}  DAVS URL  mv_works
    ${rc}  ${out}  Curl Voms MOVE Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown mv file  mv_works

Mv across storage areas fails
    [Tags]  voms  mv
    [Setup]  Setup mv file  mv_x_sa_works
    ${dest}  DAVS URL  mv_x_sa_works.moved  sa=${sa.oauth}
    ${source}  DAVS URL  mv_works
    ${rc}  ${out}  Curl Voms MOVE  ${dest}  ${source}
    Should Contain  ${out}   400
    Should Contain  ${out}   Move across storage areas is not supported
    [Teardown]   Teardown mv file cross sa   mv_x_sa_works
