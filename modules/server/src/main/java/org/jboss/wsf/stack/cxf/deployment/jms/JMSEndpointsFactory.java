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
package org.jboss.wsf.stack.cxf.deployment.jms;

import java.net.URL;

import org.jboss.wsf.spi.metadata.jms.JMSEndpointMetaData;
import org.jboss.wsf.spi.metadata.jms.JMSEndpointsMetaData;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

/**
 * JMS Endpoints OjbectModelFactory
 * 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JMSEndpointsFactory implements ObjectModelFactory
{
   private URL descriptorURL;

   public JMSEndpointsFactory(URL descriptorURL)
   {
      this.descriptorURL = descriptorURL;
   }

   /**
    * This method is called on the factory by the object model builder when the parsing starts.
    *
    * @return the root of the object model.
    */
   public Object newRoot(Object root, UnmarshallingContext navigator, String namespaceURI, String localName,
         Attributes attrs)
   {
      return new JMSEndpointsMetaData(descriptorURL);
   }

   public Object completeRoot(Object root, UnmarshallingContext ctx, String uri, String name)
   {
      return root;
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(JMSEndpointsMetaData endpoints, UnmarshallingContext navigator, String namespaceURI,
         String localName, Attributes attrs)
   {
      if ("endpoint".equals(localName) && isJMSEndpoint(attrs))
      {
         JMSEndpointMetaData endpoint = new JMSEndpointMetaData(endpoints);
         endpoint.setName(attrs.getValue("name"));
         endpoint.setImplementor(attrs.getValue("implementor"));
         endpoint.setEndpointName(attrs.getValue("endpointName"));
         endpoint.setWsdlLocation(attrs.getValue("wsdlLocation"));
         return endpoint;
      }
      else
         return null;
   }
   
   private boolean isJMSEndpoint(final Attributes attrs)
   {
      if (attrs == null)
         return false;
      if (attrs.getValue("transportId") == null)
         return false;
      
      return attrs.getValue("transportId").equals("http://cxf.apache.org/transports/jms");
   }
   
   /**
    * Called when parsing character is complete.
    */
   public void addChild(JMSEndpointsMetaData endpoints, JMSEndpointMetaData endpointMetaData, UnmarshallingContext navigator, String namespaceURI,
         String localName)
   {
      endpoints.addEndpointMetaData(endpointMetaData);
   }
}
