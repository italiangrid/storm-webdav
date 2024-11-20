/**
 * Copyright (c) Istituto Nazionale di Fisica Nucleare, 2014-2023.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.italiangrid.storm.webdav.server.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Base64;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Add 'Server' header response, using StoRM-WebDAV version (taken from the
 * .jar file) and an instance ID that is (with high likelihood) different
 * between any two invocations of StoRM-WebDAV instances.
 */
public class ServerResponseHeaderFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(ServerResponseHeaderFilter.class);

    private static final int INSTANCE_ID_LENGTH = 4;

    private static final String SERVER_HEADER_VALUE = "StoRM-WebDAV/" + lookupStormVersion()
            + " (instance=" + calculateInstanceIdentifier() + ")";

    private static String lookupStormVersion() {
        ProtectionDomain pd = ServerResponseHeaderFilter.class.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        URL u = cs.getLocation();

        Optional<String> maybeVersion = Optional.empty();
        try (InputStream is = u.openStream(); JarInputStream jis = new JarInputStream(is)) {
            Manifest m = jis.getManifest();
            if (m != null) {
                Attributes attributes = m.getMainAttributes();
                String value = attributes.getValue("Implementation-Version");
                maybeVersion = Optional.ofNullable(value);
            }
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
        }
        return maybeVersion.orElse("unknown-version");
    }

    private static String calculateInstanceIdentifier() {
        byte[] in = ByteBuffer.allocate(Long.BYTES)
                .putLong(System.currentTimeMillis())
                .array();
        byte[] encodedBytes = Base64.getEncoder().withoutPadding().encode(in);
        String encoded = new String(encodedBytes);
        return encoded.substring(encoded.length() - INSTANCE_ID_LENGTH);
    }

    private void specifyServerHeader(HttpServletResponse response) {
        response.setHeader("Server", SERVER_HEADER_VALUE);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        if (response instanceof HttpServletResponse httpServletResponse) {
            // Set the header now, in case the response is committed before
            // chain.doFilter returns.
            specifyServerHeader(httpServletResponse);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            if (response instanceof HttpServletResponse httpServletResponse) {
                // Set the header now, in case the response is not yet
                // committed.  This allows the code to override any
                // {@literal Server} header value specified earlier.
                specifyServerHeader(httpServletResponse);
            }
        }
    }
}
