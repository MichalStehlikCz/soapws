package com.provys.soapws.test;

import com.provys.db.provysdb.UserDbContext;
import com.provys.soapws.DbHandledEndpoint;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class SoapWsTestEndpoint extends DbHandledEndpoint {

  private static final String NAMESPACE_URI = "http://com.provys/wsdl/soapwstest";
  private static final String PACKAGE = "KER_SoapWsTest_PG";

  @Autowired
  public SoapWsTestEndpoint(UserDbContext dbContext,
      @Qualifier("soapWsTestSchemaValidator") Validator validator) {
    super(dbContext, validator);
  }

  @Override
  protected String getPackage() {
    return PACKAGE;
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Operation1Request")
  @ResponsePayload
  public StreamSource operation1(@RequestPayload StreamSource request) {
    return processRequest(request, "Operation1");
  }

  @PayloadRoot(namespace = NAMESPACE_URI, localPart = "Operation2Request")
  @ResponsePayload
  public StreamSource operation2(@RequestPayload StreamSource request) {
    return processRequest(request, "Operation2");
  }

  @Override
  public String toString() {
    return "SoapWsTestEndpoint{" + super.toString() + '}';
  }
}
