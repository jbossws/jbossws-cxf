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
package org.jboss.test.ws.jaxws.jbws1190;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 If the implementation bean does not implement a service endpoint interface and
 there are no @WebMethod annotations in the implementation bean (excluding
 @WebMethod annotations used to exclude inherited @WebMethods), all public
 methods other than those inherited from java.lang.Object will be exposed as Web
 Service operations, subject to the inheritance rules specified in Common
 Annotations for the Java Platform [12], section 2.1.
 */
@WebService(serviceName = "ConfidentialService", targetNamespace = "http://org.jboss/test/ws/jbws1190")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class ConfidentialEndpoint
{
   // Intentionally no @WebMethod, see above
   public String helloWorld(final String message)
   {
      return message;
   }
}
