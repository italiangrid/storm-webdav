# SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
#
# SPDX-License-Identifier: Apache-2.0

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

*** Keywords ***

Setup file for checksum  [Arguments]  ${file_name}  ${content}=Hello World!
    Default Setup
    Create Temporary File   ${file_name}  ${content}

Teardown file for checksum  [Arguments]  ${file_name}
    Default Teardown
    Teardown file   ${file_name}
    Remove Temporary File   ${file_name}


*** Test cases ***

Get checksum works
    [Setup]  Setup file for checksum  checksum_works  123456789
    [Tags]  voms  get
    ${url}  DAVS URL  checksum_works
    Davix Put Success  ${TEMPDIR}/checksum_works  ${url}
    ${rc}  ${out}  Curl Voms GET Success  ${url}
    Should Contain  ${out}  Digest: adler32=091e01de
    [Teardown]  Teardown file for checksum   checksum_works

Head checksum works
    [Setup]  Setup file for checksum  checksum_works  test123456789
    [Tags]  voms  put
    ${url}  DAVS URL  checksum_works
    Davix Put Success  ${TEMPDIR}/checksum_works  ${url}
    ${rc}  ${out}  Curl Voms HEAD Success  ${url}
    Should Contain  ${out}  Digest: adler32=1d3b039e
    [Teardown]  Teardown file for checksum   checksum_works