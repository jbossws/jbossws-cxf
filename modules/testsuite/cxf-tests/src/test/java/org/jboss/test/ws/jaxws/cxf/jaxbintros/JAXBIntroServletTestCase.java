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
package org.jboss.test.ws.jaxws.cxf.jaxbintros;

import java.net.URL;

import junit.framework.Test;

import org.jboss.ws.common.IOUtils;
import org.jboss.wsf.test.JBossWSCXFTestSetup;
import org.jboss.wsf.test.JBossWSTest;

/**
 * Test the JAXBIntroduction features.
 * 
 * jaxb-intros.xml can reside under META-INF or WEB-INF and should be
 * picked up by JAXBIntroduction deployment aspect on server side.
 *
 * @author alessio.soldano@jboss.com
 */
public class JAXBIntroServletTestCase extends JBossWSTest
{

   public static Test suite()
   {
      return new JBossWSCXFTestSetup(JAXBIntroServletTestCase.class, "jaxws-cxf-jaxbintros.jar, jaxws-cxf-jaxbintros-client.war");
   }
   
   /**
    * Both client and server side use plain UserType class but have jaxbintros in place to deal with customizations
    *
    * @throws Exception
    */
   public void testEndpoint() throws Exception
   {
      assertEquals("1", runTestInContainer("testEndpoint"));
   }

   /**
    * Client side uses the annotated user type class, server side uses the plain one but has jaxbintros in place
    *
    * @throws Exception
    */
   public void testAnnotatedUserEndpoint() throws Exception
   {
      assertEquals("1", runTestInContainer("testAnnotatedUserEndpoint"));
   }
   
   private String runTestInContainer(String test) throws Exception
   {
      URL url = new URL("http://" + getServerHost()
            + ":8080/jaxws-cxf-jaxbintros-client?path=/jaxws-cxf-jaxbintros/EndpointService&method=" + test
            + "&helper=" + Helper.class.getName());
      return IOUtils.readAndCloseStream(url.openStream());
   }
}
