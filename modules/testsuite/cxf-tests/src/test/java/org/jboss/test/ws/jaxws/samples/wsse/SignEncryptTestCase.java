/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;

import junit.framework.Test;

import org.apache.cxf.binding.soap.saaj.SAAJInInterceptor;
import org.apache.cxf.binding.soap.saaj.SAAJOutInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestSetup;

/**
 * WS-Security sign & encrypt test case
 *
 * @author alessio.soldano@jboss.com
 * @since 29-May-2008
 */
public final class SignEncryptTestCase extends JBossWSTest
{
   private final String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-sign-encrypt";
   
   public static Test suite()
   {
      return new JBossWSTestSetup(SignEncryptTestCase.class, "jaxws-samples-wsse-sign-encrypt-client.jar jaxws-samples-wsse-sign-encrypt.war");
   }

   public void test() throws Exception
   {
      QName serviceName = new QName("http://www.jboss.org/jbossws/ws-extensions/wssecurity", "SecurityService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      ServiceIface proxy = (ServiceIface)service.getPort(ServiceIface.class);
      setupWsse(proxy);
      try
      {
         assertEquals("Secure Hello World!", proxy.sayHello());
      }
      catch (SOAPFaultException e)
      {
         throw new Exception("Please check that the Bouncy Castle provider is installed.", e);
      }
   }
   
   private void setupWsse(ServiceIface proxy)
   {
      Client client = ClientProxy.getClient(proxy);
      Endpoint cxfEndpoint = client.getEndpoint();
      
      Map<String,Object> outProps = new HashMap<String,Object>();
      outProps.put("action", "Timestamp Signature Encrypt");
      outProps.put("user", "alice");
      outProps.put("signaturePropFile", "META-INF/alice.properties");
      outProps.put("signatureKeyIdentifier", "DirectReference");
      outProps.put("passwordCallbackClass", "org.jboss.test.ws.jaxws.samples.wsse.KeystorePasswordCallback");
      outProps.put("signatureParts", "{Element}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp;{Element}{http://schemas.xmlsoap.org/soap/envelope/}Body");
      outProps.put("encryptionPropFile", "META-INF/alice.properties");
      outProps.put("encryptionUser", "Bob");
      outProps.put("encryptionParts", "{Element}{http://www.w3.org/2000/09/xmldsig#}Signature;{Content}{http://schemas.xmlsoap.org/soap/envelope/}Body");
      outProps.put("encryptionSymAlgorithm", "http://www.w3.org/2001/04/xmlenc#tripledes-cbc");
      outProps.put("encryptionKeyTransportAlgorithm", "http://www.w3.org/2001/04/xmlenc#rsa-1_5");
      WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps); //request
      cxfEndpoint.getOutInterceptors().add(wssOut);
      cxfEndpoint.getOutInterceptors().add(new SAAJOutInterceptor());
      
      Map<String,Object> inProps= new HashMap<String,Object>();
      inProps.put("action", "Timestamp Signature Encrypt");
      inProps.put("signaturePropFile", "META-INF/alice.properties");
      inProps.put("passwordCallbackClass", "org.jboss.test.ws.jaxws.samples.wsse.KeystorePasswordCallback");
      inProps.put("decryptionPropFile", "META-INF/alice.properties");
      WSS4JInInterceptor wssIn = new WSS4JInInterceptor(inProps); //response
      cxfEndpoint.getInInterceptors().add(wssIn);
      cxfEndpoint.getInInterceptors().add(new SAAJInInterceptor());
   }
}
