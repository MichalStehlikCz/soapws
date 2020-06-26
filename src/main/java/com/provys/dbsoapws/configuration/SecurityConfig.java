package com.provys.dbsoapws.configuration;

import com.provys.auth.api.AuthProviderLookup;
import com.provys.dbsoapws.model.ServiceDefinition;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final ServiceDefinition serviceDefinition;
  private final AuthProviderLookup authProviderLookup;

  @Autowired
  SecurityConfig(ServiceDefinition serviceDefinition, AuthProviderLookup authProviderLookup) {
    this.serviceDefinition = Objects.requireNonNull(serviceDefinition);
    this.authProviderLookup = Objects.requireNonNull(authProviderLookup);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and().csrf().disable()
        .authorizeRequests()
        .antMatchers("/**/*.wsdl").permitAll()
        .antMatchers("/**/*.xsd").permitAll()
        .anyRequest().hasRole("USER")
        .and().addFilterBefore(new DbAuthenticationFilter(serviceDefinition, authProviderLookup),
        UsernamePasswordAuthenticationFilter.class);
    // we need to set authentication entry point to basic auth in case some endpoints use it
    var basicAuthenticationEntryPoint = new BasicAuthenticationEntryPoint();
    basicAuthenticationEntryPoint.setRealmName("Realm");
    http.exceptionHandling().authenticationEntryPoint(basicAuthenticationEntryPoint);
  }

  @Override
  public String toString() {
    return "SecurityConfig{"
        + "serviceDefinition=" + serviceDefinition
        + ", authProviderLookup=" + authProviderLookup + '}';
  }
}