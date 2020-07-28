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

Get works Setup
    Default Setup
    Create Test File   get_test

Get works Teardown
    Default Teardown
    Remove Test File   get_test

Put works Setup
    Default Setup
    Create Temporary File   put_test   123456789

Put works Teardown
    Default Teardown
    Remove Temporary File   put_test

Rm works Setup
    Default Setup
    Create Test File   rm_test

Rm works Teardown
    Default Setup
    Remove Test File   rm_test

Mkdir works Teardown
    Default Teardown
    Remove Test Directory   mkdir_test

Partial Get Works Setup
    Default Setup
    Create Test File   pget_test   1x2y456789

Partial Get Works Teardown
    Default Setup
    Remove Test File   pget_test

Partial Put Works Setup
    Default Setup
    Create Temporary File  pput0_test   0000000000
    Create Temporary File  pput1_test   1111111111

Partial Put Works Teardown
    Default Teardown
    Remove Test File  pput_test
    Remove Temporary File  pput0_test
    Remove Temporary File  pput1_test

Single Test File Setup  [Arguments]  ${file_name}
    Default Setup
    Create Test File  ${file_name}

Single Test File Teardown  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File  ${file_name}
  
Head works on large files setup   [Arguments]   ${file_name}
    Default setup
    Create Test File With Size  ${file_name}  2g

Head works on large files teardown   [Arguments]   ${file_name}
    Default Teardown
    Remove Test File  ${file_name}

*** Test cases ***

Get works
    [Tags]   voms  get
    [Setup]   Get works Setup
    Davix Get Success   ${davs.endpoint}/${sa.default}/get_test
    [Teardown]   Get works Teardown

Get returns 404 for file that does not exist
    [Tags]   voms  get
    ${rc}  ${out}   Davix Get Failure   ${davs.endpoint}/${sa.default}/does_not_exist
    Should Contain  ${out}   404
    ${rc}  ${out}   Davix Get Failure   ${davs.endpoint}/${sa.default}/does_not_exist/also
    Should Contain  ${out}   404

Put works
    [Tags]  voms  put
    [Setup]  Put works Setup
    Davix Put Success  ${TEMPDIR}/put_test  ${davs.endpoint}/${sa.default}/put_test
    Davix Get Success  ${davs.endpoint}/${sa.default}/put_test
    Remove File   put_test
    [Teardown]  Put works Teardown

Rm works
    [Tags]  voms  rm
    [Setup]  Rm works Setup
    Davix Get Success   ${davs.endpoint}/${sa.default}/rm_test
    Davix Rm Success   ${davs.endpoint}/${sa.default}/rm_test
    ${rc}  ${out}   Davix Get Failure   ${davs.endpoint}/${sa.default}/rm_test
    Should Contain  ${out}   404
    [Teardown]   Rm works teardown

Mkdir works
    [Tags]  voms  Mkdir
    ## There's a bug in Davix which returns 0 even if the mkdir call fails
    ## Davix Mkdir Success   ${davs.endpoint}/${sa.default}/mkdir_test
    ${rc}  ${out}  Curl Voms MKCOL Success   ${davs.endpoint}/${sa.default}/mkdir_test
    [Teardown]   Mkdir works teardown

Partial Get works
    [Tags]  voms  get  partial
    [Setup]  Partial Get Works Setup
    ${opts}  Set Variable  -H "Range: 0-3" ${curl.opts.default}
    ${rc}  ${out}  Curl Voms Get Success   ${davs.endpoint}/${sa.default}/pget_test  ${opts}
    Should Contain  ${out}  1x2y  
    Should Contain  ${out}  Content-Length: 4
    [Teardown]  Partial Get Works Teardown

Partial Put works
    [Tags]  voms  put  partial  
    [Setup]  Partial Put Works Setup
    ${opts}  Set Variable  -H "Content-Range: bytes=0-3/*" ${curl.opts.default}
    ${dest}  DAVS Url  pput_test
    ${rc}  ${out}  Curl Voms Put Success  ${TEMPDIR}/pput0_test  ${dest}  
    ${rc}  ${out}  Curl Voms Put Success  ${TEMPDIR}/pput1_test  ${dest}  ${opts}
    [Teardown]  Partial Put Works Teardown

Post not allowed on content
    [Tags]  voms  post
    [Setup]  Single Test File Setup   test_post_not_allowed
    ${url}  DAVS Url   test_post_not_allowed
    ${rc}  ${out}  Curl Voms Post Failure  ${url}
    Should Contain  ${out}  405 Method Not Allowed
    [Teardown]   Single Test File Teardown  test_post_not_allowed

Head works on large files
    [Tags]  voms  head
    [Setup]  Head works on large files setup  hwlf
    ${rc}  ${out}  Curl Voms HEAD Success  ${davs.endpoint}/${sa.default}/hwlf
    Should Contain  ${out}  Content-Length: 2147483648
    [Teardown]   Head works on large files teardown   hwlf