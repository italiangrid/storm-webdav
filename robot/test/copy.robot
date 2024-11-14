*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    common/setup_and_teardown.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

Default Tags   copy

*** Test cases ***

Copy works
    [Tags]  voms
    [Setup]  Setup file  copy_works
    ${dest}  DAVS URL  copy_works.dest
    ${source}  DAVS URL  copy_works
    ${rc}  ${out}  Curl Voms Push COPY Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown file  copy_works

Copy directory works
    [Tags]  voms
    [Setup]  Setup directory  copy_works
    ${dest}  DAVS URL  copy_works.dest
    ${source}  DAVS URL  copy_works
    ${rc}  ${out}  Curl Voms Push COPY Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown directory  copy_works

Copy not empty directory works
    [Tags]  voms
    [Setup]  Run Keywords   Setup directory  copy_works
    ...      AND            Create Test File   copy_works/file_copy_works
    ${dest}  DAVS URL  copy_works.dest
    ${source}  DAVS URL  copy_works
    ${rc}  ${out}  Curl Voms Push COPY Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown directory  copy_works

Copy override works
    [Tags]  voms
    [Setup]  Setup file  copy_works
    ${dest}  DAVS URL  copy_works.dest
    ${source}  DAVS URL  copy_works
    Curl Voms Push COPY Success  ${dest}  ${source}
    ${overwriteHeader}  Set variable  --header "Overwrite: T"
    ${rc}  ${out}  Curl Voms Push COPY Success  ${dest}  ${source}  ${curl.opts.default} ${overwriteHeader}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown file  copy_works

Copy override fails
    [Tags]  voms
    [Setup]  Setup file  copy_works
    ${dest}  DAVS URL  copy_works.dest
    ${source}  DAVS URL  copy_works
    Curl Voms Push COPY Success  ${dest}  ${source}
    ${overwriteHeader}  Set variable  --header "Overwrite: F"
    ${rc}  ${out}  Curl Voms Push COPY Failure  ${dest}  ${source}  ${curl.opts.default} ${overwriteHeader}
    Should Contain  ${out}  412 Precondition Failed
    [Teardown]   Teardown file  copy_works

Copy not existent resource
    [Tags]  voms
    [Setup]  Default Setup
    ${dest}  DAVS URL  copy_works.dest
    ${source}  DAVS URL  copy_works
    ${rc}  ${out}  Curl Voms Push COPY Failure  ${dest}  ${source}
    Should Contain  ${out}  404 Not Found
    [Teardown]   Default Teardown

Copy with destination equal to source
    [Tags]  voms
    [Setup]  Setup file  copy_works
    ${dest}  DAVS URL  copy_works
    ${source}  DAVS URL  copy_works
    ${overwriteHeader}  Set variable  --header "Overwrite: T"
    ${rc}  ${out}  Curl Voms Push COPY Failure  ${dest}  ${source}  ${curl.opts.default} ${overwriteHeader}
    Should Contain  ${out}  403
    [Teardown]   Teardown file  copy_works

Copy across storage areas fails
    [Tags]  voms
    [Setup]  Setup file  copy_x_sa_works
    ${dest}  DAVS URL  copy_x_sa_works.dest  sa=${sa.oauth}
    ${source}  DAVS URL  copy_x_sa_works
    ${rc}  ${out}  Curl Voms Push COPY  ${dest}  ${source}
    Should Contain  ${out}   400
    Should Contain  ${out}   Local copy across storage areas is not supported
    [Teardown]   Teardown file cross sa   copy_x_sa_works