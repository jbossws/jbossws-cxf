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
package org.jboss.test.ws.jaxws.cxf.jbws3124;

import java.io.StringWriter;

import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.jboss.wsf.test.JBossWSTest;

/**
 * See [JBWS-2942] Do not add empty Metadata tag to EndpointReference.
 *
 * @author <a href="mailto:sberyozk@redhat.com">Sergey Beryozkin</a>
 */
public final class JBWS3124TestCase extends JBossWSTest
{

   public void testEmptyMetadataDropped() 
   {
      W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
      builder.address("http://bar");
      W3CEndpointReference epr = builder.build();
      StringWriter writer = new StringWriter();
      epr.writeTo(new StreamResult(writer));
      String eprString = writer.toString();
      assertTrue(eprString.contains("EndpointReference"));
      assertFalse(eprString.contains("Metadata"));
   }
   
}
