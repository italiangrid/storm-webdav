*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

Default Tags   move

*** Keywords ***

Default Setup
    Default VOMS credential

Default Teardown
    Unset VOMS credential

Setup move file  [Arguments]  ${file_name}
    Default Setup
    Create Test File   ${file_name}  content=Hello World!

Teardown move file  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File   ${file_name}
    Remove Test File   ${file_name}.moved

Teardown move file cross sa  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File   ${file_name}
    Remove Test File   ${file_name}.moved  sa=${sa.oauth}

*** Test cases ***

Move works
    [Tags]  voms  move
    [Setup]  Setup move file  move_works
    ${dest}  DAVS URL  move_works.moved
    ${source}  DAVS URL  move_works
    ${rc}  ${out}  Curl Voms MOVE Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown move file  move_works

Move across storage areas fails
    [Tags]  voms  move
    [Setup]  Setup move file  move_x_sa_works
    ${dest}  DAVS URL  move_x_sa_works.moved  sa=${sa.oauth}
    ${source}  DAVS URL  move_works
    ${rc}  ${out}  Curl Voms MOVE  ${dest}  ${source}
    Should Contain  ${out}   400
    Should Contain  ${out}   Move across storage areas is not supported
    [Teardown]   Teardown move file cross sa   move_x_sa_works
