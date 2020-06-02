package com.provys.dbsoapws.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurationSupport;

/**
 * Class replaces standard spring WsConfigurationSupport to use mapper that maps all soap calls
 * to SoapWsDbEndpoint#operation.
 */
@Configuration
@EnableWs
public class DbWsConfigurationSupport extends WsConfigurationSupport {

  /**
   * Creates default mapping bean that maps all requests to fixed endpoint.
   *
   * @return bean that maps all incoming requests to single endpoint
   */
  @Bean
  public DbEndpointMapping dbEndpointMapping() {
    DbEndpointMapping endpointMapping = new DbEndpointMapping();
    endpointMapping.setOrder(4);
    endpointMapping.setInterceptors(this.getInterceptors());
    return endpointMapping;
  }

  @Override
  public String toString() {
    return "DbWsConfigurationSupport{"
        + '}';
  }
}
