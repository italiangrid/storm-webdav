# SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
#
# SPDX-License-Identifier: Apache-2.0

*** Keywords ***

Default Setup
    Default VOMS credential

Default Teardown
    Unset VOMS credential

Setup file  [Arguments]  ${file_name}  ${content}=Hello World!  ${sa}=${sa.default}
    Default Setup
    Create Test File   ${file_name}  ${content}  ${sa}

Setup directory  [Arguments]  ${dir_name}
    Default Setup
    Create Test Directory   ${dir_name}

Teardown file  [Arguments]  ${file_name}
    Default Teardown
    Remove Test File   ${file_name}
    Remove Test File   ${file_name}.dest

Teardown file cross sa  [Arguments]  ${file_name}  ${sa_source}=${sa.default}  ${sa_dest}=${sa.oauth}
    Default Teardown
    Remove Test File   ${file_name}  sa=${sa_source}
    Remove Test File   ${file_name}.dest  sa=${sa_dest}

Teardown directory  [Arguments]  ${dir_name}
    Default Teardown
    Remove Test Directory   ${dir_name}
    Remove Test Directory   ${dir_name}.dest
