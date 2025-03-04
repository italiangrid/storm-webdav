# SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
#
# SPDX-License-Identifier: Apache-2.0

*** Settings ***

Resource   common/utils.robot
Resource   common/credentials.robot

*** Variables ***

${davix.opts.voms}   -P grid
${davix.opts.oauth}   -H "Authorization: Bearer %{${cred.oauth.env_var_name}}" --capath /etc/grid-security/certificates

*** Keywords ***
Davix Push Copy Success  [Arguments]  ${src}  ${dst}  ${opts}=${davix.opts.voms}
    ${output}  Execute and Check Success  davix-cp ${opts} ${src} ${dst}
    RETURN  ${output}

Davix Push Copy Failure  [Arguments]  ${src}  ${dst}  ${opts}=${davix.opts.voms}
    ${output}  Execute and Check Failure  davix-cp ${opts} ${src} ${dst}
    RETURN  ${output}

Davix Get Success  [Arguments]   ${url}   ${opts}=${davix.opts.voms}
    ${output}   Execute and Check Success   davix-get ${opts} ${url}
    RETURN   ${output}

Davix Get Failure   [Arguments]   ${url}   ${opts}=${davix.opts.voms}
    ${output}   Execute and Check Failure   davix-get ${opts} ${url}
    RETURN   ${output}

Davix Put Success   [Arguments]   ${file}  ${url}   ${opts}=${davix.opts.voms}
    ${output}   Execute and Check Success   davix-put ${opts} ${file} ${url}
    RETURN   ${output}

Davix Put Failure   [Arguments]   ${file}  ${url}   ${opts}=${davix.opts.voms}
    ${output}   Execute and Check Failure   davix-put ${file} ${opts} ${url}
    RETURN   ${output}

Davix Rm Success  [Arguments]   ${url}   ${opts}=${davix.opts.voms}
    ${output}   Execute and Check Success  davix-rm ${opts} ${url}
    RETURN   ${output}

Davix Mkdir Success   [Arguments]   ${url}   ${opts}=${davix.opts.voms}
    ${output}   Execute and Check Success  davix-mkdir ${opts} ${url}
    RETURN   ${output}

Davix Mkdir Failure  [Arguments]   ${url}   ${opts}=${davix.opts.voms}
    ${output}   Execute and Check Failure  davix-mkdir ${opts} ${url}
    RETURN   ${output}