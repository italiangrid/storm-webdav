package org.italiangrid.storm.webdav.server;

import io.milton.http.HttpManager;
import io.milton.http.Request;
import io.milton.http.Response;
import io.milton.servlet.MiltonServlet;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.italiangrid.storm.webdav.fs.FilesystemAccess;
import org.italiangrid.storm.webdav.fs.attrs.ExtendedAttributesHelper;
import org.italiangrid.storm.webdav.milton.StoRMHTTPManagerBuilder;
import org.italiangrid.storm.webdav.milton.StoRMResourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MiltonFilter implements Filter {

	public static final Logger LOG = LoggerFactory.getLogger(MiltonFilter.class);

	static final Set<String> WEBDAV_METHOD_SET = new HashSet<String>();
	static final String SA_ROOT_PATH = "sa-root";

	static {
		for (WebDAVMethod m : WebDAVMethod.values()) {
			WEBDAV_METHOD_SET.add(m.name());
		}
	}

	private HttpManager miltonHTTPManager;

	private ServletContext servletContext;
	
	private FilesystemAccess filesystemAccess;
	
	private ExtendedAttributesHelper attrsHelper;

	
	@Autowired
	public MiltonFilter(FilesystemAccess fsAccess, ExtendedAttributesHelper attrsHelper) {
		this.filesystemAccess = fsAccess;
		this.attrsHelper = attrsHelper;
	}
	
	private void initMiltonHTTPManager(ServletContext context) {

		final StoRMHTTPManagerBuilder builder = new StoRMHTTPManagerBuilder();

		final StoRMResourceFactory resourceFactory = new StoRMResourceFactory(
			filesystemAccess,
			attrsHelper,
			servletContext.getInitParameter(SA_ROOT_PATH),
			servletContext.getContextPath());

		builder.setResourceFactory(resourceFactory);

		miltonHTTPManager = builder.buildHttpManager();

	}

	@Override
	public void init(FilterConfig config) throws ServletException {

		servletContext = config.getServletContext();
		initMiltonHTTPManager(servletContext);

	}

	private boolean isWebDAVMethod(ServletRequest request) {

		return WEBDAV_METHOD_SET.contains(((HttpServletRequest) request)
			.getMethod());
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
		FilterChain chain) throws IOException, ServletException {

		if (isWebDAVMethod(request)) {
			doMilton((HttpServletRequest) request, (HttpServletResponse) response);
		} else
			chain.doFilter(request, response);

	}

	public void doMilton(HttpServletRequest request, HttpServletResponse response) {

		LOG.trace("doMilton: req: {}, res: {}", request, response);

		try {
			// Is this really needed?
			MiltonServlet.setThreadlocals((HttpServletRequest) request,
				(HttpServletResponse) response);

			Request miltonReq = new io.milton.servlet.ServletRequest(request,
				servletContext);

			Response miltonRes = new io.milton.servlet.ServletResponse(response);
			miltonHTTPManager.process(miltonReq, miltonRes);

		} finally {

			MiltonServlet.clearThreadlocals();

			try {

				response.getOutputStream().flush();
				response.flushBuffer();

			} catch (IOException e) {
				LOG.error(e.getMessage(), e);
				throw new RuntimeException(e.getMessage(), e);
			}

		}

	}

	@Override
	public void destroy() {

	}
	
}
