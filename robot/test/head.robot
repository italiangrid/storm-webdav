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

Default Tags   head


*** Test cases ***

Head works
    [Tags]  voms
    [Setup]  Setup file  head_works
    ${url}  DAVS URL  head_works
    ${rc}  ${out}  Curl Voms HEAD Success  ${url}
    Should Contain  ${out}  Content-Length: 12
    [Teardown]   Teardown file  head_works

Head directory works
    [Tags]  voms
    [Setup]  Setup directory  head_works
    ${url}  DAVS URL  head_works
    ${rc}  ${out}  Curl Voms HEAD Success  ${url}
    Should Contain  ${out}  Content-Length:
    [Teardown]   Teardown directory  head_works

Head not empty directory works
    [Tags]  voms
    [Setup]  Run Keywords   Setup directory  head_works
    ...      AND            Create Test File   head_works/file_head_works   some-text
    ${url}  DAVS URL  head_works
    ${rc}  ${out}  Curl Voms HEAD Success  ${url}
    Should Contain  ${out}  Content-Length:
    ${rc}  ${out}  Curl Voms HEAD Success  ${url}/file_head_works
    Should Contain  ${out}  Content-Length: 9
    [Teardown]   Teardown directory  head_works

Head root directory works
    [Tags]  voms
    [Setup]  Default Setup
    ${url}  Set Variable  ${davs.endpoint}/${sa.default}
    ${rc}  ${out}  Curl Voms HEAD Success  ${url}
    Should Contain  ${out}  Content-Length:
    [Teardown]   Default Teardown

Head not existent resource
    [Tags]   voms
    ${rc}  ${out}   Curl Voms HEAD Failure   ${davs.endpoint}/${sa.default}/does_not_exist
    Should Contain  ${out}   404
    ${rc}  ${out}   Curl Voms HEAD Failure   ${davs.endpoint}/${sa.default}/does_not_exist/also
    Should Contain  ${out}   404

Head works on large files
    [Tags]  voms
    [Setup]  Run Keywords  Default setup
    ...      AND  Create Test File With Size  hwlf  2G
    ${rc}  ${out}  Curl Voms HEAD Success  ${davs.endpoint}/${sa.default}/hwlf
    Should Contain  ${out}  ength: 2147483648
    [Teardown]   Teardown file  hwlf
