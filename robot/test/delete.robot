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

Default Tags   delete


*** Test cases ***

Delete works
    [Tags]  voms
    [Setup]  Setup file  delete_works
    ${url}  DAVS URL  delete_works
    ${rc}  ${out}  Curl Voms DELETE Success  ${url}
    ${rc}  ${out}   Davix Get Failure   ${url}  ${davix.opts.voms}
    Should Contain  ${out}   404
    [Teardown]   Teardown file  delete_works

Delete directory works
    [Tags]  voms
    [Setup]  Setup directory  delete_works
    ${url}  DAVS URL  delete_works
    ${rc}  ${out}  Curl Voms DELETE Success  ${url}
    ${rc}  ${out}   Davix Get Failure   ${url}  ${davix.opts.voms}
    Should Contain  ${out}   404
    [Teardown]   Teardown directory  delete_works

Delete not empty directory fails
    [Documentation]  Since v1.3.1 removing not empty directories is not allowed
    [Tags]  voms
    [Setup]  Run Keywords   Setup directory  delete_works
    ...      AND            Create Test File   delete_works/file_delete_works
    ${url}  DAVS URL  delete_works
    ${rc}  ${out}  Curl Voms DELETE Failure  ${url}
    Should Contain  ${out}  412 Precondition Failed
    [Teardown]   Teardown directory  delete_works

Delete not existent resource
    [Tags]  voms
    [Setup]  Default Setup
    ${url}  DAVS URL  delete_works
    ${rc}  ${out}  Curl Voms DELETE Failure  ${url}
    Should Contain  ${out}  404 Not Found
    [Teardown]   Default Teardown