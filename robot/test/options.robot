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

Default Tags   options


*** Test cases ***

Options on storage area root works
    [Tags]  voms
    [Setup]  Default Setup
    ${url}  Set Variable  ${davs.endpoint}/${sa.default}
    ${rc}  ${out}  Curl Voms OPTIONS  ${url}
    Should Be Equal As Integers   ${rc}   0
    [Teardown]   Default Teardown

Options on file works
    [Tags]  voms
    [Setup]  Setup file  option_works
     ${url}  DAVS URL  option_works
    ${rc}  ${out}  Curl Voms OPTIONS  ${url}
    Should Be Equal As Integers   ${rc}   0
    [Teardown]   Teardown file  option_works

Options on directory works
    [Tags]  voms
    [Setup]  Setup directory  option_works
     ${url}  DAVS URL  option_works
    ${rc}  ${out}  Curl Voms OPTIONS  ${url}
    Should Be Equal As Integers   ${rc}   0
    [Teardown]   Teardown directory  option_works