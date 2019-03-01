#!/bin/bash
#
# Copyright (c) Istituto Nazionale di Fisica Nucleare, 2018.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
set -ex

DAV_HOST=${DAV_HOST:-localhost}

REMOTE_DAV_HOST=${REMOTE_DAV_HOST:-${DAV_HOST:-localhost}}

REPORTS_DIR=${REPORTS_DIR:-reports}

ROBOT_ARGS=${ROBOT_ARGS:-}

DEFAULT_ARGS="--pythonpath .:common --variable dav.host:${DAV_HOST} --variable remote.dav.host:${REMOTE_DAV_HOST} -d ${REPORTS_DIR}"

ARGS=${DEFAULT_ARGS}

if [ -n "${ROBOT_ARGS}" ]; then
  ARGS="${ARGS} ${ROBOT_ARGS}"
fi

robot ${ARGS} test 
