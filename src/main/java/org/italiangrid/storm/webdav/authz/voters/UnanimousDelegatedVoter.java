package org.italiangrid.storm.webdav.authz.voters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;

public class UnanimousDelegatedVoter implements AccessDecisionVoter<FilterInvocation> {

  public static final Logger LOG = LoggerFactory.getLogger(UnanimousDelegatedVoter.class);

  private final List<AccessDecisionVoter<FilterInvocation>> voters;

  private final String name;

  private UnanimousDelegatedVoter(String name, List<AccessDecisionVoter<FilterInvocation>> voters) {
    this.name = name;
    this.voters = voters;
  }

  @Override
  public boolean supports(ConfigAttribute attribute) {
    return false;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return FilterInvocation.class.isAssignableFrom(clazz);
  }

  @Override
  public int vote(Authentication authentication, FilterInvocation filter,
      Collection<ConfigAttribute> attributes) {

    int grant = 0;

    List<ConfigAttribute> singleAttributeList = new ArrayList<>(1);
    singleAttributeList.add(null);

    for (ConfigAttribute attribute : attributes) {
      singleAttributeList.set(0, attribute);

      for (AccessDecisionVoter<FilterInvocation> voter : voters) {
        int result = voter.vote(authentication, filter, singleAttributeList);

        if (LOG.isDebugEnabled()) {
          LOG.debug("Voter: {}, returned: {}", voter, result);
        }

        switch (result) {
          case AccessDecisionVoter.ACCESS_GRANTED:
            grant++;

            break;

          case AccessDecisionVoter.ACCESS_DENIED:
            return AccessDecisionVoter.ACCESS_DENIED;

          default:
            break;
        }
      }
    }

    // To get this far, there were no deny votes
    if (grant > 0) {
      return AccessDecisionVoter.ACCESS_GRANTED;
    }


    return AccessDecisionVoter.ACCESS_ABSTAIN;
  }

  public static UnanimousDelegatedVoter forVoters(String name,
      List<AccessDecisionVoter<FilterInvocation>> accessDecisionVoters) {
    return new UnanimousDelegatedVoter(name, accessDecisionVoters);
  }

  @Override
  public String toString() {
    return name;
  }
}
