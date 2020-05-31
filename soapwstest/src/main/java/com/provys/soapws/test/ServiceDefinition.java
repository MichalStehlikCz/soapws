package com.provys.soapws.test;

import com.provys.common.exception.InternalException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.checkerframework.checker.nullness.qual.Nullable;

public class ServiceDefinition {

  private final @Nullable Integer port;
  private final @Nullable String address;
  private final String servicePath;
  private final Map<String, EndpointDefinition> endpointsByNamespace;

  public ServiceDefinition(@Nullable Integer port, @Nullable String address, String servicePath,
      Collection<EndpointDefinition> endpoints) {
    this.port = port;
    this.address = address;
    this.servicePath = servicePath;
    this.endpointsByNamespace = endpoints.stream()
        .collect(Collectors.toUnmodifiableMap(endpoint -> endpoint.getXsdSchema().getTargetNamespace(),
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

  public EndpointDefinition getForNamespace(String namespace) {
    var result = endpointsByNamespace.get(namespace);
    if (result == null) {
      throw new InternalException("Endpoint for namespace " + namespace + " not found");
    }
    return result;
  }

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
