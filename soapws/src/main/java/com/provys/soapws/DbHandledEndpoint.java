package com.provys.soapws;

import com.provys.common.exception.InternalException;
import com.provys.db.dbcontext.DbContext;
import com.provys.db.dbcontext.SqlException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Types;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Validator;
import org.xml.sax.SAXException;

/**
 * Common ancestor for end-points, handled completely via database call (e.g. BLOB IN, BLOB OUT).
 */
public abstract class DbHandledEndpoint {

  private final DbContext dbContext;

  public DbHandledEndpoint(DbContext dbContext) {
    this.dbContext = dbContext;
  }

  private static byte[] readRequestData(StreamSource request) {
    try (var requestStream = request.getInputStream()) {
      return requestStream.readAllBytes();
    } catch (IOException e) {
      throw new InternalException("Error reading request data", e);
    }
  }

  /**
   * Get server package where server logic for this endpoint is implemented.
   *
   * @return name of server package with interface logic
   */
  protected abstract String getPackage();

  private StreamSource serverCall(byte[] requestData, String method) {
    try (var connection = dbContext.getConnection();
        var statement = connection.prepareCall(
            "BEGIN\n"
                + "  " + getPackage() + ".mp_Import" + method + "(\n"
                + "        p_Request => ?\n"
                + "      , o_Response => ?\n"
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
   * Process request - validate data and call server procedure.
   *
   * @param request contains request payload
   * @return result of server method call
   */
  protected StreamSource processRequest(StreamSource request) {
    byte[] requestData = readRequestData(request);
    String method;
    XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
    try (var requestStream = new ByteArrayInputStream(requestData)) {
      XMLEventReader reader = xmlInputFactory.createXMLEventReader(requestStream);
      var element = reader.nextTag().asStartElement();
      method = element.getName().getLocalPart();

    } catch (XMLStreamException | IOException e) {
      throw new InternalException("cannot read XML stream", e);
    }

//    validateRequest(requestData);
    return serverCall(requestData, method);
  }

  @Override
  public String toString() {
    return "DbHandledEndpoint{"
        + "dbContext=" + dbContext
//        + ", validator=" + validator
        + '}';
  }
}
