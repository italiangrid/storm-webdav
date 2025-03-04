# SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
#
# SPDX-License-Identifier: Apache-2.0

*** Settings ***

Resource   common/utils.robot
Resource   common/credentials.robot

*** Variables ***

${curl.opts.default}  --location --include --fail-with-body --no-progress-meter
${x509.trustdir}  /etc/grid-security/certificates

*** Keywords ***
Curl   [Arguments]  ${url}  ${opts}=${EMPTY}
    ${cmd}   Set Variable  curl ${url} ${curl.opts.default} ${opts}
    Log   ${cmd}   level=debug
    ${rc}   ${out}    Run and Return RC And Output   ${cmd}
    Log   ${out}   level=debug
    RETURN  ${rc}  ${out}

Curl Success  [Arguments]  ${url}  ${opts}=${EMPTY}
    ${rc}  ${out}   Execute and Check Success  curl ${url} ${curl.opts.default} ${opts}
    RETURN  ${rc}  ${out}

Curl Error   [Arguments]  ${url}  ${opts}=${EMPTY}
    ${rc}  ${out}   Execute and Check Failure  curl ${url} ${curl.opts.default} ${opts}
    RETURN  ${rc}  ${out}

Get Curl Voms Proxy Options
    ${eec}  ${eec_file}   Extract Eec From Proxy   %{X509_USER_PROXY}
    ${opts}   Set variable  --cert %{X509_USER_PROXY} --cacert ${eec_file} --capath ${x509.trustdir} 
    RETURN  ${opts}

Curl Voms HEAD Success  [Arguments]  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   --HEAD ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Success  ${url} ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms HEAD Failure  [Arguments]  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   --HEAD ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Error  ${url} ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms Get Success  [Arguments]  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X GET ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Success  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms Get Failure  [Arguments]  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X GET ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Error  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms MKCOL Success  [Arguments]  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X MKCOL ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Success  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms MKCOL Failure  [Arguments]  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X MKCOL ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Error  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms Pull COPY Success   [Arguments]  ${dest}  ${source}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X COPY -H "Source: ${source}" ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Success  ${dest}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms Pull COPY Failure   [Arguments]  ${dest}  ${source}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X COPY -H "Source: ${source}" ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Error  ${dest}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms Push COPY Success  [Arguments]  ${dest}  ${source}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X COPY -H "Destination: ${dest}" ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Success  ${source}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms Push COPY Failure  [Arguments]  ${dest}  ${source}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X COPY -H "Destination: ${dest}" ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Error  ${source}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms Push COPY  [Arguments]  ${dest}  ${source}  ${opts}=-S -l -i
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X COPY -H "Destination: ${dest}" ${opts} ${voms_opts}
    ${rc}  ${out}  Curl  ${source}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms PUT Success  [Arguments]  ${file}  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X PUT -T ${file} ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Success  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms PUT Failure  [Arguments]  ${file}  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X PUT -T ${file} ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Error  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms POST Success  [Arguments]  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X POST ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Success  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms POST Failure  [Arguments]  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X POST ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Error  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms DELETE Success  [Arguments]  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X DELETE ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Success  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms DELETE Failure  [Arguments]  ${url}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X DELETE ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Error  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms MOVE Success   [Arguments]  ${dest}  ${source}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X MOVE -H "Destination: ${dest}" ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Success  ${source}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms MOVE Failure   [Arguments]  ${dest}  ${source}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X MOVE -H "Destination: ${dest}" ${opts} ${voms_opts}
    ${rc}  ${out}  Curl Error  ${source}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms MOVE  [Arguments]  ${dest}  ${source}  ${opts}=-s -L -i
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X MOVE -H "Destination: ${dest}" ${opts} ${voms_opts}
    ${rc}  ${out}  Curl  ${source}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms OPTIONS  [Arguments]  ${url}  ${opts}=-s -L -i
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X OPTIONS ${voms_opts}
    ${rc}  ${out}  Curl  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl pull COPY Success  [Arguments]  ${dest}  ${source}  ${opts}=${curl.opts.default}
    ${all_opts}   Set variable   -X COPY -H "Source: ${source}" ${opts}
    ${rc}  ${out}  Curl Success  ${dest}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl push COPY Success  [Arguments]  ${dest}  ${source}  ${opts}=${curl.opts.default}
    ${all_opts}   Set variable   -X COPY -H "Destination: ${dest}" ${opts}
    ${rc}  ${out}  Curl Success  ${source}  ${all_opts}
    RETURN  ${rc}  ${out}

Curl Voms PROPFIND  [Arguments]  ${url}  ${body}  ${opts}=${curl.opts.default}
    ${voms_opts}  Get Curl Voms Proxy Options
    ${all_opts}   Set variable   -X PROPFIND ${opts} ${voms_opts} --data ${body}
    ${rc}  ${out}  Curl  ${url}  ${all_opts}
    RETURN  ${rc}  ${out}