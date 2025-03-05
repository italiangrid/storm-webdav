*** Keywords ***

Execute and Check Success   [Arguments]   ${cmd}
    Log   ${cmd}   level=debug
    ${rc}   ${output}    Run and Return RC And Output   ${cmd}
    Log   ${output}   level=debug
    Should Be Equal As Integers   ${rc}   0   ${cmd} exited with status ${rc} != 0 : ${output}   False
    RETURN   ${rc}  ${output}

Execute and Check Failure   [Arguments]   ${cmd}
    Log   ${cmd}   level=debug
    ${rc}   ${output}    Run and Return RC And Output   ${cmd}
    Log   ${output}   level=debug
    Should Not Be Equal As Integers   ${rc}   0   ${cmd} exited with 0 : ${output}   False
    RETURN   ${rc}  ${output}

Create Temporary File  [Arguments]  ${file}  ${content}=${EMPTY}
    ${path}    Normalize Path   ${TEMPDIR}/${file}
    File Should Not Exist   ${path}
    Create File   ${path}  ${content}

Remove Temporary File  [Arguments]  ${file}
    ${path}    Normalize Path   ${TEMPDIR}/${file}
    Remove File   ${path}
