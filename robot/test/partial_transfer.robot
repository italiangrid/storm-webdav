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
    ${rc}  ${out}  Curl Voms Get Success   ${url}  ${curl.opts.default} -H "Range: 1-3,20-24"
    Should Contain  ${out}  Content-Range: bytes 1-3/13
    Should Contain  ${out}  est
    Should Contain  ${out}  Content-Length: 3
    [Teardown]  Teardown file  partial_works