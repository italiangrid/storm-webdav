package org.italiangrid.storm.webdav.spring.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import org.italiangrid.storm.webdav.authz.VOMSAttributeCertificateAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationProvider;
import org.italiangrid.storm.webdav.authz.VOMSPreAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSVOAuthority;
import org.italiangrid.storm.webdav.authz.util.ReadonlyHTTPMethodMatcher;
import org.italiangrid.storm.webdav.authz.vomsmap.VOMSMapAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.vomsmap.VOMSMapDetailsService;
import org.italiangrid.storm.webdav.config.Constants;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.italiangrid.storm.webdav.spring.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@Import(AppConfig.class)
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	ServletContext context;
	
	@Autowired
	ServiceConfiguration serviceConfiguration;
	
	@Autowired
	VOMSMapDetailsService vomsMapDetailsService;

	private String getROVOAccessRule(Collection<String> vos) {

		StringBuilder accessRule = new StringBuilder();

		boolean first = true;
		accessRule.append("hasAnyRole(");

		for (String vo : vos) {

			if (!first) {
				accessRule.append(",");
			}

			VOMSVOAuthority voAuthority = new VOMSVOAuthority(vo);

			accessRule.append(String.format("'%s'", voAuthority));

			first = false;
		}

		accessRule.append(")");

		return accessRule.toString();
	}

	protected VOMSAuthenticationFilter buildVOMSAuthenticationFilter(
		VOMSAuthenticationProvider provider) {
		
		List<VOMSAuthDetailsSource> vomsHelpers = new ArrayList<VOMSAuthDetailsSource>();

		vomsHelpers.add(new VOMSAttributeCertificateAuthDetailsSource());
		
		if (serviceConfiguration.enableVOMSMapFiles() && vomsMapDetailsService != null) {

			vomsHelpers.add(new VOMSMapAuthDetailsSource(vomsMapDetailsService));

		}

		VOMSAuthenticationFilter filter = new VOMSAuthenticationFilter(provider);
		filter.setAuthenticationDetailsSource(new VOMSPreAuthDetailsSource(
			vomsHelpers));
		return filter;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		VOMSAuthenticationProvider prov = new VOMSAuthenticationProvider();

		http.csrf().disable();

		http.authenticationProvider(prov).addFilter(
			buildVOMSAuthenticationFilter(prov));

		StorageAreaInfo sa = (StorageAreaInfo) context
			.getAttribute(Constants.SA_CONF_KEY);

		if (sa.anonymousReadEnabled() && sa.authenticatedReadEnabled()) {

			String accessRule = String
				.format("isAnonymous() or isAuthenticated() or %s",
					getROVOAccessRule(sa.vos()));

			http.authorizeRequests().requestMatchers(new ReadonlyHTTPMethodMatcher())
				.access(accessRule);

		} else if (sa.authenticatedReadEnabled()) {

			String accessRule = String.format("isAuthenticated() or %s",
				getROVOAccessRule(sa.vos()));

			http.authorizeRequests().requestMatchers(new ReadonlyHTTPMethodMatcher())
				.access(accessRule);

		}

		for (String vo : sa.vos()) {

			VOMSVOAuthority voAuthority = new VOMSVOAuthority(vo);

			http.authorizeRequests().antMatchers("**")
				.hasAuthority(voAuthority.getAuthority());
		}

	}

}
