#!/bin/bash
set -ex

sudo /scripts/init-certs.sh
sudo /scripts/init-sa-config.sh
sudo /scripts/init-storage.sh
