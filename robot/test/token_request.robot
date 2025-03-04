# SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
#
# SPDX-License-Identifier: Apache-2.0

*** Settings ***
Library     String
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    test/variables.robot


Test Setup  Default Setup
Test Teardown   Default Teardown

*** Keywords ***

Get access token
    ${opts}  Set variable   -d "grant_type=client_credentials" ${curl.opts.default}
    ${rc}  ${out}  Curl Voms POST Success  ${token.endpoint}  ${opts}
    ${response}   Get Line  ${out}  -1
    ${rc}  ${out}  Execute and Check Success  echo '${response}' | jq -r .access_token 
    RETURN  ${out}


Default Setup
    Default VOMS credential

Default Teardown
    Unset VOMS credential

Get works with locally issued token Setup
    Default Setup
    Create Test File   token_get_test

Get works with locally issued token Teardown
    Default Teardown
    Remove Test File   token_get_test

Get works with locally issued token fga Setup
    Default Setup
    Create Test File   token_get_test   sa=fga

Get works with locally issued token fga Teardown
    Default Teardown
    Remove Test File   token_get_test   sa=fga

Put works with locally issued token Setup
    Default Setup
    Create Temporary File   token_put_test   12345678

Put works with locally issued token Teardown
    Default Teardown
    Remove Temporary File   token_put_test

Put works with locally issued token fga Setup
    Default Setup
    Create Temporary File   token_put_test   12345678

Put works with locally issued token fga Teardown
    Default Teardown
    Remove Temporary File   token_put_test

*** Test cases ***

Get works with locally issued token
    [Tags]   voms  token  Get
    [Setup]   Get works with locally issued token Setup
    ${token}  Get access token
    ${url}   DAVS Url  token_get_test
    ${opts}  Set variable  -H "Authorization: Bearer ${token}" --capath /etc/grid-security/certificates
    Davix Get Success  ${url}  ${opts}
    [Teardown]  Get works with locally issued token Teardown

Put works with locally issued token
    [Tags]   voms  token  Put
    [Setup]   Put works with locally issued token Setup
    ${token}  Get access token
    ${url}   DAVS Url  token_put_test
    ${opts}  Set variable  -H "Authorization: Bearer ${token}" --capath /etc/grid-security/certificates
    Davix Put Success  ${TEMPDIR}/token_put_test  ${url}  ${opts}
    Davix Get Success  ${url}  ${opts}
    Remove Test File  token_put_test
    [Teardown]  Put works with locally issued token Teardown

Get works with locally issued token fga
    [Tags]   voms  token  Get   fga
    [Setup]   Get works with locally issued token fga Setup
    ${token}  Get access token
    ${url}   DAVS Url  token_get_test  sa=fga
    ${opts}  Set variable  -H "Authorization: Bearer ${token}" --capath /etc/grid-security/certificates
    Davix Get Success  ${url}  ${opts}
    [Teardown]  Get works with locally issued token fga Teardown

Put works with locally issued token fga
    [Tags]   voms  token  Put   fga
    [Setup]   Put works with locally issued token fga Setup
    ${token}  Get access token
    ${url}   DAVS Url  token_put_test   sa=fga
    ${opts}  Set variable  -H "Authorization: Bearer ${token}" --capath /etc/grid-security/certificates
    Davix Put Success  ${TEMPDIR}/token_put_test  ${url}  ${opts}
    Davix Get Success  ${url}  ${opts}
    Remove Test File  token_put_test  ${sa.fga}
    [Teardown]  Put works with locally issued token fga Teardown
