#!/bin/bash

set -e

if [ ! -e "openssl.conf" ]; then
  >&2 echo "The configuration file 'openssl.conf' doesn't exist in this directory"
  exit 1
fi

certs_dir=/certs
ta_dir=/trust-anchors
ca_bundle_prefix=/etc/pki
vomsdir=/vomsdir

rm -rf "${certs_dir}"
mkdir -p "${certs_dir}"
rm -rf "${ta_dir}"
mkdir -p "${ta_dir}"
rm -rf "${vomsdir}"
mkdir -p "${vomsdir}"

export CA_NAME=igi_test_ca
export X509_CERT_DIR="${ta_dir}"

make_ca.sh
make_crl.sh
install_ca.sh igi_test_ca "${ta_dir}"

ca_bundle="${ca_bundle_prefix}"/tls/certs
cat "${ta_dir}"/igi_test_ca.pem >> "${ca_bundle}"/ca-bundle.crt

make_cert.sh test0
cp igi_test_ca/certs/test0.* "${certs_dir}"

make_cert.sh storm_test_example
cp igi_test_ca/certs/storm_test_example.* "${certs_dir}"
chown 1000:1000 "${certs_dir}"/storm_test_example.*

make_cert.sh storm-alias_test_example
cp igi_test_ca/certs/storm-alias_test_example.* "${certs_dir}"
chown 1000:1000 "${certs_dir}"/storm-alias_test_example.*

make_cert.sh voms_test_example
cp igi_test_ca/certs/voms_test_example.* "${certs_dir}"
mkdir -p "${vomsdir}"/test.vo
openssl x509 -in "${certs_dir}"/voms_test_example.cert.pem -noout -subject -issuer -nameopt compat \
  | sed -e 's/subject=//' -e 's/issuer=//' > "${vomsdir}"/test.vo/voms.test.example.lsc

proxy_name=x509up_u1000
echo pass | voms-proxy-fake --debug -conf proxies.d/${proxy_name}.conf -out "${certs_dir}"/${proxy_name} --pwstdin
chmod 600 "${certs_dir}"/${proxy_name}
