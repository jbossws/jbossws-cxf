/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.jbws2074.usecase5.service;

import jakarta.ejb.Stateless;
import jakarta.jws.WebService;

import org.jboss.logging.Logger;
import org.jboss.ws.api.annotation.EndpointConfig;
import org.jboss.ws.api.annotation.WebContext;

@Stateless
@WebService
(
   name = "EJB3",
   serviceName = "EJB3Service",
   targetNamespace = "http://ws.jboss.org/jbws2074",
   endpointInterface = "org.jboss.test.ws.jaxws.jbws2074.usecase5.service.EJB3Iface"
)
@EndpointConfig(configFile = "endpoint-config.xml", configName = "MyConf")
@WebContext
(
   urlPattern="/Service", 
   contextRoot="/jaxws-jbws2074-usecase5"
)
public class EJB3Impl implements EJB3Iface
{

   private static Logger log = Logger.getLogger(EJB3Impl.class);

   public String echo(String msg)
   {
      log.info("echo: " + msg);
      return msg + ":EJB3Impl";
   }

}
