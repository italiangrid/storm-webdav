#!/bin/bash

# SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare
#
# SPDX-License-Identifier: Apache-2.0

set -ex

REPORTS_DIR=${REPORTS_DIR:-reports}

ROBOT_ARGS=${ROBOT_ARGS:-}

DEFAULT_ARGS="--pythonpath .:common -d ${REPORTS_DIR}"

ARGS=${DEFAULT_ARGS}

if [ -n "${ROBOT_ARGS}" ]; then
  ARGS="${ARGS} ${ROBOT_ARGS}"
fi

robot ${ARGS} test 
