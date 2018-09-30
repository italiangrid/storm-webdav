*** Settings ***
Library    OperatingSystem

*** Variables ***

${sa.1}   test.vo
${sa.2}   noauth
${sa.3}   auth
${sa.4}   oauth

${sa.default}   ${sa.1}
${sa.noauth}    ${sa.2}
${sa.auth}      ${sa.3}
${sa.oauth}     ${sa.4}

${storage.root}   /storage

*** Keywords ***
Create Test File  [Arguments]  ${file}  ${content}=${EMPTY}   ${sa}=${sa.default}
    ${path}=   Normalize Path   ${storage.root}/${sa}/${file}
    File Should Not Exist   ${path}
    Create File   ${path}  ${content}

Remove Test File  [Arguments]  ${file}  ${sa}=${sa.default}
    ${path}=   Normalize Path   ${storage.root}/${sa}/${file}
    Remove file  ${path}

Remove Test Directory  [Arguments]  ${file}  ${sa}=${sa.default}
    ${path}=   Normalize Path   ${storage.root}/${sa}/${file}
    Remove Directory  ${path}