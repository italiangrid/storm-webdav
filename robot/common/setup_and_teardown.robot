*** Keywords ***

Default Setup
    Default VOMS credential

Default Teardown
    Unset VOMS credential

Setup file  [Arguments]  ${file_name}  ${content}=Hello World!
    Default Setup
    Create Test File   ${file_name}  ${content}

Setup directory  [Arguments]  ${dir_name}
    Default Setup
    Create Test Directory   ${dir_name}

Teardown file  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File   ${file_name}
    Remove Test File   ${file_name}.dest

Teardown file cross sa  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File   ${file_name}
    Remove Test File   ${file_name}.dest  sa=${sa.oauth}

Teardown directory  [Arguments]  ${dir_name}
    Default Teardown
    Remove Test Directory   ${dir_name}
    Remove Test Directory   ${dir_name}.dest