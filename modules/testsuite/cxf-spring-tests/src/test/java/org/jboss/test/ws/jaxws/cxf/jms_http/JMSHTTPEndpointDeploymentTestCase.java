/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.jms_http;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import javax.naming.Context;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.jms.JMSConduit;
import org.apache.cxf.transport.jms.JNDIConfiguration;
import org.jboss.test.ws.jaxws.cxf.jms.HelloWorld;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Test case for deploying an archive with a JMS (SOAP-over-JMS 1.0) and a HTTP endpoints 
 *
 * @author alessio.soldano@jboss.com
 * @since 10-Jun-2011
 */
public final class JMSHTTPEndpointDeploymentTestCase extends JBossWSTest
{
   public static Test suite()
   {
      return new JBossWSCXFTestSetup(JMSHTTPEndpointDeploymentTestCase.class, "jaxws-cxf-jms-http-deployment-test-servlet.war,jaxws-cxf-jms-http-deployment.war");
   }
   
   public void testJMSEndpointServerSide() throws Exception
   {
      URL url = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-jms-http-deployment-test-servlet");
      BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
      assertEquals("true", br.readLine());
   }
   
   public void testJMSEndpointClientSide() throws Exception
   {
      URL wsdlUrl = getResourceURL("jaxws/cxf/jms_http/WEB-INF/wsdl/HelloWorldService.wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldService");

      Service service = Service.create(wsdlUrl, serviceName);
      HelloWorld proxy = (HelloWorld) service.getPort(new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldImplPort"), HelloWorld.class);
      setupProxy(proxy);
      assertEquals("Hi", proxy.echo("Hi"));
   }
   
   public void testHTTPEndpointClientSide() throws Exception
   {
      URL wsdlUrl = new URL("http://" + getServerHost() + ":8080/jaxws-cxf-jms-http-deployment?wsdl");
      QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/jms", "HelloWorldService");

      Service service = Service.create(wsdlUrl, serviceName);
      HelloWorld proxy = (HelloWorld) service.getPort(new QName("http://org.jboss.ws/jaxws/cxf/jms", "HttpHelloWorldImplPort"), HelloWorld.class);
      assertEquals("(http) Hi", proxy.echo("Hi"));
   }
   
   private void setupProxy(HelloWorld proxy) {
      final String user = "guest";
      final String pwd = "pass";
      JMSConduit conduit = (JMSConduit)ClientProxy.getClient(proxy).getConduit();
      JNDIConfiguration jndiConfig = conduit.getJmsConfig().getJndiConfig();
      jndiConfig.setConnectionUserName(user);
      jndiConfig.setConnectionPassword(pwd);
      Properties props = conduit.getJmsConfig().getJndiTemplate().getEnvironment();
      props.put(Context.SECURITY_PRINCIPAL, user);
      props.put(Context.SECURITY_CREDENTIALS, pwd);
   }
}
