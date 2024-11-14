*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    common/setup_and_teardown.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

Default Tags   mkcol


*** Test cases ***

Mkcol works
    [Tags]  voms
    ${url}  DAVS URL  mkcol_works
    Curl Voms MKCOL Success   ${url}
    ${rc}  ${out}  Curl Voms HEAD Success  ${url}
    Should Contain  ${out}  Content-Length: 4096
    [Teardown]   Teardown directory  mkcol_works

Mkcol with missing parent
    [Tags]  voms
    ${url}  DAVS URL  missing-dir/mkcol_works
    ${rc}  ${out}  Curl Voms MKCOL Failure   ${url}
    Should Contain  ${out}  409 Conflict
    [Teardown]   Teardown directory  missing-dir

Mkcol on existent resource
    [Tags]  voms
    [Setup]  Setup directory  mkcol_works
    ${url}  DAVS URL  mkcol_works
    ${rc}  ${out}  Curl Voms MKCOL Failure   ${url}
    Should Contain  ${out}  405 Method Not Allowed
    [Teardown]   Teardown directory  mkcol_works