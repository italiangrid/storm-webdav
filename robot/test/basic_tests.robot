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
  
Head works on large files setup   [Arguments]   ${file_name}
    Default setup
    Create Test File With Size  ${file_name}  2G

Head works on large files teardown   [Arguments]   ${file_name}
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

Mkdir works
    [Tags]  voms  Mkdir
    ## There's a bug in Davix which returns 0 even if the mkdir call fails
    ## Davix Mkdir Success   ${davs.endpoint}/${sa.default}/mkdir_test
    ${rc}  ${out}  Curl Voms MKCOL Success   ${davs.endpoint}/${sa.default}/mkdir_test
    [Teardown]   Mkdir works teardown

Post not allowed on content
    [Tags]  voms  post
    [Setup]  Single Test File Setup   test_post_not_allowed
    ${url}  DAVS Url   test_post_not_allowed
    ${rc}  ${out}  Curl Voms Post Failure  ${url}
    Should Contain  ${out}  405
    [Teardown]   Single Test File Teardown  test_post_not_allowed

Head works on large files
    [Tags]  voms  head
    [Setup]  Head works on large files setup  hwlf
    ${rc}  ${out}  Curl Voms HEAD Success  ${davs.endpoint}/${sa.default}/hwlf
    Should Contain  ${out}  ength: 2147483648
    [Teardown]   Head works on large files teardown   hwlf