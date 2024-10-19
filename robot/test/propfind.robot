*** Settings ***
Resource    common/storage_areas.robot
Resource    common/credentials.robot
Resource    common/davix.robot
Resource    common/curl.robot
Resource    common/setup_and_teardown.robot
Resource    test/variables.robot

Test Setup  Default Setup
Test Teardown   Default Teardown

Default Tags   propfind

*** Keywords ***

Get PROPFIND ALLPROP body
  ${output}  Set variable  "<?xml version='1.0' encoding='utf-8' ?><D:propfind xmlns:D='DAV:'><D:allprop/></D:propfind>"
  [Return]  ${output}

Get PROPFIND PROPNAME body
  ${output}  Set variable  "<?xml version='1.0' encoding='utf-8' ?><D:propfind xmlns:D='DAV:'><D:propname/></D:propfind>"
  [Return]  ${output}

Get PROPFIND PROP body  [Arguments]  ${propname}
  ${output}  Set variable  "<?xml version='1.0' encoding='utf-8' ?><D:propfind xmlns:D='DAV:'><D:prop><D:${propname}/><prop/></D:propfind>"
  [Return]  ${output}


*** Test cases ***

Propfind allprop works
    [Tags]   voms
    [Setup]   Setup file  propfind_works
    ${url}  DAVS URL  propfind_works
    ${body}  Get PROPFIND ALLPROP body
    ${rc}  ${out}  Curl Voms PROPFIND  ${url}  ${body}
    Should Contain  ${out}  <ns1:Checksum></ns1:Checksum>
    Should Contain  ${out}  <d:iscollection>FALSE</d:iscollection>
    Should Contain  ${out}  <d:displayname>propfind_works</d:displayname> 
    Should Contain  ${out}  <d:status>HTTP/1.1 200 OK</d:status>
    Should Contain  ${out}  <d:getcontentlength>12</d:getcontentlength>
    [Teardown]   Teardown file  propfind_works

Propfind allprop not empty directory works
    [Tags]  voms
    [Setup]  Run Keywords   Setup directory  propfind_works
    ...      AND            Create Test File   propfind_works/file_propfind_works
    ${url}  DAVS URL  propfind_works
    ${body}  Get PROPFIND ALLPROP body
    ${rc}  ${out}  Curl Voms PROPFIND  ${url}  ${body}
    Should Contain  ${out}  <ns1:Checksum></ns1:Checksum>
    Should Contain  ${out}  <d:iscollection>FALSE</d:iscollection><d:displayname>file_propfind_works</d:displayname>
    Should Contain  ${out}  <d:iscollection>TRUE</d:iscollection><d:displayname>propfind_works</d:displayname>
    Should Contain  ${out}  <d:status>HTTP/1.1 200 OK</d:status>
    Should Contain  ${out}  <d:getcontentlength>0</d:getcontentlength>
    [Teardown]   Teardown directory  propfind_works

Propfind propname works
    [Tags]   voms
    [Setup]   Setup file  propfind_works
    ${url}  DAVS URL  propfind_works
    ${body}  Get PROPFIND PROPNAME body
    ${rc}  ${out}  Curl Voms PROPFIND  ${url}  ${body}
    Should Contain  ${out}  <d:displayname>propfind_works</d:displayname> 
    [Teardown]   Teardown file  propfind_works

Propfind status property works
    [Tags]   voms
    [Setup]   Setup file  propfind_works
    ${url}  DAVS URL  propfind_works
    ${body}  Get PROPFIND PROP body  status
    ${rc}  ${out}  Curl Voms PROPFIND  ${url}  ${body}
    Should Contain  ${out}  <d:status>HTTP/1.1 200 OK</d:status>
    [Teardown]   Teardown file  propfind_works