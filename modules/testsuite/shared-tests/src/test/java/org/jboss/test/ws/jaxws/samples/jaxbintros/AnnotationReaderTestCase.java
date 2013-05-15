/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.jaxbintros;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.jboss.jaxb.intros.BindingCustomizationFactory;
import org.jboss.wsf.test.JBossWSTest;

/**
 * @author Heiko.Braun@jboss.com
 * @author alessio.soldano@jboss.com
 */
public class AnnotationReaderTestCase extends JBossWSTest
{
   public void testUnmarshal() throws Exception
   {
      String reqString = 
         "   <ns1:user xmlns:ns1='http://org.jboss.ws/provider' string='Kermit'>" + 
         "      <qname>The Frog</qname>" + 
         "    </ns1:user>";

      Map<String, Object> jaxbConfig = BindingCustomizationFactory.getBindingCustomization(new FileInputStream(getResourceFile(
            "jaxws/samples/jaxbintros/META-INF/jaxb-intros.xml").getPath()), "http://org.jboss.ws/provider");
      
      JAXBContext jaxbContext = JAXBContext.newInstance(new Class[] { UserType.class }, jaxbConfig);
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

      JAXBElement jbe = unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(reqString.getBytes())

      ), UserType.class);

      UserType ut = (UserType)jbe.getValue();
      assertEquals("Kermit", ut.getString());
      assertEquals("The Frog", ut.getQname().getLocalPart());

   }
}
