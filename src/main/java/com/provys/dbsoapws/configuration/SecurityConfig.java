package com.provys.dbsoapws.configuration;

import com.provys.auth.oracle.ProvysOracleAuthProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

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
    http
        .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
        .csrf().disable()
        .authorizeRequests()
        .antMatchers("/**/*.wsdl").permitAll()
        .antMatchers("/**/*?wsdl").permitAll()
        .antMatchers("/**/*.xsd").permitAll()
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