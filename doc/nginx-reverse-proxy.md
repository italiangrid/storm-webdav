# Use nginx as a reverse proxy

It is possible to deploy StoRM WebDAV using nginx as a reverse proxy.

The main pros of this type of deployment are:

- use nginx to manage TLS termination
- delegate VOMS proxy authentication to [ngx_http_voms_module](https://baltig.infn.it/cnafsd/ngx_http_voms_module)
- improve performance of downloads by using nginx to handle GET requests

## How to deploy StoRM WebDAV using nginx

Install nginx and [ngx_http_voms_module](https://baltig.infn.it/cnafsd/ngx_http_voms_module) on your server.

Change the configuration of nginx to:

- enable the client certificates
- set the correct headers for VOMS authentication
- add an internal endpoint to which to redirect GET requests

In your `application.yml` configuration set `storm.nginx-reverse-proxy` to `true`.

Example nginx configuration:

```
server {
	location /internal-get {
		internal;
		alias /;
		sendfile on;
		tcp_nopush on;
		keepalive_timeout 65;
		tcp_nodelay on;
	}
	location / {
		proxy_pass http://127.0.0.1:8086;
		proxy_set_header X-VOMS-voms_user $voms_user;
		proxy_set_header X-VOMS-ssl_client_ee_s_dn $ssl_client_ee_s_dn;
		proxy_set_header X-VOMS-voms_user_ca $voms_user_ca;
		proxy_set_header X-VOMS-ssl_client_ee_i_dn $ssl_client_ee_i_dn;
		proxy_set_header X-VOMS-voms_fqans $voms_fqans;
		proxy_set_header X-VOMS-voms_server $voms_server;
		proxy_set_header X-VOMS-voms_server_ca $voms_server_ca;
		proxy_set_header X-VOMS-voms_vo $voms_vo;
		proxy_set_header X-VOMS-voms_server_uri $voms_server_uri;
		proxy_set_header X-VOMS-voms_not_before $voms_not_before;
		proxy_set_header X-VOMS-voms_not_after $voms_not_after;
		proxy_set_header X-VOMS-voms_generic_attributes $voms_generic_attributes;
		proxy_set_header X-VOMS-voms_serial $voms_serial;
	}
	listen [::]:8443 ssl http2;
	listen 8443 ssl http2;
	ssl_certificate /etc/grid-security/hostcert.pem;
	ssl_certificate_key /etc/grid-security/hostkey.pem;
	ssl_client_certificate /etc/pki/ca-trust/extracted/pem/tls-ca-bundle-all.pem;
	ssl_verify_client optional;
	ssl_verify_depth 10;
	client_max_body_size 0;
	error_page 497 https://$host:8443$request_uri;
}
```
