package com.provys.soapws.test;

import com.provys.common.exception.InternalException;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;
import org.xml.sax.SAXException;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

  @Bean
  public ServletRegistrationBean<MessageDispatcherServlet> messageDispatcherServlet(
      ApplicationContext applicationContext) {
    MessageDispatcherServlet servlet = new MessageDispatcherServlet();
    servlet.setApplicationContext(applicationContext);
    servlet.setTransformWsdlLocations(true);
    return new ServletRegistrationBean<>(servlet, "/ws/*");
  }

  @Bean(name = "soapwstest")
  public DefaultWsdl11Definition defaultWsdl11Definition(
      @Qualifier("soapWsTestSchema") XsdSchema soapWsTestSchema) {
    DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
    wsdl11Definition.setPortTypeName("SoapWsTestPort");
    wsdl11Definition.setLocationUri("/ws");
    wsdl11Definition.setTargetNamespace("http://com.provys/wsdl/soapwstest");
    wsdl11Definition.setSchema(soapWsTestSchema);
    return wsdl11Definition;
  }

  @Bean
  public XsdSchema soapWsTestSchema() {
    return new SimpleXsdSchema(new ClassPathResource("soapwstest.xsd"));
  }

  @Bean
  public Validator soapWsTestSchemaValidator() {
    SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
    try {
      Schema schema = factory.newSchema(new ClassPathResource("soapwstest.xsd").getFile());
      return schema.newValidator();
    } catch (IOException e) {
      throw new InternalException("Failed to read xsd", e);
    } catch (SAXException e) {
      throw new InternalException("Failed to parse xsd", e);
    }
  }
}
