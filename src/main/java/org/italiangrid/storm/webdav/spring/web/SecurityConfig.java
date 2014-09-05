package org.italiangrid.storm.webdav.spring.web;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContext;

import org.italiangrid.storm.webdav.authz.DefaultVOMSAttributesExtractor;
import org.italiangrid.storm.webdav.authz.VOMSAttributesExtractor;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationFilter;
import org.italiangrid.storm.webdav.authz.VOMSAuthenticationProvider;
import org.italiangrid.storm.webdav.authz.VOMSPreAuthDetailsSource;
import org.italiangrid.storm.webdav.authz.VOMSVOGrantedAuthority;
import org.italiangrid.storm.webdav.authz.util.ReadonlyHTTPMethodMatcher;
import org.italiangrid.storm.webdav.authz.vomsmap.VOMSMapAttributesExtractor;
import org.italiangrid.storm.webdav.authz.vomsmap.VOMSMapDetailsService;
import org.italiangrid.storm.webdav.config.Constants;
import org.italiangrid.storm.webdav.config.ServiceConfiguration;
import org.italiangrid.storm.webdav.config.StorageAreaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	ServletContext context;

	private String getROVOAccessRule(Collection<String> vos) {

		StringBuilder accessRule = new StringBuilder();

		boolean first = true;
		accessRule.append("hasAnyRole(");

		for (String vo : vos) {

			if (!first) {
				accessRule.append(",");
			}

			VOMSVOGrantedAuthority voAuthority = new VOMSVOGrantedAuthority(vo);

			accessRule.append(String.format("'%s'", voAuthority));

			first = false;
		}

		accessRule.append(")");

		return accessRule.toString();
	}

	protected VOMSAuthenticationFilter buildVOMSAuthenticationFilter(
		VOMSAuthenticationProvider provider) {

		List<VOMSAttributesExtractor> vomsHelpers = new ArrayList<VOMSAttributesExtractor>();

		vomsHelpers.add(new DefaultVOMSAttributesExtractor());
		ServiceConfiguration configuration = (ServiceConfiguration) context
			.getAttribute(Constants.SERVICE_CONF_KEY);

		if (configuration.enableVOMSMapFiles()) {

			VOMSMapDetailsService vmds = (VOMSMapDetailsService) context
				.getAttribute(Constants.VOMS_MAP_DS_KEY);
			vomsHelpers.add(new VOMSMapAttributesExtractor(vmds));

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

			VOMSVOGrantedAuthority voAuthority = new VOMSVOGrantedAuthority(vo);

			http.authorizeRequests().antMatchers("**")
				.hasAuthority(voAuthority.getAuthority());
		}

	}

}
