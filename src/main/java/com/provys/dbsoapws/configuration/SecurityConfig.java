package com.provys.dbsoapws.configuration;

import com.provys.auth.api.AuthProviderLookup;
import com.provys.auth.oracle.OracleAuthProvider;
import com.provys.dbsoapws.model.ServiceDefinition;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final ServiceDefinition serviceDefinition;
  private final AuthProviderLookup authProviderLookup;

  private final OracleAuthProvider authProvider;

  @Autowired
  SecurityConfig(ServiceDefinition serviceDefinition, AuthProviderLookup authProviderLookup,
      OracleAuthProvider authProvider) {
    this.serviceDefinition = Objects.requireNonNull(serviceDefinition);
    this.authProviderLookup = Objects.requireNonNull(authProviderLookup);
    this.authProvider = authProvider;
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
//        .and().httpBasic()
        .and().addFilterBefore(new DbAuthenticationFilter(serviceDefinition, authProviderLookup),
        UsernamePasswordAuthenticationFilter.class)
    ;
  }

  @Override
  @Autowired
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(authProvider);
  }

  @Override
  public String toString() {
    return "SecurityConfig{"
        + "serviceDefinition=" + serviceDefinition
        + ", authProviderLookup=" + authProviderLookup
        + ", authProvider=" + authProvider + '}';
  }
}