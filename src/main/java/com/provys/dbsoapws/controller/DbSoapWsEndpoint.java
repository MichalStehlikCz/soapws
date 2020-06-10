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
import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;
import org.springframework.ws.transport.context.TransportContext;
import org.springframework.ws.transport.context.TransportContextHolder;
import org.springframework.ws.transport.http.HttpServletConnection;

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

  private String getDbProcedure(byte[] requestData, String uri) {
    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    try (var requestStream = new ByteArrayInputStream(requestData)) {
      XMLEventReader reader = xmlInputFactory.createXMLEventReader(requestStream);
      var element = reader.nextTag().asStartElement();
      var namespace = element.getName().getNamespaceURI();
      var endpoint = serviceDefinition.getForNamespace(namespace);
      /* Verify endpoint against uri - while spring can handle endpoint resolution from request,
         security is filtered on uri level.
         We cannot use default Spring approach where single MessageDispatcher works with one
         security configuration as we cannot create varying number of message dispatchers based on
         configuration.
       */
      if (!('/' + endpoint.getName()).equalsIgnoreCase(uri)) {
        throw new InternalException("Namespace does not correspond to use uri (uri " + uri
            + ", resolved endpoint " + endpoint.getName());
      }
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

  private StreamSource serverCall(byte[] requestData, String uri) {
    var dbProcedure = getDbProcedure(requestData, uri);
    try (var connection = dbContext.getConnection();
        var statement = connection.prepareCall(
            "BEGIN\n"
                + "  " + dbProcedure + "(\n"
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

  /**
   * Operation being invoked by all SOAP calls. Deciphers package name from target namespace and
   * operation from operation name and calls database.
   *
   * @param request is SOAP request payload
   * @return result returned from database
   */
  @ResponsePayload
  public StreamSource operation(@RequestPayload StreamSource request) {
    byte[] requestData = readRequestData(request);
    TransportContext context = TransportContextHolder.getTransportContext();
    @SuppressWarnings("resource") // we do not manage this connection, only access its properties
    HttpServletConnection connection = (HttpServletConnection )context.getConnection();
    HttpServletRequest httpRequest = connection.getHttpServletRequest();
    return serverCall(requestData, httpRequest.getServletPath()
        + (httpRequest.getPathInfo() == null ? "" : httpRequest.getPathInfo()));
  }

  @Override
  public String toString() {
    return "SoapWsTestEndpoint{" + super.toString() + '}';
  }
}
