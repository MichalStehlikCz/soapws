package com.provys.dbsoapws.model;

import com.provys.common.exception.InternalException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Definition of services to be exposed.
 */
public class ServiceDefinition {

  private final @Nullable Integer port;
  private final @Nullable String address;
  private final String servicePath;
  private final Map<String, EndpointDefinition> endpointsByNamespace;

  /**
   * Create service definition based on supplied parameters.
   *
   * @param port is port on which service should be available; null means service.port config will
   *            be used
   * @param address is network adapter on which service should be available; null means all
   * @param servicePath is path under which services will be available; path of each service is
   *                   appended to it
   * @param endpoints is collections of endpoints that should be exposed
   */
  public ServiceDefinition(@Nullable Integer port, @Nullable String address, String servicePath,
      Collection<EndpointDefinition> endpoints) {
    this.port = port;
    this.address = address;
    if (servicePath.isBlank()) {
      this.servicePath = "/";
    } else if (servicePath.charAt(0) == '/') {
      this.servicePath = servicePath;
    } else {
      this.servicePath = '/' + servicePath;
    }
    this.endpointsByNamespace = endpoints.stream()
        .collect(
            Collectors.toUnmodifiableMap(endpoint -> endpoint.getXsdSchema().getTargetNamespace(),
                Function.identity()));
  }

  /**
   * Value of field port.
   *
   * @return value of field port
   */
  public @Nullable Integer getPort() {
    return port;
  }

  /**
   * Value of field address.
   *
   * @return value of field address
   */
  public @Nullable String getAddress() {
    return address;
  }

  /**
   * Value of field servicePath.
   *
   * @return value of field servicePath
   */
  public String getServicePath() {
    return servicePath;
  }

  /**
   * Retrieve end point description for specified namespace.
   *
   * @param namespace is namespace of request
   * @return endpoint definition corresponding to specified namespace
   */
  public EndpointDefinition getForNamespace(String namespace) {
    var result = endpointsByNamespace.get(namespace);
    if (result == null) {
      throw new InternalException("Endpoint for namespace " + namespace + " not found");
    }
    return result;
  }

  /**
   * Collection of configured endpoints.
   *
   * @return collection of endpoints service consists of
   */
  public Collection<EndpointDefinition> getEndpoints() {
    return endpointsByNamespace.values();
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ServiceDefinition that = (ServiceDefinition) o;
    return Objects.equals(port, that.port)
        && Objects.equals(address, that.address)
        && servicePath.equals(that.servicePath)
        && endpointsByNamespace.equals(that.endpointsByNamespace);
  }

  @Override
  public int hashCode() {
    int result = port != null ? port.hashCode() : 0;
    result = 31 * result + (address != null ? address.hashCode() : 0);
    result = 31 * result + servicePath.hashCode();
    result = 31 * result + endpointsByNamespace.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "ServiceDefinition{"
        + "port=" + port
        + ", address='" + address + '\''
        + ", servicePath='" + servicePath + '\''
        + ", endpointsByNamespace=" + endpointsByNamespace
        + '}';
  }
}
