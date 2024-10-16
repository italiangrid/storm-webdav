*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    common/setup_and_teardown.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

Default Tags   put

*** Keywords ***

Put Setup  [Arguments]  ${file_name}
    Default Setup
    Create Temporary File   ${file_name}   123456789

Put Teardown  [Arguments]  ${file_name}
    Default Teardown
    Remove Temporary File   ${file_name}
    Remove Test File   ${file_name}

Put directory Teardown  [Arguments]  ${file_name}  ${directory_name}=${file_name}
    Default Teardown
    Remove Temporary File   ${file_name}
    Remove Test Directory  ${directory_name}

*** Test cases ***

Put works
    [Tags]  voms
    [Setup]  Put Setup   put_works
    ${url}  DAVS URL  put_works
    ${rc}  ${out}  Curl Voms PUT Success  ${TEMPDIR}/put_works  ${url}
	Should Contain  ${out}  201 Created
    ${rc}  ${out}  Curl Voms Get Success  ${url}
    Should Contain  ${out}  123456789
    [Teardown]  Put Teardown   put_works

Put override works
    [Tags]  voms
    [Setup]  Put Setup   put_works
    ${url}  DAVS URL  put_works
    Curl Voms PUT Success  ${TEMPDIR}/put_works  ${url}
    ${rc}  ${out}  Curl Voms PUT Success  ${TEMPDIR}/put_works  ${url}
    Should Contain  ${out}  204 No Content
    [Teardown]  Put Teardown   put_works

Put with missing parent works
    [Tags]  voms
    [Setup]  Put Setup   put_works
    ${url}  DAVS URL  put-directory/put_works
    ${rc}  ${out}  Curl Voms PUT Success  ${TEMPDIR}/put_works  ${url}
    Should Contain  ${out}  201 Created
    [Teardown]  Run Keywords  Put Teardown   put_works
    ...         AND           Remove Test Directory  put-directory

Put over directory not allowed
    [Tags]  voms  known-issue
    [Setup]  Run Keywords  Setup directory  put_works
    ...      AND           Put Setup  put_works
    ${url}  DAVS URL  put_works
    ${rc}  ${out}  Curl Voms PUT Failure  ${TEMPDIR}/put_works  ${url}
    Should Contain  ${out}  405 Method not allowed
    [Teardown]  Put directory Teardown  put_works