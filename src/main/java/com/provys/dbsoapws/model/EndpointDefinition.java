package com.provys.dbsoapws.model;

import java.util.Locale;
import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.xml.xsd.XsdSchema;

public class EndpointDefinition {

  private final String name;
  private final String path;
  private final String packageNm;
  private final String authProvider;
  private final XsdSchema xsdSchema;

  private static String validatePath(String name, @Nullable String path) {
    var result = (path == null) ? name : path;
    if (result.charAt(0) != '/') {
      result = '/' + result;
    }
    result = result.toLowerCase(Locale.ENGLISH);
    return result;
  }

  /**
   * Create new endpoint definition with supplied properties.
   *
   * @param name is name of endpoint
   * @param path is path (not used at the moment)
   * @param packageNm is package handling calls to this endpoint
   * @param authProvider is authentication provider used for this endpoint
   * @param xsdSchema is xsd defining requests for this endpoint
   */
  public EndpointDefinition(String name, @Nullable String path, String packageNm,
      String authProvider, XsdSchema xsdSchema) {
    if (name.isEmpty()) {
      throw new IllegalArgumentException("Endpoint name cannot be empty");
    }
    this.name = name;
    this.path = validatePath(name, path);
    this.packageNm = Objects.requireNonNull(packageNm);
    this.authProvider = Objects.requireNonNull(authProvider);
    this.xsdSchema = Objects.requireNonNull(xsdSchema);
  }

  /**
   * Value of field name.
   *
   * @return value of field name
   */
  public String getName() {
    return name;
  }

  /**
   * Value of field path.
   *
   * @return value of field path
   */
  public String getPath() {
    return path;
  }

  /**
   * Value of field packageNm.
   *
   * @return value of field packageNm
   */
  public String getPackageNm() {
    return packageNm;
  }

  /**
   * Value of field authProvider.
   *
   * @return value of field authProvider
   */
  public String getAuthProvider() {
    return authProvider;
  }

  /**
   * Value of field xsdSchema.
   *
   * @return value of field xsdSchema
   */
  public XsdSchema getXsdSchema() {
    return xsdSchema;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EndpointDefinition that = (EndpointDefinition) o;
    return name.equals(that.name)
        && path.equals(that.path)
        && packageNm.equals(that.packageNm)
        && authProvider.equals(that.authProvider)
        && xsdSchema.equals(that.xsdSchema);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + path.hashCode();
    result = 31 * result + packageNm.hashCode();
    result = 31 * result + authProvider.hashCode();
    result = 31 * result + xsdSchema.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "EndpointDefinition{"
        + "name='" + name + '\''
        + ", path='" + path + '\''
        + ", packageNm='" + packageNm + '\''
        + ", authProvider='" + authProvider + '\''
        + ", xsdSchema=" + xsdSchema
        + '}';
  }
}
