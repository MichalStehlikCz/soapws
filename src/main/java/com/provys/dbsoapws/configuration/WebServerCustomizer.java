package com.provys.dbsoapws.configuration;

import com.provys.common.exception.InternalException;
import com.provys.dbsoapws.model.ServiceDefinition;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.stereotype.Component;

@Component
public class WebServerCustomizer
    implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

  private final @Nullable Integer port;
  private final @Nullable String address;
  private final String servicePath;

  /**
   * Create web server customized that will configure web server based on our ServiceDefinition.
   *
   * @param serviceDefinition is service definition for our db soap webservice
   */
  @Autowired
  public WebServerCustomizer(ServiceDefinition serviceDefinition) {
    this.port = serviceDefinition.getPort();
    this.address = serviceDefinition.getAddress();
    this.servicePath = serviceDefinition.getServicePath();
  }

  @Override
  public void customize(ConfigurableServletWebServerFactory webServerFactory) {
    if (port != null) {
      webServerFactory.setPort(port);
    }
    if (address != null) {
      try {
        webServerFactory.setAddress(InetAddress.getByName(address));
      } catch (UnknownHostException e) {
        throw new InternalException("Failed to evaluate address " + address
            + "for listening interface", e);
      }
    }
    webServerFactory.setContextPath(servicePath);
  }

  @Override
  public String toString() {
    return "WebServerCustomizer{"
        + "port=" + port
        + ", address='" + address + '\''
        + ", servicePath='" + servicePath + '\''
        + '}';
  }
}