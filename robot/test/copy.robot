*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

Default Tags   copy

*** Keywords ***

Default Setup
    Default VOMS credential

Default Teardown
    Unset VOMS credential

Setup copy file  [Arguments]  ${file_name}
    Default Setup
    Create Test File   ${file_name}  content=Hello World!

Setup copy directory  [Arguments]  ${dir_name}
    Default Setup
    Create Test Directory   ${dir_name}

Teardown copy file  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File   ${file_name}
    Remove Test File   ${file_name}.copied

Teardown copy file cross sa  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File   ${file_name}
    Remove Test File   ${file_name}.copied  sa=${sa.oauth}

Teardown copy directory  [Arguments]  ${dir_name}
    Default Teardown
    Remove Test Directory   ${dir_name}
    Remove Test Directory   ${dir_name}.copied

*** Test cases ***

Local copy works
    [Tags]  voms
    [Setup]  Setup copy file  copy_works
    ${dest}  DAVS URL  copy_works.copied
    ${source}  DAVS URL  copy_works
    ${rc}  ${out}  Curl Voms Push COPY Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown copy file  copy_works

Local copy directory works
    [Tags]  voms
    [Setup]  Setup copy directory  copy_works
    ${dest}  DAVS URL  copy_works.copied
    ${source}  DAVS URL  copy_works
    ${rc}  ${out}  Curl Voms Push COPY Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown copy directory  copy_works

Local copy not empty directory works
    [Tags]  voms
    [Setup]  Run Keywords   Setup copy directory  copy_works
    ...      AND            Create Test File   copy_works/file_copy_works
    ${dest}  DAVS URL  copy_works/file_copy_works.copied
    ${source}  DAVS URL  copy_works/file_copy_works
    ${rc}  ${out}  Curl Voms Push COPY Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown copy directory  copy_works

Local copy override works
    [Tags]  voms
    [Setup]  Setup copy file  copy_works
    ${dest}  DAVS URL  copy_works.copied
    ${source}  DAVS URL  copy_works
    Curl Voms Push COPY Success  ${dest}  ${source}
    ${overwriteHeader}  Set variable  --header "Overwrite: T"
    ${rc}  ${out}  Curl Voms Push COPY Success  ${dest}  ${source}  ${curl.opts.default} ${overwriteHeader}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Teardown copy file  copy_works

Local copy override fails
    [Tags]  voms
    [Setup]  Setup copy file  copy_works
    ${dest}  DAVS URL  copy_works.copied
    ${source}  DAVS URL  copy_works
    Curl Voms Push COPY Success  ${dest}  ${source}
    ${overwriteHeader}  Set variable  --header "Overwrite: F"
    ${rc}  ${out}  Curl Voms Push COPY Failure  ${dest}  ${source}  ${curl.opts.default} ${overwriteHeader}
    Should Contain  ${out}  412 Precondition Failed
    [Teardown]   Teardown copy file  copy_works

Local copy not existent file
    [Tags]  voms
    [Setup]  Default Setup
    ${dest}  DAVS URL  copy_works.copied
    ${source}  DAVS URL  copy_works
    ${rc}  ${out}  Curl Voms Push COPY Failure  ${dest}  ${source}
    Should Contain  ${out}  404 Not Found
    [Teardown]   Default Teardown

Local copy with destination equal to source
    [Tags]  voms
    [Setup]  Setup copy file  copy_works
    ${dest}  DAVS URL  copy_works
    ${source}  DAVS URL  copy_works
    ${overwriteHeader}  Set variable  --header "Overwrite: T"
    ${rc}  ${out}  Curl Voms Push COPY Failure  ${dest}  ${source}  ${curl.opts.default} ${overwriteHeader}
    Should Contain  ${out}  403
    [Teardown]   Teardown copy file  copy_works

Local copy across storage areas fails
    [Tags]  voms
    [Setup]  Setup copy file  copy_x_sa_works
    ${dest}  DAVS URL  copy_x_sa_works.copied  sa=${sa.oauth}
    ${source}  DAVS URL  copy_x_sa_works
    ${rc}  ${out}  Curl Voms Push COPY  ${dest}  ${source}
    Should Contain  ${out}   400
    Should Contain  ${out}   Local copy across storage areas is not supported
    [Teardown]   Teardown copy file cross sa   copy_x_sa_works
