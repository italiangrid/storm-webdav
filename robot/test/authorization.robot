*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    common/setup_and_teardown.robot
Resource    common/oidc-agent.robot
Resource    test/variables.robot

Test Setup  Get token
Test Teardown   Get token

*** Variables ***

${oauth.group.claim}   wlcg.groups
${oauth.group.value}   cms

${oauth.optional.group.claim}   ${oauth.group.claim}:/${oauth.optional.group.value}
${oauth.optional.group.value}   data-manager


*** Keywords ***

Setup directory fga  [Arguments]  ${dir_name}  ${file_name}=test_file
    Create Test Directory   ${dir_name}   ${sa.fga}
    Create Test File  ${dir_name}/${file_name}   Hello world!   ${sa.fga}

Teardown file fga  [Arguments]  ${file_name}
    Remove Test File  ${file_name}  ${sa.fga}

Teardown directory fga  [Arguments]  ${dir_name}
    Remove Test Directory  ${dir_name}  ${sa.fga}


*** Test cases ***

Read access allowed to anyone to the public area
    [Tags]  fga  get
    [Setup]  Setup directory fga  public
    ${url}   DAVS URL  public/test_file  ${sa.fga}
    ${rc}  ${out}  Curl Success   ${url}  ${curl.opts.default}
    Should Contain   ${out}  Hello world!
    [Teardown]  Teardown directory fga  public

List access allowed to anyone to the public area
    [Tags]  fga  propfind
    [Setup]  Setup directory fga  public
    ${url}   DAVS URL  public  ${sa.fga}
    ${rc}  ${out}  Curl Success   ${url}  -X PROPFIND ${curl.opts.default}
    Should Contain   ${out}  test_file
    [Teardown]  Teardown directory fga  public

Anonymous put not allowed to the public area
    [Tags]  fga  put
    [Setup]  Run Keywords  Create Temporary File   put_not_allowed   123456789
    ...      AND           Setup directory fga  public
    ${url}  DAVS URL   public/put_not_allowed  ${sa.fga}
    ${rc}  ${out}  Curl Error  ${url}  -X PUT -T ${TEMPDIR}/put_not_allowed ${curl.opts.default}
    Should Match Regexp  ${out}  401|403
    [Teardown]  Run Keywords  Remove Temporary File  put_not_allowed
    ...         AND           Teardown directory fga  public

Anonymous mkcol not allowed to the public area
    [Tags]  fga  mkcol
    [Setup]  Setup directory fga  public
    ${url}  DAVS URL  public/mkcol_not_allowed  ${sa.fga}
    ${rc}  ${out}  Curl Error  ${url}  -X MKCOL ${curl.opts.default}
    Should Match Regexp  ${out}  401|403
    [Teardown]  Teardown directory fga  public

Anonymous read not allowed outside the public area
    [Tags]  fga  get
    [Setup]  Setup directory fga  anonymous
    ${url}   DAVS URL  anonymous/test_file  ${sa.fga}
    ${rc}  ${out}  Curl Error   ${url}  ${curl.opts.default}
    Should Match Regexp  ${out}  401|403
    [Teardown]  Teardown directory fga  anonymous

Anonymous list not allowed outside the public area
    [Tags]  fga  propfind
    [Setup]  Setup directory fga  anonymous
    ${url}   DAVS URL  anonymous  ${sa.fga}
    ${rc}  ${out}  Curl Error   ${url}  -X PROPFIND ${curl.opts.default}
    Should Match Regexp  ${out}  401|403
    [Teardown]  Teardown directory fga  anonymous

Read access allowed to trusted issued tokens
    [Tags]  fga  get  oauth
    [Setup]  Setup directory fga  trusted_issuer
    ${token}  Get token  scope=-s openid
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}   DAVS URL  trusted_issuer/test_file  ${sa.fga}
    ${rc}  ${out}  Curl Success   ${url}  ${curl.opts.oauth} ${curl.opts.default}
    Should Contain   ${out}   Hello world!
    [Teardown]  Teardown directory fga  trusted_issuer

List access allowed to trusted issued tokens
    [Tags]  fga  propfind  oauth
    [Setup]  Setup directory fga  trusted_issuer
    ${token}  Get token  scope=-s openid
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}   DAVS URL  trusted_issuer  ${sa.fga}
    ${rc}  ${out}  Curl Success   ${url}  -X PROPFIND ${curl.opts.oauth} ${curl.opts.default}
    Should Contain   ${out}  test_file
    [Teardown]  Teardown directory fga  trusted_issuer

Put not allowed to the trusted issued tokens
    [Tags]  fga  put  oauth
    [Setup]  Create Temporary File   trusted_issuer   123456789
    ${token}  Get token  scope=-s openid
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}  DAVS URL  trusted_issuer  ${sa.fga}
    ${rc}  ${out}  Curl Error  ${url}  -X PUT -T ${TEMPDIR}/trusted_issuer ${curl.opts.oauth} ${curl.opts.default}
    Should Match Regexp  ${out}  401|403
    [Teardown]   Remove Temporary File  trusted_issuer

Mkcol not allowed to the trusted issued tokens
    [Tags]  fga  mkcol  oauth
    ${token}  Get token  scope=-s openid
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}  DAVS URL  trusted_issuer  ${sa.fga}
    ${rc}  ${out}  Curl Error  ${url}  -X MKCOL ${curl.opts.oauth} ${curl.opts.default}
    Should Match Regexp  ${out}  401|403

Read access allowed to the cms group in the namespace
    [Tags]  fga  get  oauth
    [Setup]  Setup directory fga  cms
    ${token}  Get token  scope=-s ${oauth.group.claim}
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}   DAVS URL  cms/test_file  ${sa.fga}
    ${rc}  ${out}  Curl Success   ${url}  ${curl.opts.oauth} ${curl.opts.default}
    Should Contain   ${out}   Hello world!
    [Teardown]  Teardown directory fga  cms

List access allowed to the cms group in the namespace
    [Tags]  fga  propfind  oaut
    [Setup]  Setup directory fga  cms
    ${token}  Get token  scope=-s ${oauth.group.claim}
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}   DAVS URL  cms  ${sa.fga}
    ${rc}  ${out}  Curl Success   ${url}  -X PROPFIND ${curl.opts.oauth} ${curl.opts.default}
    Should Contain   ${out}  test_file
    [Teardown]  Teardown directory fga  cms

Put allowed to the cms group in the namespace
    [Tags]  fga  put  oauth
    [Setup]  Run Keywords  Create Temporary File   cms_group   123456789
    ...      AND           Setup directory fga  cms
    ${token}  Get token  scope=-s ${oauth.group.claim}
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}  DAVS URL  cms/cms_group  ${sa.fga}
    ${rc}  ${out}  Curl Success  ${url}  -X PUT -T ${TEMPDIR}/cms_group ${curl.opts.oauth} ${curl.opts.default}
    Should Contain   ${out}   201 Created
    [Teardown]   Run Keywords  Remove Temporary File  cms_group
    ...          AND           Teardown directory fga  cms

Mkcol allowed to the cms group in the namespace
    [Tags]  fga  mkcol  oauth
    [Setup]  Setup directory fga  cms
    ${token}  Get token  scope=-s ${oauth.group.claim}
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}  DAVS URL  cms/cms_group  ${sa.fga}
    Curl Success  ${url}  -X MKCOL ${curl.opts.oauth} ${curl.opts.default}
    [Teardown]   Teardown directory fga  cms

Put denied to the cms group outside the namespace
    [Tags]  fga  put  oauth
    [Setup]  Run Keywords  Create Temporary File   denied   123456789
    ...      AND           Setup directory fga  denied
    ${token}  Get token  scope=-s ${oauth.group.claim}
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}  DAVS URL  denied/denied  ${sa.fga}
    ${rc}  ${out}  Curl Error  ${url}  -X PUT -T ${TEMPDIR}/denied ${curl.opts.oauth} ${curl.opts.default}
    Should Match Regexp  ${out}  401|403
    [Teardown]   Run Keywords  Remove Temporary File  denied
    ...          AND           Teardown directory fga  denied

Mkcol denied to the cms group outside the namespace
    [Tags]  fga  mkcol  oauth
    ${token}  Get token  scope=-s ${oauth.group.claim}
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}  DAVS URL  denied  ${sa.fga}
    ${rc}  ${out}  Curl Error  ${url}  -X MKCOL ${curl.opts.oauth} ${curl.opts.default}
    Should Match Regexp  ${out}  401|403

Read access allowed to data-manager group
    [Tags]  fga  get  oauth
    [Setup]  Setup directory fga  data-manager
    ${token}  Get token  scope=-s ${oauth.optional.group.claim}
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}   DAVS URL  data-manager/test_file  ${sa.fga}
    ${rc}  ${out}  Curl Success   ${url}  ${curl.opts.oauth} ${curl.opts.default}
    Should Contain   ${out}   Hello world!
    [Teardown]  Teardown directory fga  data-manager

List access allowed to data-manager group
    [Tags]  fga  propfind  oauth
    [Setup]  Setup directory fga  data-manager
    ${token}  Get token  scope=-s ${oauth.optional.group.claim}
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}   DAVS URL  data-manager  ${sa.fga}
    ${rc}  ${out}  Curl Success   ${url}  -X PROPFIND ${curl.opts.oauth} ${curl.opts.default}
    Should Contain   ${out}  test_file
    [Teardown]  Teardown directory fga  data-manager

Put allowed to data-manager group
    [Tags]  fga  put  oauth
    [Setup]  Create Temporary File   data-manager   123456789
    ${token}  Get token  scope=-s ${oauth.optional.group.claim}
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}  DAVS URL  data-manager  ${sa.fga}
    ${rc}  ${out}  Curl Success  ${url}  -X PUT -T ${TEMPDIR}/data-manager ${curl.opts.oauth} ${curl.opts.default}
    Should Contain   ${out}   201 Created
    [Teardown]   Run Keywords   Remove Temporary File  data-manager
    ...          AND            Teardown file fga  data-manager

Mkcol allowed to data-manager group
    [Tags]  fga  mkcol  oauth
    ${token}  Get token  scope=-s ${oauth.optional.group.claim}
    ${curl.opts.oauth}  Set Variable  -H "Authorization: Bearer %{${cred.oauth.env_var_name}}"
    ${url}  DAVS URL  data-manager  ${sa.fga}
    ${rc}  ${out}  Curl Success  ${url}  -X MKCOL ${curl.opts.oauth} ${curl.opts.default}
    [Teardown]   Teardown directory fga  data-manager