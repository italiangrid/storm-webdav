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

Set extended attr  [Arguments]  ${file}  ${attr}  ${attr_value}

Set checksum attr  [Arguments]  ${file}  ${checksum}

Get checksum works setup
    Default Setup
    Create Temporary File  checksum_test  123456789

Get checksum works Teardown
    Default Teardown
    Remove Test File  checksum_test
    Remove Temporary File   checksum_test

*** Test cases ***

Get checksum works
    [Setup]  Get checksum works setup
    [Tags]  voms  checksum  put
    ${dst}  DAVS Url  checksum_test
    Davix Put Success  ${TEMPDIR}/checksum_test  ${dst}
    ${rc}  ${out}  Curl Voms Get Success  ${dst}
    Should Contain  ${out}  Digest: adler32=91e01de
    [Teardown]  Get checksum works Teardown