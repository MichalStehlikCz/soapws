package com.provys.dbsoapws.configuration;

import com.provys.dbsoapws.model.ServiceDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;

@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

  /**
   * Registers our special message dispatcher servlet.
   *
   * @param applicationContext is Spring application context
   * @param serviceDefinition is service definition, loaded from database
   * @return registration bean for our message dispatcher servlet
   */
  @Bean
  @Autowired
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
      ApplicationContext applicationContext, ServiceDefinition serviceDefinition) {
    MessageDispatcherServlet servlet = new DbMessageDispatcherServlet(serviceDefinition);
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet,
        serviceDefinition.getServicePath() + "/*");
  }

  /**
   * Registers filter for ?wsdl translation filter.
   *
   * @return filter registrar for ?wsdl translation filter.
   */
  @Bean
  public FilterRegistrationBean<WsdlQueryCompatibilityFilter> registerRequestLogFilter() {
    var reg = new FilterRegistrationBean<>(new WsdlQueryCompatibilityFilter());
    reg.setOrder(-500);
    return reg;
  }
}
