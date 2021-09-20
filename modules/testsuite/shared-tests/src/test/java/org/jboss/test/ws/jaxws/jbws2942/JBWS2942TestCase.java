/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2942;

import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;
import jakarta.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.jboss.wsf.test.JBossWSTest;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.container.test.api.RunAsClient;

/**
 * [JBWS-2942] Do not add empty Metadata tag to EndpointReference.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
@RunWith(Arquillian.class)
public final class JBWS2942TestCase extends JBossWSTest
{
   @Test
   @RunAsClient
   public void testEmptyMetadataDropped() 
   {
      W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
      builder.address("http://bar");
      W3CEndpointReference epr = builder.build();
      StringWriter writer = new StringWriter();
      epr.writeTo(new StreamResult(writer));
      String eprString = writer.toString();
      assertTrue(eprString.contains("EndpointReference"));
      assertTrue(eprString.contains("Address"));
      assertFalse(eprString.contains("Metadata"));
   }
   
}
