*** Settings ***

Resource   common/utils.robot

*** Variables ***

${davix.opts.default}   -P grid

*** Keywords ***

Davix Get Success  [Arguments]   ${url}   ${opts}=${davix.opts.default}
    ${output}   Execute and Check Success   davix-get ${opts} ${url}
    [Return]   ${output}

Davix Get Failure   [Arguments]   ${url}   ${opts}=${davix.opts.default}
    ${output}   Execute and Check Failure   davix-get ${opts} ${url}
    [Return]   ${output}

Davix Put Success   [Arguments]   ${file}  ${url}   ${opts}=${davix.opts.default}
    ${output}   Execute and Check Success   davix-put ${opts} ${file} ${url}
    [Return]   ${output}

Davix Put Failure   [Arguments]   ${file}  ${url}   ${opts}=${davix.opts.default}
    ${output}   Execute and Check Failure   davix-put ${file} ${opts} ${url}
    [Return]   ${output}

Davix Rm Success  [Arguments]   ${url}   ${opts}=${davix.opts.default}
    ${output}   Execute and Check Success  davix-rm ${opts} ${url}
    [Return]   ${output}

Davix Mkdir Success   [Arguments]   ${url}   ${opts}=${davix.opts.default}
    ${output}   Execute and Check Success  davix-mkdir ${opts} ${url}
    [Return]   ${output}

Davix Mkdir Failure  [Arguments]   ${url}   ${opts}=${davix.opts.default}
    ${output}   Execute and Check Failure  davix-mkdir ${opts} ${url}
    [Return]   ${output}