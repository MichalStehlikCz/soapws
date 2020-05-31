package com.provys.soapws.test;

import com.provys.common.exception.InternalException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.xml.sax.SAXException;

@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

  private static XsdSchema getXsdFromFile(String fileName) {
    var result = new SimpleXsdSchema(new FileSystemResource(fileName));
    try {
      result.afterPropertiesSet();
      return result;
    } catch (ParserConfigurationException e) {
      throw new InternalException("Failed to parse xsd file " + fileName, e);
    } catch (IOException e) {
      throw new InternalException("Failed to read xsd file " + fileName, e);
    } catch (SAXException e) {
      throw new InternalException("Invalid xml in xsd file " + fileName, e);
    }
  }
  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
      ApplicationContext applicationContext) {
    var xsdSchemas = new HashMap<String, XsdSchema>(2);
    xsdSchemas.put("SoapWsTest1", getXsdFromFile("soapwstest1.xsd"));
    xsdSchemas.put("SoapWsTest2", getXsdFromFile("soapwstest2.xsd"));
    MessageDispatcherServlet servlet = new DbMessageDispatcherServlet(xsdSchemas);
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet, "/ws/*");
  }
}
