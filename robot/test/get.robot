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