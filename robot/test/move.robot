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

Default Tags   move


*** Test cases ***

Move works
    [Tags]  voms
    [Setup]  Setup file  move_works
    ${dest}  DAVS URL  move_works.dest
    ${source}  DAVS URL  move_works
    ${rc}  ${out}  Curl Voms MOVE Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown file  move_works

Move directory works
    [Tags]  voms
    [Setup]  Setup directory  move_works
    ${dest}  DAVS URL  move_works.dest
    ${source}  DAVS URL  move_works
    ${rc}  ${out}  Curl Voms MOVE Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown directory  move_works

Move not empty directory works
    [Tags]  voms
    [Setup]  Run Keywords   Setup directory  move_works
    ...      AND            Create Test File   move_works/file_move_works
    ${dest}  DAVS URL  move_works.dest
    ${source}  DAVS URL  move_works
    ${rc}  ${out}  Curl Voms MOVE Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown directory  move_works

Move override works
    [Tags]  voms
    [Setup]  Setup file  move_works
    ${dest}  DAVS URL  move_works.dest
    ${source}  DAVS URL  move_works
    Curl Voms Push COPY Success  ${dest}  ${source}
    ${overwriteHeader}  Set variable  --header "Overwrite: T"
    ${rc}  ${out}  Curl Voms MOVE Success  ${dest}  ${source}  ${curl.opts.default} ${overwriteHeader}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    Should Contain  ${out}  204 No Content
    [Teardown]   Teardown file  move_works

Move override fails
    [Tags]  voms
    [Setup]  Setup file  move_works
    ${dest}  DAVS URL  move_works.dest
    ${source}  DAVS URL  move_works
    Curl Voms Push COPY Success  ${dest}  ${source}
    ${overwriteHeader}  Set variable  --header "Overwrite: F"
    ${rc}  ${out}  Curl Voms MOVE Failure  ${dest}  ${source}  ${curl.opts.default} ${overwriteHeader}
    Should Contain  ${out}  412 Precondition Failed
    [Teardown]   Teardown file  move_works

Move not existent resource
    [Tags]  voms
    [Setup]  Default Setup
    ${dest}  DAVS URL  move_works.dest
    ${source}  DAVS URL  move_works
    ${rc}  ${out}  Curl Voms MOVE Failure  ${dest}  ${source}
    Should Contain  ${out}  404 Not Found
    [Teardown]   Default Teardown

Move with destination equal to source
    [Tags]  voms
    [Setup]  Setup file  move_works
    ${dest}  DAVS URL  move_works
    ${source}  DAVS URL  move_works
    ${overwriteHeader}  Set variable  --header "Overwrite: T"
    ${rc}  ${out}  Curl Voms MOVE Failure  ${dest}  ${source}  ${curl.opts.default} ${overwriteHeader}
    Should Contain  ${out}  403
    [Teardown]   Teardown file  move_works

Move across storage areas fails
    [Tags]  voms
    [Setup]  Setup file  move_x_sa_works
    ${dest}  DAVS URL  move_x_sa_works.dest  sa=${sa.oauth}
    ${source}  DAVS URL  move_works
    ${rc}  ${out}  Curl Voms MOVE  ${dest}  ${source}
    Should Contain  ${out}   400
    Should Contain  ${out}   Move across storage areas is not supported
    [Teardown]   Teardown file cross sa   move_x_sa_works
