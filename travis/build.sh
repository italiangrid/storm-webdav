#!/bin/bash
set -e

mvn -B clean compile
echo "storm-webdav build completed succesfully"

mvn -B clean test
echo "storm-webdav tests completed succesfully"
