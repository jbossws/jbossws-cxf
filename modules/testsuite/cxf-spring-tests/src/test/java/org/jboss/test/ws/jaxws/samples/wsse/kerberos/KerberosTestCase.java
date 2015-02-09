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

import java.io.File;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.policy.WSPolicyFeature;
import org.apache.cxf.ws.security.kerberos.KerberosClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.ws.jaxws.samples.wsse.kerberos.contract.DoubleItPortType;
import org.jboss.wsf.test.JBossWSTest;
import org.jboss.wsf.test.JBossWSTestHelper;
import org.jboss.wsf.test.WrapThreadContextClassLoader;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
 
/**
 * This test is ignored. Please remove @Ignore to enable this test.  
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


@RunWith(Arquillian.class)
@Ignore("This test requires manually setup KDC")
public class KerberosTestCase extends JBossWSTest
{
  
   private static final String namespace = "http://www.example.org/contract/DoubleIt";

   @Deployment(name="jaxws-samples-wsse-kerberos", testable = false)
   public static WebArchive createDeployment1() {
      WebArchive archive = ShrinkWrap.create(WebArchive.class, "jaxws-samples-wsse-kerberos.war");
         archive
               .setManifest(new StringAsset("Manifest-Version: 1.0\n"
                     + "Dependencies: org.apache.ws.security\n"))
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/cxf.xml"), "cxf.xml")
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.kerberos.DoubleItPortTypeImpl.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.kerberos.KeystorePasswordCallback.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.kerberos.contract.DoubleItFault.class)
               .addClass(org.jboss.test.ws.jaxws.samples.wsse.kerberos.contract.DoubleItPortType.class)
               .addPackage("org.jboss.test.ws.jaxws.samples.wsse.kerberos.schema")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/WEB-INF/alice.jks"), "alice.jks")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/WEB-INF/alice.properties"), "alice.properties")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/WEB-INF/bob.jks"), "bob.jks")
               .addAsResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/WEB-INF/bob.properties"), "bob.properties")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/WEB-INF/jbossws-cxf.xml"), "jbossws-cxf.xml")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/WEB-INF/wsdl/DoubleItKerberos.wsdl"), "wsdl/DoubleItKerberos.wsdl")
               .addAsWebInfResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/WEB-INF/wsdl/DoubleItLogical.wsdl"), "wsdl/DoubleItLogical.wsdl")
               .setWebXML(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/WEB-INF/web.xml"));
      return archive;
   }
   
   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void testKerberosSupport() throws Exception
   {
      String serviceURL = "http://" + getServerHost()  + ":" + getServerPort() + "/jaxws-samples-wsse-kerberos/DoubleItKerberosSupport";
      QName servicePort = new QName(namespace, "DoubleItKerberosSupportingPort");

      QName serviceName = new QName(namespace, "DoubleItService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      DoubleItPortType proxy = (DoubleItPortType)service.getPort(servicePort, DoubleItPortType.class);
      setupKerberosSupport(proxy);
      assertEquals(20, proxy.doubleIt(10));
   }


   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void testKerberosTransport() throws Exception
   {
      final int serverPort = getServerPort();
      final int serverSecurePort = serverPort + 363; //8080 + 363 = 8443

      String serviceURL = "https://" + getServerHost() + ":" + serverSecurePort + "/jaxws-samples-wsse-kerberos/DoubleItKerberosTransport";
      QName servicePort = new QName(namespace, "DoubleItKerberosTransportPort");
      QName serviceName = new QName(namespace, "DoubleItService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      DoubleItPortType proxy = (DoubleItPortType)service.getPort(servicePort, DoubleItPortType.class);
      setupKerberosTransport(proxy);
      assertEquals(20, proxy.doubleIt(10));
   }


   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void testKerberosSymmetricSupporting() throws Exception
   {
      String serviceURL = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-samples-wsse-kerberos/DoubleItKerberosOverSymmetricSupporting";
      QName servicePort = new QName(namespace, "DoubleItKerberosSymmetricSupportingPort");
      QName serviceName = new QName(namespace, "DoubleItService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      DoubleItPortType proxy = (DoubleItPortType)service.getPort(servicePort, DoubleItPortType.class);
      setupSymmetricSupporting(proxy);
      assertEquals(20, proxy.doubleIt(10));
   }


   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void testKerberosAsymmetric() throws Exception
   {
      String serviceURL = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-samples-wsse-kerberos/DoubleItKerberosAsymmetric";
      QName servicePort = new QName(namespace, "DoubleItKerberosAsymmetricPort");
      QName serviceName = new QName(namespace, "DoubleItService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      DoubleItPortType proxy = (DoubleItPortType)service.getPort(servicePort, DoubleItPortType.class);
      setupAsymmetric(proxy);
      assertEquals(20, proxy.doubleIt(10));
   }

   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void testKerberosOverAsymmetricSignedEncrypted() throws Exception
   {
      String serviceURL = "http://" + getServerHost()  + ":" + getServerPort() + "/jaxws-samples-wsse-kerberos/DoubleItKerberosOverAsymmetricSignedEncrypted";
      QName servicePort = new QName(namespace, "DoubleItKerberosAsymmetricSignedEncryptedPort");
      QName serviceName = new QName(namespace, "DoubleItService");
      URL wsdlURL = new URL(serviceURL + "?wsdl");
      Service service = Service.create(wsdlURL, serviceName);
      DoubleItPortType proxy = (DoubleItPortType)service.getPort(servicePort, DoubleItPortType.class);
      setupAsymmetricSignedEncrypted(proxy);
      assertEquals(20, proxy.doubleIt(10));
   }

   @Test
   @RunAsClient
   @WrapThreadContextClassLoader
   public void testKerberosKerberosSymmetric() throws Exception
   {
      String serviceURL = "http://" + getServerHost() + ":" + getServerPort() + "/jaxws-samples-wsse-kerberos/DoubleItKerberosSymmetric";
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
      KerberosClient kerberosClient = new KerberosClient();
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
      KerberosClient kerberosClient = new KerberosClient();
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
      KerberosClient kerberosClient = new KerberosClient();
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
      KerberosClient kerberosClient = new KerberosClient();
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
      KerberosClient kerberosClient = new KerberosClient();
      kerberosClient.setServiceName("bob@service.ws.apache.org");
      kerberosClient.setContextName("alice");
      cxfEndpoint.put("ws-security.kerberos.client", kerberosClient);     
   }
   
   @Override
   protected String getClientJarPaths() {
      return JBossWSTestHelper.writeToFile(new JBossWSTestHelper.JarDeployment("jaxws-samples-wsse-kerberos-client.jar") { {
         archive
               .addManifest()
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos//cxf.xml"), "cxf.xml")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/META-INF/alice.jks"), "alice.jks")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/META-INF/alice.properties"), "alice.properties")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/META-INF/bob.jks"), "bob.jks")
               .addAsManifestResource(new File(JBossWSTestHelper.getTestResourcesDir() + "/jaxws/samples/wsse/kerberos/META-INF/bob.properties"), "bob.properties");
         }
      });
   }
   
}
