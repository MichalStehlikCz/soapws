package com.provys.dbsoapws.configuration;

import com.provys.common.exception.InternalException;
import com.provys.dbsoapws.model.ServiceDefinition;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.ws.support.WebUtils;
import org.springframework.ws.transport.http.HttpTransportConstants;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.WsdlDefinition;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.XsdSchema;

/**
 * Special message dispatcher servlet that takes list of xsd files and exposes them as wsdls and
 * that routes all requests to single DB endpoint.
 */
class DbMessageDispatcherServlet extends MessageDispatcherServlet {

  private static final long serialVersionUID = 4876097123271799936L;

  /** Suffix of a WSDL request uri. */
  private static final String WSDL_SUFFIX_NAME = ".wsdl";

  /** Suffix of a XSD request uri. */
  private static final String XSD_SUFFIX_NAME = ".xsd";

  private final Map<String, XsdSchema> xsdSchemas;
  private final Map<String, WsdlDefinition> wsdlDefinitions;

  private static WsdlDefinition buildWsdlFromXsd(String name, XsdSchema xsdSchema) {
    var wsdlDefinition = new DefaultWsdl11Definition();
    wsdlDefinition.setPortTypeName(name + "Port");
    wsdlDefinition.setLocationUri('/' + name.toLowerCase(Locale.ENGLISH));
    wsdlDefinition.setTargetNamespace(xsdSchema.getTargetNamespace());
    wsdlDefinition.setSchema(xsdSchema);
    try {
      wsdlDefinition.afterPropertiesSet();
      return wsdlDefinition;
    } catch (Exception e) {
      throw new InternalException("Error preparing wsdl definition from xsd " + name, e);
    }
  }

  DbMessageDispatcherServlet(ServiceDefinition serviceDefinition) {
    this.xsdSchemas = serviceDefinition.getEndpoints().stream()
        .map(epd -> Map.entry(epd.getName().toLowerCase(Locale.ENGLISH), epd.getXsdSchema()))
        .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
    this.wsdlDefinitions = serviceDefinition.getEndpoints().stream()
        .map(epd -> Map.entry(epd.getName().toLowerCase(Locale.ENGLISH),
            buildWsdlFromXsd(epd.getName(), epd.getXsdSchema())))
        .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
  }

  @Override
  @SuppressWarnings("override.return.invalid") // spring ancestor is not annotated
  protected @Nullable WsdlDefinition getWsdlDefinition(HttpServletRequest request) {
    if (HttpTransportConstants.METHOD_GET.equals(request.getMethod())
        && request.getRequestURI().endsWith(WSDL_SUFFIX_NAME)) {
      String fileName = WebUtils.extractFilenameFromUrlPath(request.getRequestURI());
      return wsdlDefinitions.get(fileName);
    }
    return null;
  }

  @Override
  @SuppressWarnings("override.return.invalid") // spring ancestor is not annotated
  protected @Nullable XsdSchema getXsdSchema(HttpServletRequest request) {
    if (HttpTransportConstants.METHOD_GET.equals(request.getMethod())
        && request.getRequestURI().endsWith(XSD_SUFFIX_NAME)) {
      String fileName = WebUtils.extractFilenameFromUrlPath(request.getRequestURI());
      return xsdSchemas.get(fileName);
    }
    return null;
  }

  @Override
  public String toString() {
    return "DbMessageDispatcherServlet{"
        + "xsdSchemas=" + xsdSchemas
        + ", wsdlDefinitions=" + wsdlDefinitions + '}';
  }
}
