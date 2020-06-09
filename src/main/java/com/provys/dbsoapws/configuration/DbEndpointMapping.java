package com.provys.dbsoapws.configuration;

import static org.checkerframework.checker.nullness.NullnessUtil.castNonNull;

import com.provys.common.exception.InternalException;
import com.provys.dbsoapws.controller.DbSoapWsEndpoint;
import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.stream.StreamSource;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

public class DbEndpointMapping extends AbstractEndpointMapping {

  private @MonotonicNonNull MethodEndpoint dbEndpoint = null;

  @Override
  protected void initApplicationContext() {
    super.initApplicationContext();
    try {
      dbEndpoint = new MethodEndpoint("dbSoapWsEndpoint",
          Objects.requireNonNull(castNonNull(getApplicationContext())),
          DbSoapWsEndpoint.class.getMethod("operation", StreamSource.class,
              MessageContext.class)
      );
    } catch (NoSuchMethodException e) {
      throw new InternalException("Invalid reference to DbSoapWsEndpoint#operation", e);
    }
  }

  @Override
  @SuppressWarnings("override.return.invalid") // Spring is not annotated
  protected @Nullable Object getEndpointInternal(MessageContext messageContext) throws Exception {
    TransportContext context = TransportContextHolder.getTransportContext();
    @SuppressWarnings("resource") // we do not manage this connection, only access its properties
    HttpServletConnection connection = (HttpServletConnection )context.getConnection();
    HttpServletRequest httpRequest = connection.getHttpServletRequest();
    if (httpRequest.getPathInfo().equals("/error")) {
      // we do not want to map error page to our endpoint
      return null;
    }
    return dbEndpoint;
  }

  @Override
  public String toString() {
    return "DbEndpointMapping{"
        + "dbEndpoint=" + dbEndpoint + '}';
  }
}
