/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.test.ws.jaxws.samples.wsse.kerberos;

import java.security.Principal;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;

import org.jboss.test.ws.jaxws.samples.wsse.kerberos.contract.DoubleItFault;
import org.jboss.test.ws.jaxws.samples.wsse.kerberos.contract.DoubleItPortType;

@WebService(targetNamespace = "http://www.example.org/contract/DoubleIt", 
             serviceName = "DoubleItService",
             portName = "DoubleItKerberosSupportingPort",
             wsdlLocation = "WEB-INF/wsdl/DoubleItKerberos.wsdl",
             endpointInterface = "org.jboss.test.ws.jaxws.samples.wsse.kerberos.contract.DoubleItPortType")

public class DoubleItPortTypeImpl implements DoubleItPortType
{

   @Resource
   WebServiceContext wsContext;

   public int doubleIt(int numberToDouble) throws DoubleItFault
   {
      Principal pr = wsContext.getUserPrincipal();

      if (pr == null)
      {
         throw new DoubleItFault("Principal must not be null");
      }

      if (pr.getName() == null)
      {
         throw new DoubleItFault("Principal.getName() must not return null");
      }
      return numberToDouble * 2;
   }

}
