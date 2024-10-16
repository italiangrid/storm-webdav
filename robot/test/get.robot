*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    common/setup_and_teardown.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

Default Tags   get


*** Test cases ***

Get works
    [Tags]   voms
    [Setup]   Setup file  get_works
    ${url}  DAVS URL  get_works
    ${rc}  ${out}  Curl Voms GET Success  ${url}
    Should Contain  ${out}  Hello World!
    Davix Get Success   ${url}  ${davix.opts.voms}
    [Teardown]   Teardown file  get_works

Get directory works
    [Tags]  voms
    [Setup]  Setup directory  get_works
    ${url}  DAVS URL  get_works
    ${rc}  ${out}  Curl Voms GET Success  ${url}
    Davix Get Success   ${url}  ${davix.opts.voms}
    [Teardown]   Teardown directory  get_works

Get not empty directory works
    [Tags]  voms
    [Setup]  Run Keywords   Setup directory  get_works
    ...      AND            Create Test File   get_works/file_get_works
    ${url}  DAVS URL  get_works
    ${rc}  ${out}  Curl Voms GET Success  ${url}
    Davix Get Success   ${url}  ${davix.opts.voms}
    [Teardown]   Teardown directory  get_works

Get root directory works
    [Tags]  voms
    [Setup]  Default Setup
    ${url}  Set Variable  ${davs.endpoint}/${sa.default}
    ${rc}  ${out}  Curl Voms GET Success  ${url}
    Davix Get Success   ${url}  ${davix.opts.voms}
    [Teardown]   Default Teardown

Get not existent resource
    [Tags]   voms
    ${rc}  ${out}   Davix Get Failure   ${davs.endpoint}/${sa.default}/does_not_exist
    Should Contain  ${out}   404
    ${rc}  ${out}   Davix Get Failure   ${davs.endpoint}/${sa.default}/does_not_exist/also
    Should Contain  ${out}   404

Partial Get works
    [Tags]  voms  partial
    [Setup]  Setup file  get_works  test123456789
    ${url}  DAVS URL  get_works
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
    ${rc}  ${out}  Curl Voms Get Success   ${url}  ${curl.opts.default} -H "Range: 1-3,5-7,10-11"
    Should Contain  ${out}  Content-Range: bytes 1-3/13
    Should Contain  ${out}  est
    Should Contain  ${out}  Content-Range: bytes 5-7/13
    Should Contain  ${out}  234
    Should Contain  ${out}  Content-Range: bytes 10-11/13
    Should Contain  ${out}  78
    ${rc}  ${out}  Curl Voms Get Success   ${url}  ${curl.opts.default} -H "Range: 11-13"
    Should Contain  ${out}  Content-Range: bytes 11-12/13
    Should Contain  ${out}  89
    Should Contain  ${out}  Content-Length: 2
    ${rc}  ${out}  Curl Voms Get Failure   ${url}  ${curl.opts.default} -H "Range: 20-24"
    Should Match Regexp  ${out}  416 Requested Range Not Satisfiable|416 Range Not Satisfiable
    ${rc}  ${out}  Curl Voms Get Success   ${url}  ${curl.opts.default} -H "Range: 1-3,20-24"
    Should Contain  ${out}  Content-Range: bytes 1-3/13
    Should Contain  ${out}  est
    Should Contain  ${out}  Content-Length: 3
    [Teardown]  Teardown file  get_works