#!/bin/bash
set -ex

sudo cp /code/target/storm-webdav-server.jar /usr/share/java/storm-webdav/storm-webdav-server.jar

sudo /scripts/run-service.sh
