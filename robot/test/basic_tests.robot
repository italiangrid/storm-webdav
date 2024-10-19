*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    common/setup_and_teardown.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

*** Test cases ***

Post not allowed on content
    [Tags]  voms  post
    [Setup]  Setup file   test_post_not_allowed
    ${url}  DAVS Url   test_post_not_allowed
    ${rc}  ${out}  Curl Voms Post Failure  ${url}
    Should Contain  ${out}  405 Method Not Allowed
    [Teardown]   Teardown file  test_post_not_allowed

Rename file with missing parent
    [Tags]   voms
    [Setup]   Setup file  rename-me
    ${source}  DAVS URL  rename-me
    ${dest}  DAVS URL  /parent-dir/child-dir/rename-me
    ${rc}  ${out}  Curl Voms GET Success  ${source}
    Should Contain  ${out}  Hello World!
    ${rc}  ${out}  Curl Voms HEAD Failure  ${dest}
    Should Contain  ${out}  404
    ${rc}  ${out}  Curl Voms HEAD Failure  ${davs.endpoint}/${sa.default}/parent-dir/child-dir
    Should Contain  ${out}  404
    ${rc}  ${out}  Curl Voms HEAD Failure  ${davs.endpoint}/${sa.default}/parent-dir
    Should Contain  ${out}  404
    Curl Voms MKCOL Success  ${davs.endpoint}/${sa.default}/parent-dir
    Curl Voms MKCOL Success  ${davs.endpoint}/${sa.default}/parent-dir/child-dir
    ${rc}  ${out}  Curl Voms MOVE Success  ${dest}  ${source}
    Davix Get Success   ${dest}  ${davix.opts.voms}
    [Teardown]   Run Keywords  Default Teardown
    ...          AND           Remove Test File   rename-me
    ...          AND           Remove Test Directory   parent-dir