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
package org.jboss.test.ws.jaxws.samples.wsse.kerberos;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.policy.WSPolicyFeature;
import org.apache.cxf.ws.security.kerberos.KerberosClient;
import org.jboss.test.ws.jaxws.samples.wsse.kerberos.contract.DoubleItPortType;
import org.jboss.wsf.stack.cxf.security.authentication.callback.UsernameTokenCallback;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;
 
/**
 * This test is excluded. Please modify modules/testsuite/pom.xml to enable this test.  
 * Before run this test, a KDC of realm "WS.APACHE.ORG" is required to setup first.
 * Please look at these two links to find more info about setup a KDC on Fedora and configure it with realm and principals: 
 * https://docs.fedoraproject.org/en-US/Fedora//html/Security_Guide/sect-Security_Guide-Kerberos-Configuring_a_Kerberos_5_Server.html
 * http://coheigea.blogspot.com/2011/10/using-kerberos-with-web-services-part-i.html
 * In server side, add the following security-domain to security subsystem section in AS7/WildFly80 standalone.xml:
 * 
 * <pre>
 *               {@code
 *                  <security-domain name="alice" cache-type="default">
                    <authentication>
                        <login-module code="com.sun.security.auth.module.Krb5LoginModule" flag="required">
                            <module-option name="refreshKrb5Config" value="true"/>
                            <module-option name="useKeyTab" value="true"/>
                            <module-option name="keyTab" value="/home/jimma/alice.keytab"/>
                            <module-option name="principal" value="alice"/>
                            <module-option name="storeKey" value="true"/>
                            <module-option name="debug" value="true"/>
                        </login-module>
                    </authentication>
                </security-domain>
                <security-domain name="bob" cache-type="default">
                    <authentication>
                        <login-module code="com.sun.security.auth.module.Krb5LoginModule" flag="required">
                            <module-option name="refreshKrb5Config" value="true"/>
                            <module-option name="useKeyTab" value="true"/>
                            <module-option name="keyTab" value="/home/jimma/bob.keytab"/>
                            <module-option name="principal" value="bob/service.ws.apache.org"/>
                            <module-option name="storeKey" value="true"/>
                            <module-option name="debug" value="true"/>
                        </login-module>
                    </authentication>
                </security-domain>
                
                }
   </pre>
 * Run this test with command : <pre>mvn clean install -Ptestsuite,wildfly800,spring -Dtest=KerberosTestCase 
 * -Djava.security.auth.login.config=modules/testsuite/cxf-spring-tests/target/test-resources/jaxws/samples/wsse/kerberos/kerberos.jaas</pre>
 */ 
 


public class KerberosTestCase extends JBossWSTest
{
  
   private static final String namespace = "http://www.example.org/contract/DoubleIt";
   private static final QName serviceName = new QName(namespace, "DoubleItService");
   public static Test suite()
   {
      JBossWSCXFTestSetup testSetup;
      testSetup = new JBossWSCXFTestSetup(KerberosTestCase.class, "jaxws-samples-wsse-kerberos-client.jar jaxws-samples-wsse-kerberos.war");      
      Map<String, String> sslOptions = new HashMap<String, String>();
      if (isTargetJBoss7())
      {
         sslOptions.put("certificate-key-file", System.getProperty("org.jboss.ws.testsuite.server.keystore"));
         sslOptions.put("password", "changeit");
         sslOptions.put("verify-client", "false");
         sslOptions.put("key-alias", "tomcat");
      }
      else
      {
         sslOptions.put("server-identity.ssl.keystore-path", System.getProperty("org.jboss.ws.testsuite.server.keystore"));
         sslOptions.put("server-identity.ssl.keystore-password", "changeit");
         sslOptions.put("server-identity.ssl.alias", "tomcat");
      }
      testSetup.setHttpsConnectorRequirement(sslOptions);
      
      return testSetup;
   }

   public void testKerberosSupport() throws Exception
   {
      String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-kerberos/DoubleItKerberosSupport";
      QName servicePort = new QName(namespace, "DoubleItKerberosSupportingPort");

      QName serviceName = new QName(namespace, "DoubleItService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      DoubleItPortType proxy = (DoubleItPortType)service.getPort(servicePort, DoubleItPortType.class);
      setupKerberosSupport(proxy);
      assertEquals(20, proxy.doubleIt(10));
   }
   
   
   public void testKerberosTransport() throws Exception
   {
      String serviceURL = "https://" + getServerHost() + ":8443/jaxws-samples-wsse-kerberos/DoubleItKerberosTransport";
      QName servicePort = new QName(namespace, "DoubleItKerberosTransportPort");
      QName serviceName = new QName(namespace, "DoubleItService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      DoubleItPortType proxy = (DoubleItPortType)service.getPort(servicePort, DoubleItPortType.class);
      setupKerberosTransport(proxy);
      assertEquals(20, proxy.doubleIt(10));
   }
   
   
   public void testKerberosSymmetricSupporting() throws Exception
   {
      String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-kerberos/DoubleItKerberosOverSymmetricSupporting";
      QName servicePort = new QName(namespace, "DoubleItKerberosSymmetricSupportingPort");
      QName serviceName = new QName(namespace, "DoubleItService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      DoubleItPortType proxy = (DoubleItPortType)service.getPort(servicePort, DoubleItPortType.class);
      setupSymmetricSupporting(proxy);
      assertEquals(20, proxy.doubleIt(10));
   }
   
   
   public void testKerberosAsymmetric() throws Exception
   {
      String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-kerberos/DoubleItKerberosAsymmetric";
      QName servicePort = new QName(namespace, "DoubleItKerberosAsymmetricPort");
      QName serviceName = new QName(namespace, "DoubleItService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      DoubleItPortType proxy = (DoubleItPortType)service.getPort(servicePort, DoubleItPortType.class);
      setupAsymmetric(proxy);
      assertEquals(20, proxy.doubleIt(10));
   }
   
   public void testKerberosOverAsymmetricSignedEncrypted() throws Exception
   {
      String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-kerberos/DoubleItKerberosOverAsymmetricSignedEncrypted";
      QName servicePort = new QName(namespace, "DoubleItKerberosAsymmetricSignedEncryptedPort");
      QName serviceName = new QName(namespace, "DoubleItService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      DoubleItPortType proxy = (DoubleItPortType)service.getPort(servicePort, DoubleItPortType.class);
      setupAsymmetricSignedEncrypted(proxy);
      assertEquals(20, proxy.doubleIt(10));
   }
   
   public void testKerberosKerberosSymmetric() throws Exception
   {
      String serviceURL = "http://" + getServerHost() + ":8080/jaxws-samples-wsse-kerberos/DoubleItKerberosSymmetric";
      QName servicePort = new QName(namespace, "DoubleItKerberosSymmetricPort");
      QName serviceName = new QName(namespace, "DoubleItService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      DoubleItPortType proxy = (DoubleItPortType)service.getPort(servicePort, DoubleItPortType.class);
      setupSymmetricSupporting(proxy);
      assertEquals(20, proxy.doubleIt(10));
   }   
   
   private void setupAsymmetricSignedEncrypted(DoubleItPortType proxy)
   {
      Client client = ClientProxy.getClient(proxy);
      Endpoint cxfEndpoint = client.getEndpoint();
      cxfEndpoint.put("ws-security.callback-handler","org.jboss.test.ws.jaxws.samples.wsse.kerberos.KeystorePasswordCallback");
      cxfEndpoint.put("ws-security.encryption.properties", "META-INF/bob.properties");
      cxfEndpoint.put("ws-security.encryption.username", "bob");
      cxfEndpoint.put("ws-security.signature.properties", "META-INF/alice.properties");
      cxfEndpoint.put("ws-security.signature.username", "alice");
      
      client.getBus().getFeatures().add(new LoggingFeature());
      client.getBus().getFeatures().add(new WSPolicyFeature());
      KerberosClient kerberosClient = new KerberosClient(client.getBus());
      kerberosClient.setServiceName("bob@service.ws.apache.org");
      kerberosClient.setContextName("alice");
      cxfEndpoint.put("ws-security.kerberos.client", kerberosClient);     
   }

  private void setupAsymmetric(DoubleItPortType proxy)
   {
      Client client = ClientProxy.getClient(proxy);
      Endpoint cxfEndpoint = client.getEndpoint();
      cxfEndpoint.put("ws-security.callback-handler","org.jboss.test.ws.jaxws.samples.wsse.kerberos.KeystorePasswordCallback");
      cxfEndpoint.put("ws-security.encryption.properties", "META-INF/bob.properties");
      cxfEndpoint.put("ws-security.encryption.username", "bob");
      cxfEndpoint.put("ws-security.signature.properties", "META-INF/alice.properties");
      cxfEndpoint.put("ws-security.signature.username", "alice");
      
      client.getBus().getFeatures().add(new LoggingFeature());
      client.getBus().getFeatures().add(new WSPolicyFeature());
      KerberosClient kerberosClient = new KerberosClient(client.getBus());
      kerberosClient.setServiceName("bob@service.ws.apache.org");
      kerberosClient.setContextName("alice");
      cxfEndpoint.put("ws-security.kerberos.client", kerberosClient);     
   }
     
   private void setupSymmetricSupporting(DoubleItPortType proxy)
   {
      Client client = ClientProxy.getClient(proxy);
      Endpoint cxfEndpoint = client.getEndpoint();
      cxfEndpoint.put("ws-security.encryption.properties", "META-INF/bob.properties");
      cxfEndpoint.put("ws-security.encryption.username", "bob");
      
      client.getBus().getFeatures().add(new LoggingFeature());
      client.getBus().getFeatures().add(new WSPolicyFeature());
      KerberosClient kerberosClient = new KerberosClient(client.getBus());
      kerberosClient.setServiceName("bob@service.ws.apache.org");
      kerberosClient.setContextName("alice");
      cxfEndpoint.put("ws-security.kerberos.client", kerberosClient);     
   }

   private void setupKerberosSupport(DoubleItPortType proxy)
   {
      Client client = ClientProxy.getClient(proxy);
      Endpoint cxfEndpoint = client.getEndpoint();
      client.getBus().getFeatures().add(new LoggingFeature());
      client.getBus().getFeatures().add(new WSPolicyFeature());
      KerberosClient kerberosClient = new KerberosClient(client.getBus());
      kerberosClient.setServiceName("bob@service.ws.apache.org");
      kerberosClient.setContextName("alice");
      cxfEndpoint.put("ws-security.kerberos.client", kerberosClient);     
   }
   
   
   private void setupKerberosTransport(DoubleItPortType proxy)
   {
      Client client = ClientProxy.getClient(proxy);
      Endpoint cxfEndpoint = client.getEndpoint();
      cxfEndpoint.put("ws-security.is-bsp-compliant", "false");
 
      client.getBus().getFeatures().add(new LoggingFeature());
      client.getBus().getFeatures().add(new WSPolicyFeature());
      KerberosClient kerberosClient = new KerberosClient(client.getBus());
      kerberosClient.setServiceName("bob@service.ws.apache.org");
      kerberosClient.setContextName("alice");
      cxfEndpoint.put("ws-security.kerberos.client", kerberosClient);     
   }
}
