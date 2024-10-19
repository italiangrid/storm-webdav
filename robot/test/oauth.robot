*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown


*** Test cases ***

OAuth Get Works
    [Tags]  oauth  get
    [Setup]  Create Test File  oauth_get_test   Hello world!   ${sa.oauth}
    ${url}   DAVS URL  oauth_get_test  ${sa.oauth}
    Davix Get Success   ${url}  ${davix.opts.oauth}
    [Teardown]  Remove Test File   oauth_get_test  ${sa.oauth}

OAuth Put works
    [Tags]  oauth  put
    [Setup]  Create Temporary File   oauth_put_test   123456789
    ${url}  DAVS URL  oauth_put_test  ${sa.oauth}
    Davix Put Success  ${TEMPDIR}/oauth_put_test  ${url}  ${davix.opts.oauth}
    Davix Get Success  ${url}  ${davix.opts.oauth}
    Remove Test File   oauth_put_test  ${sa.oauth}
    [Teardown]  Remove Temporary File  oauth_put_test