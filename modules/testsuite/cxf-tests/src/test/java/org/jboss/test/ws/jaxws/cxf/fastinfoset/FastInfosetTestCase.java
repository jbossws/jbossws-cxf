/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.cxf.fastinfoset;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import junit.framework.Test;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

public class FastInfosetTestCase extends JBossWSTest
{
   private String endpointURl = "http://" + getServerHost() + ":8080/jaxws-cxf-fastinfoset/HelloWorldService/HelloWorldImpl";


   public static Test suite()
   {
      return new JBossWSCXFTestSetup(FastInfosetTestCase.class, "jaxws-cxf-fastinfoset.war");
   }
   
   
   public void testInfoset() throws Exception
   {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      ByteArrayOutputStream in = new ByteArrayOutputStream();
      PrintWriter pwIn = new PrintWriter(in);
      PrintWriter pwOut = new PrintWriter(out);
      Bus bus = BusFactory.newInstance().createBus();
      BusFactory.setThreadDefaultBus(bus);
      try {
         bus.getInInterceptors().add(new LoggingInInterceptor(pwIn));
         bus.getOutInterceptors().add(new LoggingOutInterceptor(pwOut));
   
         URL wsdlURL = new URL(endpointURl + "?wsdl");
         QName serviceName = new QName("http://org.jboss.ws/jaxws/cxf/fastinfoset", "HelloWorldService");
         Service service = Service.create(wsdlURL, serviceName);
         QName portQName = new QName("http://org.jboss.ws/jaxws/cxf/fastinfoset", "HelloWorldImplPort");
         HelloWorld port = (HelloWorld) service.getPort(portQName, HelloWorld.class);
         assertEquals("helloworld", port.echo("helloworld"));
         assertTrue("request is expected fastinfoset", out.toString().indexOf("application/fastinfoset") > -1);
         assertTrue("response is expected fastinfoset", in.toString().indexOf("application/fastinfoset") > -1);
      } finally {
         bus.shutdown(true);
         pwOut.close();
         pwIn.close();
      }

   }
   

}
