package com.provys.dbsoapws.model;

import java.util.Objects;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.xml.xsd.XsdSchema;

public class EndpointDefinition {

  private final String name;
  private final @Nullable String path;
  private final String packageNm;
  private final String authProvider;
  private final XsdSchema xsdSchema;

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
    this.name = name;
    this.path = path;
    this.packageNm = packageNm;
    this.authProvider = authProvider;
    this.xsdSchema = xsdSchema;
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
  public @Nullable String getPath() {
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
        && Objects.equals(path, that.path)
        && packageNm.equals(that.packageNm)
        && authProvider.equals(that.authProvider)
        && xsdSchema.equals(that.xsdSchema);
  }

  @Override
  public int hashCode() {
    int result = name.hashCode();
    result = 31 * result + (path != null ? path.hashCode() : 0);
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
