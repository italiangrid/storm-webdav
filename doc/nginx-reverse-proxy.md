<!--
SPDX-FileCopyrightText: 2014 Istituto Nazionale di Fisica Nucleare

SPDX-License-Identifier: Apache-2.0
-->

# Use NGINX as a reverse proxy

It is possible to deploy StoRM WebDAV using NGINX as a reverse proxy.

The main pros of this type of deployment are:

- use NGINX to manage TLS termination
- delegate VOMS proxy authentication to [ngx_http_voms_module](https://baltig.infn.it/cnafsd/ngx_http_voms_module)
- improve performance of downloads by using NGINX to handle GET requests

## How to deploy StoRM WebDAV using NGINX

Install NGINX and [ngx_http_voms_module](https://baltig.infn.it/cnafsd/ngx_http_voms_module) on your server.

Change the configuration of NGINX to:

- enable client certificate authentication
- set the correct headers for VOMS authentication
- add an internal endpoint to which to redirect GET requests

In your `application.yml` configuration set `storm.nginx.enabled` to `true`.

Example NGINX configuration:

```
upstream storm-webdav {
    keepalive 2;
    # Substitute 8086 with the storm.connector.port in your application.yml configuration
    server 127.0.0.1:8086;
}

server {
	location /.storm-webdav/internal/get {
		internal;
		alias /;
		sendfile on;
		tcp_nopush on;
		keepalive_timeout 65;
		tcp_nodelay on;
		# Needed to send end UDP firefly with flowd to support SciTags
		if ($upstream_http_x_scitag_actid) {
			# If the tranfer used SciTags, write the correct string to the flowd pipe to send the end UDP firefly
			# StoRM WebDAV writes the start string, but it's unaware of when the transfer is completed 
			# Leverage the NGINX access_log written at the end of the request to do this
			access_log /var/run/flowd flowd;
		}
		add_header Server $upstream_http_server;
	}
	location / {
		proxy_pass http://storm-webdav;
		proxy_pass_header Server;
		proxy_http_version 1.1;
		proxy_set_header Connection "";
		proxy_set_header Forwarded "$proxy_forwarded_by;$proxy_forwarded_for;host=$http_host;proto=$scheme";
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
	listen [::]:8443 ssl;
	listen 8443 ssl;
	http2 on;
	ssl_certificate /etc/grid-security/hostcert.pem;
	ssl_certificate_key /etc/grid-security/hostkey.pem;
	# `tls-ca-bundle-all.pem` is a bundle of all the system and IGTF CA certificates, including those used for client authentication. For more information see https://github.com/indigo-iam/egi-trust-anchors-container
	ssl_client_certificate /etc/pki/ca-trust/extracted/pem/tls-ca-bundle-all.pem;
	ssl_verify_client optional;
	ssl_verify_depth 10;
	client_max_body_size 0;
	# https://nginx.org/en/docs/http/ngx_http_ssl_module.html#errors
	# 497 a regular request has been sent to the HTTPS port
	error_page 497 https://$host:8443$request_uri;
}
```

Also add this to the NGINX configuration:

```
http {
	# https://github.com/nginxinc/nginx-wiki/blob/master/source/start/topics/examples/forwarded.rst
	map $remote_addr $proxy_forwarded_for {
		# IPv4 addresses can be sent as-is
		~^[0-9.]+$          "for=\"$remote_addr:$remote_port\"";

		# IPv6 addresses need to be bracketed and quoted
		~^[0-9A-Fa-f:.]+$   "for=\"[$remote_addr]:$remote_port\"";

		# Unix domain socket names cannot be represented in RFC 7239 syntax
		default             "for=unknown";
	}
	map $server_addr $proxy_forwarded_by {
		# IPv4 addresses can be sent as-is
		~^[0-9.]+$          "by=\"$server_addr:$server_port\"";

		# IPv6 addresses need to be bracketed and quoted
		~^[0-9A-Fa-f:.]+$   "by=\"[$server_addr]:$server_port\"";

		# Unix domain socket names cannot be represented in RFC 7239 syntax
		default             "host=unknown";
	}
	# Needed to send end UDP firefly with flowd to support SciTags
	log_format flowd 'end tcp $server_addr $server_port $remote_addr $remote_port $upstream_http_x_scitag_actid $upstream_http_x_scitag_expid';
}
```

If using SELinux, check the `httpd_can_network_connect` (or `httpd_can_network_relay`) option.
To learn more, read [this blog post](https://www.f5.com/company/blog/nginx/using-nginx-plus-with-selinux).
