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
package org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl;

import java.util.logging.Logger;

@javax.jws.WebService(
   serviceName = "JBWS3792WSService",
   portName = "JBWS3792WSPort",
   targetNamespace = "http://test.jbws3792/",
   wsdlLocation = "http://localhost:8080/jbws3792-external-wsdl/jbws3792.wsdl",
   endpointInterface = "org.jboss.test.ws.jaxws.cxf.jbws3792.wsImpl.JBWS3792WS")

public class JBWS3792WSImpl implements JBWS3792WS {

   private static final Logger LOG = Logger.getLogger(JBWS3792WSImpl.class.getName());

   public java.lang.String hello() {
      LOG.info("Executing operation hello");
      try {
         java.lang.String _return = "Hello world!";
         return _return;
      } catch (java.lang.Exception ex) {
         ex.printStackTrace();
         throw new RuntimeException(ex);
      }
   }

}

