package com.provys.dbsoapws.configuration;

import com.provys.common.datatype.DtBinaryData;
import com.provys.common.datatype.DtUid;
import com.provys.common.exception.InternalException;
import com.provys.db.dbcontext.DbConnection;
import com.provys.db.dbcontext.DbContext;
import com.provys.db.dbcontext.SqlException;
import com.provys.db.provysdb.AdminDbContext;
import com.provys.dbsoapws.model.EndpointDefinition;
import com.provys.dbsoapws.model.ServiceDefinition;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.InputStreamResource;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.xml.sax.SAXException;

@Configuration
@Profile("!test")
public class DbConfigReader {

  private static final String DEFAULT_AUTH_PROVIDER = "NONE";

  private final String deviceNm;
  private final DbContext dbContext;

  @Autowired
  public DbConfigReader(@Value("${dbsoapws.deviceNm}") String deviceNm,
      AdminDbContext dbContext) {
    this.deviceNm = deviceNm;
    this.dbContext = dbContext;
  }

  private static XsdSchema getXsdFromBinaryData(DtBinaryData data) {
    var result = new SimpleXsdSchema(new InputStreamResource(data.getInputStream()));
    try {
      result.afterPropertiesSet();
      return result;
    } catch (ParserConfigurationException e) {
      throw new InternalException("Failed to parse xsd data " + data, e);
    } catch (IOException e) {
      throw new InternalException("Failed to read xsd data " + data, e);
    } catch (SAXException e) {
      throw new InternalException("Invalid xml in xsd data " + data, e);
    }
  }

  private DtUid getDeviceId(DbConnection connection) throws SQLException {
    try (var getDeviceStatement = connection.prepareStatement(
        "SELECT\n"
            + "    device.device_id\n"
            + "FROM\n"
            + "    vid_device_vw device\n"
            + "WHERE\n"
            + "      (device.name_nm=?)")) {
      getDeviceStatement.setNonNullString(1, deviceNm);
      try (var resultSet = getDeviceStatement.executeQuery()) {
        if (!resultSet.next()) {
          throw new InternalException("Device " + deviceNm + " not found");
        }
        return resultSet.getNonNullDtUid(1);
      }
    }
  }

  private static class ServiceParams {
    private @Nullable Integer port;
    private @Nullable String address;
    private final String servicePath;

    ServiceParams(String servicePath) {
      this.servicePath = servicePath;
    }

    ServiceParams() {
      this("");
    }
  }

  private static ServiceParams getServiceParams(DbConnection connection, DtUid deviceId)
      throws SQLException {
    try (var getSoapWsStatement = connection.prepareStatement(
        "SELECT\n"
            + "    VID_DevIfaceParam_EP.mfw_GetValue_DevIface(deviface.deviface_id, 'PORT')\n"
            + "  , VID_DevIfaceParam_EP.mfw_GetValue_DevIface(deviface.deviface_id, 'ADDRESS')\n"
            + "  , VID_DevIfaceParam_EP.mfw_GetValue_DevIface(deviface.deviface_id, 'ROOT')\n"
            + "FROM\n"
            + "    vid_devtype_vw devtype\n"
            + "  , vid_deviface_vw deviface\n"
            + "WHERE\n"
            + "      (devtype.name_nm='DBSOAPWSSRV')\n"
            + "  AND (deviface.device_id=?)\n"
            + "  AND (deviface.devtype_id=devtype.devtype_id)")) {
      getSoapWsStatement.setNonNullDtUid(1, deviceId);
      try (var resultSet = getSoapWsStatement.executeQuery()) {
        if (resultSet.next()) {
          // only set properties if interface was found
          var serviceParams = new ServiceParams(resultSet.getNonNullString(3));
          serviceParams.port = resultSet.getNullableInteger(1);
          serviceParams.address = resultSet.getNullableString(2);
          if (resultSet.next()) {
            throw new InternalException("Incorrect configuration for device " + deviceId
                + ": multiple interfaces of type DBSOAPWSSRV");
          }
          return serviceParams;
        }
      }
      return new ServiceParams();
    }
  }

  private static List<EndpointDefinition> getEndpoints(DbConnection connection, DtUid deviceId)
      throws SQLException {
    try (var getSoapWsStatement = connection.prepareStatement(
        "SELECT\n"
            + "    VID_DevIfaceParam_EP.mfw_GetValue_DevIface(deviface.deviface_id, 'NAME')\n"
            + "  , VID_DevIfaceParam_EP.mfw_GetValue_DevIface(deviface.deviface_id, 'PATH')\n"
            + "  , VID_DevIfaceParam_EP.mfw_GetValue_DevIface(deviface.deviface_id,"
            + " 'PACKAGE_NM')\n"
            + "  , VID_DevIfaceParam_EP.mfw_GetValue_DevIface(deviface.deviface_id,"
            + " 'AUTHPROVIDER_NM')\n"
            + "  , KER_AttrVal_EP.mf_GetValueB_NMObject(\n"
            + "          VID_DevIfaceParam_EP.mfw_GetID_DevIface(deviface.deviface_id, 'XSD')\n"
            + "        , 'DEVIFACEPARAM'\n"
            + "        , 'BVALUE'\n"
            + "      )\n"
            + "FROM\n"
            + "    vid_devtype_vw devtype\n"
            + "  , vid_deviface_vw deviface\n"
            + "WHERE\n"
            + "      (devtype.name_nm='DBSOAPWSSERVICE')\n"
            + "  AND (deviface.device_id=?)\n"
            + "  AND (deviface.devtype_id=devtype.devtype_id)")) {
      getSoapWsStatement.setNonNullDtUid(1, deviceId);
      try (var resultSet = getSoapWsStatement.executeQuery()) {
        List<EndpointDefinition> endpointDefinitions = new ArrayList<>(5);
        while (resultSet.next()) {
          // read endpoint definition
          var authProvider = resultSet.getNullableString(4);
          if (authProvider == null) {
            authProvider = DEFAULT_AUTH_PROVIDER;
          }
          endpointDefinitions.add(new EndpointDefinition(
              resultSet.getNonNullString(1),
              resultSet.getNullableString(2),
              resultSet.getNonNullString(3),
              authProvider,
              getXsdFromBinaryData(resultSet.getNonNullDtBinaryData(5))));
        }
        return endpointDefinitions;
      }
    }
  }

  @Bean
  ServiceDefinition serviceDefinition() {
    try (var connection = dbContext.getConnection()) {
      var deviceId = getDeviceId(connection);
      var serviceParams = getServiceParams(connection, deviceId);
      var endpointDefinitions = getEndpoints(connection, deviceId);
      return new ServiceDefinition(serviceParams.port, serviceParams.address,
          serviceParams.servicePath, endpointDefinitions);
    } catch (SQLException e) {
      throw new SqlException("Failed to close connection", e);
    }
  }

  @Override
  public String toString() {
    return "DbConfigReader{"
        + "deviceNm='" + deviceNm + '\''
        + ", dbContext=" + dbContext
        + '}';
  }
}
