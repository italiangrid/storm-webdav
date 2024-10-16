*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    common/setup_and_teardown.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

Default Tags   checksum


*** Test cases ***

Get checksum works
    [Setup]  Run Keywords   Default Setup
    ...      AND            Create Temporary File  checksum_works  123456789
    [Tags]  voms  put
    ${dst}  DAVS URL  checksum_works
    Davix Put Success  ${TEMPDIR}/checksum_works  ${dst}
    ${rc}  ${out}  Curl Voms GET Success  ${dst}
    Should Contain  ${out}  Digest: adler32=091e01de
    [Teardown]  Run Keywords   Teardown file  checksum_works
    ...         AND            Remove Temporary File   checksum_works