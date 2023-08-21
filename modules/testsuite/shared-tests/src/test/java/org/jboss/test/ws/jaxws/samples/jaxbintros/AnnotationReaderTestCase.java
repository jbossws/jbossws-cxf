/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jboss.test.ws.jaxws.samples.jaxbintros;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.util.Map;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.jboss.jaxb.intros.BindingCustomizationFactory;
import org.jboss.wsf.test.JBossWSTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;

/**
 * @author Heiko.Braun@jboss.com
 * @author alessio.soldano@jboss.com
 */
@RunWith(Arquillian.class)
public class AnnotationReaderTestCase extends JBossWSTest
{
   @Test
   @RunAsClient
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

      JAXBElement<UserType> jbe = unmarshaller.unmarshal(new StreamSource(new ByteArrayInputStream(reqString.getBytes())

      ), UserType.class);

      UserType ut = jbe.getValue();
      assertEquals("Kermit", ut.getString());
      assertEquals("The Frog", ut.getQname().getLocalPart());

   }
}
