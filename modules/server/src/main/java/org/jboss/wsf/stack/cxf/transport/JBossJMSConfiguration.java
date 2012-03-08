/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.transport;

import java.util.Properties;

import org.apache.cxf.transport.jms.JMSConfiguration;
import org.apache.cxf.transport.jms.JNDIConfiguration;
import org.springframework.jndi.JndiTemplate;

/**
 * A JBoss AS version of Apache CXF JMSConfiguration
 * 
 * @see org.apache.cxf.binding.soap.SoapTransportFactory.SoapEndpointInfo
 * 
 * @author alessio.soldano@jboss.com
 * @since 07-Mar-2012
 * 
 */
public class JBossJMSConfiguration extends JMSConfiguration
{
   @Override
   public void setJndiTemplate(JndiTemplate jndiTemplate)
   {
      //erase props to have correct JBoss AS server side jndi lookup
      jndiTemplate.setEnvironment(new Properties());
      super.setJndiTemplate(jndiTemplate);
   }

   @Override
   public void setJndiConfig(JNDIConfiguration jndiConfig)
   {
      //erase props and set correct connection factory for JBoss AS server side connection lookup
      jndiConfig.setEnvironment(new Properties());
      jndiConfig.setConnectionPassword(null);
      jndiConfig.setConnectionUserName(null);
      jndiConfig.setJndiConnectionFactoryName("java:/ConnectionFactory");
      super.setJndiConfig(jndiConfig);
   }

}
