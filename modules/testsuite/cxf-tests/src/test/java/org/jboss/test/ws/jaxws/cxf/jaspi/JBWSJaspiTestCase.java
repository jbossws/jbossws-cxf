/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.cxf.jaspi;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.ws.security.util.XmlSchemaDateFormat;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

public class JBWSJaspiTestCase extends JBossWSTest
{
   private String address = "http://" + getServerHost() + ":8080/jaxws-cxf-jaspi/JaspiEndpoint";
   private String wsdlURl = address + "?wsdl";
   private String targetNS = "http://org.jboss.ws.jaxws.cxf/jaspi";
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(JBWSJaspiTestCase.class, "jaxws-cxf-jaspi.war");
   }
   
   public void testValidMessage() throws Exception
   {
      
      Service service = Service.create(new URL(wsdlURl), new QName(targetNS, "JaspiService"));
      Dispatch<SOAPMessage> dispatch = service.createDispatch(new QName(targetNS, "JaspiEndpointPort"), SOAPMessage.class, Service.Mode.MESSAGE);
      SOAPMessage response = dispatch.invoke(prepareSOAPMessage("org/jboss/test/ws/jaxws/cxf/jaspi/usernametoken-soapmessage.xml"));
      response.writeTo(System.out);
   }
  
   private SOAPMessage prepareSOAPMessage(String messageFile) throws Exception {
      MessageFactory factory = MessageFactory.newInstance();
      URL fileURl = Thread.currentThread().getContextClassLoader()
            .getResource(messageFile);
      FileInputStream fins = new FileInputStream(fileURl.getFile());
      String content = readFile(fins);
      XmlSchemaDateFormat formater = new XmlSchemaDateFormat();

      String replaced = content.replaceAll("\\$NOW", formater.format(new Date()));     
      ByteArrayInputStream bin = new ByteArrayInputStream(replaced.getBytes());
      return factory.createMessage(null, bin);
   }
   
   private String readFile(FileInputStream in) throws IOException {
      StringBuilder sb = new StringBuilder(1024);
      for (int i = in.read(); i != -1; i = in.read()) {
         sb.append((char) i);
      }
      in.close();
      return sb.toString();
   }

   
}
