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
package org.jboss.test.ws.publish;

import jakarta.xml.soap.SOAPBody;
import jakarta.xml.soap.SOAPMessage;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.ServiceMode;
import jakarta.xml.ws.WebServiceProvider;

import org.jboss.logging.Logger;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@WebServiceProvider
@ServiceMode(value = Service.Mode.MESSAGE)
public class EndpointImpl5 implements Provider<SOAPMessage>
{
   // Provide logging
   private static Logger log = Logger.getLogger(EndpointImpl5.class);

   public SOAPMessage invoke(SOAPMessage request)
   {
      log.info("echo (5): " + request);
      try {
         SOAPBody sb = request.getSOAPBody();
         NodeList nl = sb.getElementsByTagName("arg0");
         if (nl.getLength() != 1) {
            throw new IllegalArgumentException("Unexpected input!");
         }
         Node ret = sb.getOwnerDocument().createElement("return");
         Node arg0 = nl.item(0);
         ret.appendChild(arg0.getFirstChild().cloneNode(true));
         Node parent = arg0.getParentNode();
         parent.removeChild(arg0);
         parent.appendChild(ret);
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
      return request;
   }
}
