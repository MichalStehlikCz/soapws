package com.provys.dbsoapws.configuration;

import static org.checkerframework.checker.nullness.NullnessUtil.castNonNull;

import com.provys.common.exception.InternalException;
import com.provys.dbsoapws.controller.DbSoapWsEndpoint;
import java.util.Objects;
import javax.xml.transform.stream.StreamSource;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.MethodEndpoint;
import org.springframework.ws.server.endpoint.mapping.AbstractEndpointMapping;

public class DbEndpointMapping extends AbstractEndpointMapping {

  @Override
  protected void initApplicationContext() {
    super.initApplicationContext();
    try {
        setDefaultEndpoint(new MethodEndpoint("dbSoapWsEndpoint",
            Objects.requireNonNull(castNonNull(getApplicationContext())),
            DbSoapWsEndpoint.class.getMethod("operation", StreamSource.class)
        ));
    } catch (NoSuchMethodException e) {
      throw new InternalException("Invalid reference to DbSoapWsEndpoint#operation", e);
    }
  }

  @Override
  @SuppressWarnings("override.return.invalid") // Spring is not annotated
  protected @Nullable Object getEndpointInternal(MessageContext messageContext) throws Exception {
    return null;
  }
}
