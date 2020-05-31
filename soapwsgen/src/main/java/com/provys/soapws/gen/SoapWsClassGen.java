package com.provys.soapws.gen;

import com.provys.auth.oracle.ProvysOracleAuthProvider;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.Objects;
import javax.lang.model.element.Modifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

public class SoapWsClassGen {

  private final String packageName;
  private final String moduleName;

  public SoapWsClassGen(String packageName, String moduleName) {
    this.packageName = packageName;
    this.moduleName = moduleName;
  }

  public JavaFile genSecurityConfig() {
    return JavaFile
        .builder(packageName,
            TypeSpec.classBuilder("SecurityConfig")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Configuration.class)
                .addAnnotation(EnableWebSecurity.class)
                .superclass(WebSecurityConfigurerAdapter.class)
                .addField(ProvysOracleAuthProvider.class, "authProvider", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Autowired.class)
                    .addParameter(ProvysOracleAuthProvider.class, "authProvider")
                    .addStatement("this.authProvider = $T.requireNonNull(authProvider)", Objects.class)
                    .build())
                .addMethod(MethodSpec.methodBuilder("configure")
                    .addModifiers(Modifier.PROTECTED)
                    .addAnnotation(Override.class)
                    .addParameter(HttpSecurity.class, "http")
                    .addException(Exception.class)
                    .addStatement(CodeBlock.builder()
                        .add("http")
                        .add("    .sessionManagement().sessionCreationPolicy($T.STATELESS)", SessionCreationPolicy.class)
                        .add("    .and().csrf().disable()")
                        .add("    .authorizeRequests()")
                        .add("    .antMatchers(\"/**/*.wsdl\").permitAll()")
                        .add("    .anyRequest().hasRole(\"USER\")")
                        .add("    .and().httpBasic()")
                        .build())
                    .build())
                .addMethod(MethodSpec.methodBuilder("configureGlobal")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Autowired.class)
                    .addParameter(AuthenticationManagerBuilder.class, "auth")
                    .addStatement(CodeBlock.builder()
                        .add("auth")
                        .add("    .authenticationProvider(authProvider)")
                        .build())
                    .build())
                .addMethod(MethodSpec.methodBuilder("toString")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(String.class)
                    .addStatement(CodeBlock.builder()
                        .add("return \"SecurityConfig{\"")
                        .add("    + \"authProvider=\" + authProvider + '}'")
                        .build())
                    .build())
                .build()
        ).build();
  }
}
