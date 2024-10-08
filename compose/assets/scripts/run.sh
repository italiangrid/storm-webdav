#!/bin/bash
set -ex

/scripts/init-storage.sh
/scripts/unpack-tarball.sh
/scripts/run-service.sh
