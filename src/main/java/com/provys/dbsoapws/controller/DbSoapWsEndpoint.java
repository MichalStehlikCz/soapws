package com.provys.dbsoapws.controller;

import com.provys.common.exception.InternalException;
import com.provys.db.dbcontext.DbContext;
import com.provys.db.dbcontext.SqlException;
import com.provys.db.provysdb.UserDbContext;
import com.provys.dbsoapws.model.ServiceDefinition;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

@Endpoint
public class DbSoapWsEndpoint {

  private static final String OPERATION_SUFFIX = "Request";

  private final ServiceDefinition serviceDefinition;
  private final DbContext dbContext;

  @Autowired
  public DbSoapWsEndpoint(ServiceDefinition serviceDefinition, UserDbContext dbContext) {
    this.serviceDefinition = serviceDefinition;
    this.dbContext = dbContext;
  }

  private static byte[] readRequestData(StreamSource request) {
    try (var requestStream = request.getInputStream()) {
      return requestStream.readAllBytes();
    } catch (IOException e) {
      throw new InternalException("Error reading request data", e);
    }
  }

  private String getDbProcedure(byte[] requestData) {
    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    try (var requestStream = new ByteArrayInputStream(requestData)) {
      XMLEventReader reader = xmlInputFactory.createXMLEventReader(requestStream);
      var element = reader.nextTag().asStartElement();
      var namespace = element.getName().getNamespaceURI();
      var packageNm = serviceDefinition.getForNamespace(namespace).getPackageNm();
      var method = element.getName().getLocalPart();
      if (!method.endsWith(OPERATION_SUFFIX)) {
        throw new InternalException("Method in soap request should end with Request, found "
            + method);
      }
      return packageNm + ".mp_Import"
          + method.substring(0, method.length() - OPERATION_SUFFIX.length());
    } catch (XMLStreamException | IOException e) {
      throw new InternalException("cannot read XML stream", e);
    }
  }

  private StreamSource serverCall(byte[] requestData) {
    try (var connection = dbContext.getConnection();
        var statement = connection.prepareCall(
            "BEGIN\n"
                + "  " + getDbProcedure(requestData) + "(\n"
                + "        ?\n"
                + "      , ?\n"
                + "    );\n"
                + "END;")) {
      statement.setBytes(1, requestData);
      statement.registerOutParameter(2, Types.BLOB);
      statement.execute();
      var resultData = statement.getBytes(2);
      return new StreamSource(new ByteArrayInputStream(resultData));
    } catch (SQLException e) {
      throw new SqlException("Failed to execute import call", e);
    }
  }

//  @PayloadRoot(namespace = "http://com.provys/wsdl/soapwstest", localPart = "Operation1")
  @ResponsePayload
  public StreamSource operation(@RequestPayload StreamSource request) {
    byte[] requestData = readRequestData(request);
    return serverCall(requestData);
  }

  @Override
  public String toString() {
    return "SoapWsTestEndpoint{" + super.toString() + '}';
  }
}
