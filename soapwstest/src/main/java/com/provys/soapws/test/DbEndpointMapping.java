package com.provys.soapws.test;

import static org.checkerframework.checker.nullness.NullnessUtil.castNonNull;

import com.provys.common.exception.InternalException;
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
        setDefaultEndpoint(new MethodEndpoint("soapWsDbEndpoint",
            Objects.requireNonNull(castNonNull(getApplicationContext())),
            SoapWsDbEndpoint.class.getMethod("operation", StreamSource.class)
        ));
    } catch (NoSuchMethodException e) {
      throw new InternalException("Invalid reference to SoapWsDbEndpoint#operation", e);
    }
  }

  @Override
  @SuppressWarnings("override.return.invalid") // Spring is not annotated
  protected @Nullable Object getEndpointInternal(MessageContext messageContext) throws Exception {
    return null;
  }
}
