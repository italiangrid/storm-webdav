*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    common/setup_and_teardown.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

Default Tags   partial

*** Keywords ***

Partial Put Setup
    Default Setup
    Create Temporary File  pput0_test   0000000000
    Create Temporary File  pput1_test   1111111111

Partial Put Teardown
    Default Teardown
    Remove Test File  pput_test
    Remove Temporary File  pput0_test
    Remove Temporary File  pput1_test

*** Test cases ***

Partial Get works
    [Tags]  voms  get
    [Setup]  Setup file  partial_works  test123456789
    ${url}  DAVS URL  partial_works
    ${rc}  ${out}  Curl Voms Get Success   ${url}  ${curl.opts.default} -H "Range: 0-3"
    Should Contain  ${out}  test
    Should Not Contain  ${out}  123456789
    Should Contain  ${out}  Content-Length: 4
    ${rc}  ${out}  Curl Voms Get Success   ${url}  ${curl.opts.default} -H "Range: 4-7"
    Should Contain  ${out}  1234
    Should Contain  ${out}  Content-Length: 4
    ${rc}  ${out}  Curl Voms Get Success   ${url}  ${curl.opts.default} -H "Range: 9-12"
    Should Contain  ${out}  6789
    Should Contain  ${out}  Content-Length: 4
    [Teardown]  Teardown file  partial_works

Partial Get with multiple range
    [Tags]  voms  get
    [Setup]  Setup file  partial_works  test123456789
    ${url}  DAVS URL  partial_works
    ${rc}  ${out}  Curl Voms Get Success   ${url}  ${curl.opts.default} -H "Range: 1-3,5-7,10-11"
    Should Contain  ${out}  Content-Range: bytes 1-3/13
    Should Contain  ${out}  est
    Should Contain  ${out}  Content-Range: bytes 5-7/13
    Should Contain  ${out}  234
    Should Contain  ${out}  Content-Range: bytes 10-11/13
    Should Contain  ${out}  78
    [Teardown]  Teardown file  partial_works

Partial Get not entirely on range
    [Tags]  voms  get
    [Setup]  Setup file  partial_works  test123456789
    ${url}  DAVS URL  partial_works
    ${rc}  ${out}  Curl Voms Get Success   ${url}  ${curl.opts.default} -H "Range: 11-13"
    Should Contain  ${out}  Content-Range: bytes 11-12/13
    Should Contain  ${out}  89
    Should Contain  ${out}  Content-Length: 2
    [Teardown]  Teardown file  partial_works

Partial Get out of range
    [Tags]  voms  get
    [Setup]  Setup file  partial_works  test123456789
    ${url}  DAVS URL  partial_works
    ${rc}  ${out}  Curl Voms Get Failure   ${url}  ${curl.opts.default} -H "Range: 20-24"
    Should Match Regexp  ${out}  416 Requested Range Not Satisfiable|416 Range Not Satisfiable
    [Teardown]  Teardown file  partial_works

Partial Get out in one of multiple range
    [Tags]  voms  get
    [Setup]  Setup file  partial_works  test123456789
    ${url}  DAVS URL  partial_works
    ${rc}  ${out}  Curl Voms Get Success   ${url}  ${curl.opts.default} -H "Range: 1-3,20-24"
    Should Contain  ${out}  Content-Range: bytes 1-3/13
    Should Contain  ${out}  est
    Should Contain  ${out}  Content-Length: 3
    [Teardown]  Teardown file  partial_works

Partial Put works
    [Tags]  voms  put
    [Setup]  Partial Put Setup
    ${opts}  Set Variable  -H "Content-Range: bytes=0-3/*" ${curl.opts.default}
    ${dest}  DAVS Url  pput_test
    ${rc}  ${out}  Curl Voms Put Success  ${TEMPDIR}/pput0_test  ${dest}  
    ${rc}  ${out}  Curl Voms Put Success  ${TEMPDIR}/pput1_test  ${dest}  ${opts}
    [Teardown]  Partial Put Teardown