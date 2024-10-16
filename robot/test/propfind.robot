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
  ${output}  Set variable  <?xml version='1.0' encoding='utf-8'?><propfind xmlns='DAV:'><allprop/></propfind>
  [Return]  ${output}

Get PROPFIND PROPNAME body
  ${output}  Set variable  <?xml version='1.0' encoding='utf-8'?><propfind xmlns='DAV:'><propname/></propfind>
  [Return]  ${output}

Get PROPFIND PROP body  [Arguments]  ${propname}
  ${output}  Set variable  <?xml version='1.0' encoding='utf-8'?><propfind xmlns='DAV:'><prop><${propname}/><prop/></propfind>
  [Return]  ${output}


*** Test cases ***
