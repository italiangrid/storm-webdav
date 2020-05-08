#!/bin/bash

set -ex

source /etc/sysconfig/storm-webdav

update-ca-trust

java ${STORM_WEBDAV_JVM_OPTS} -jar ${STORM_WEBDAV_JAR}
