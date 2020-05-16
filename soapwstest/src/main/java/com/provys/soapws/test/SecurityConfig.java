package com.provys.soapws.test;

import com.provys.auth.oracle.ProvysOracleAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

  private final ProvysOracleAuthProvider authProvider;

  @Autowired
  public SecurityConfig(ProvysOracleAuthProvider authProvider) {
    this.authProvider = authProvider;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.csrf().disable()
        .authorizeRequests()
        .antMatchers("/**/*.wsdl").permitAll()
        .anyRequest().hasRole("USER")
        .and().httpBasic();
  }

  @Autowired
  public void configureGlobal(AuthenticationManagerBuilder auth) {
    auth
        .authenticationProvider(authProvider);
  }

  @Override
  public String toString() {
    return "SecurityConfig{"
        + "authProvider=" + authProvider + '}';
  }
}