*** Settings ***
Library    OperatingSystem

Resource   common/utils.robot

*** Variables ***

${sa.default}   test.vo
${sa.auth}      auth
${sa.noauth}    noauth
${sa.fga}       fga
${sa.oauth}     oauth-authz
${sa.wlcg}      wlcg

${storage.root}   /storage

*** Keywords ***
Create Test File  [Arguments]  ${file}  ${content}=${EMPTY}   ${sa}=${sa.default}
    ${path}=   Normalize Path   ${storage.root}/${sa}/${file}
    File Should Not Exist   ${path}
    Create File   ${path}  ${content}

Create Test File With Size  [Arguments]  ${file}  ${size}  ${sa}=${sa.default}
    ${path}=   Normalize Path   ${storage.root}/${sa}/${file}
    File Should Not Exist   ${path}
    ${rc}  ${out}  Execute and Check Success  dd if=/dev/zero of=${path} bs=1 count=0 seek=${size}

Create 1MB Test File  [Arguments]  ${file}  ${sa}=${sa.default}
    ${path}=   Normalize Path   ${storage.root}/${sa}/${file}
    File Should Not Exist   ${path}
    ${rc}  ${out}  Execute and Check Success  dd if=/dev/zero of=${path} bs=1 count=0 seek=1048576
    
Remove Test File  [Arguments]  ${file}  ${sa}=${sa.default}
    ${path}=   Normalize Path   ${storage.root}/${sa}/${file}
    Remove file  ${path}

Remove Test Directory  [Arguments]  ${file}  ${sa}=${sa.default}
    ${path}=   Normalize Path   ${storage.root}/${sa}/${file}
    Remove Directory  ${path}