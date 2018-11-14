#!/bin/bash
set -ex

pwd

sudo apt-get install -y wget
wget https://raw.githubusercontent.com/italiangrid/build-settings/master/maven/cnaf-mirror-settings.xml

mv cnaf-mirror-settings.xml ~/.m2/settings.xml
