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

import org.jboss.wsf.spi.metadata.jms.JMSDescriptorParser;
import org.jboss.wsf.spi.metadata.jms.JMSEndpointsMetaData;

/**
 * JMS descriptor processor implementation.
 * 
 * @author <a href="ropalka@redhat.com">Richard Opalka</a>
 */
public final class JMSDescriptorParserImpl implements JMSDescriptorParser
{
   private String descriptorName;
   
   @Override
   public String getDescriptorName()
   {
      return this.descriptorName;
   }

   /**
    * Invoked via MC.
    * @param descriptorName
    */
   public void setDescriptorName(final String descriptorName)
   {
      this.descriptorName = descriptorName;
   }

   @Override
   public JMSEndpointsMetaData parse(URL url)
   {
      return JMSEndpointsFactory.load(url);
   }
}
