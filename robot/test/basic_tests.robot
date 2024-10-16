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

Put works Setup
    Default Setup
    Create Temporary File   put_test   123456789

Put works Teardown
    Default Teardown
    Remove Temporary File   put_test

Mkdir works Teardown
    Default Teardown
    Remove Test Directory   mkdir_test

Single Test File Setup  [Arguments]  ${file_name}
    Default Setup
    Create Test File  ${file_name}

Single Test File Teardown  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File  ${file_name}

*** Test cases ***

Put works
    [Tags]  voms  put
    [Setup]  Put works Setup
    Davix Put Success  ${TEMPDIR}/put_test  ${davs.endpoint}/${sa.default}/put_test
    Davix Get Success  ${davs.endpoint}/${sa.default}/put_test
    Remove Test File   put_test
    [Teardown]  Put works Teardown

Post not allowed on content
    [Tags]  voms  post
    [Setup]  Single Test File Setup   test_post_not_allowed
    ${url}  DAVS Url   test_post_not_allowed
    ${rc}  ${out}  Curl Voms Post Failure  ${url}
    Should Contain  ${out}  405
    [Teardown]   Single Test File Teardown  test_post_not_allowed