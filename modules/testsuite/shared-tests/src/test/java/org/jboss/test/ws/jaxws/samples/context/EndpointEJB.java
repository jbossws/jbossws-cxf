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
package org.jboss.test.ws.jaxws.samples.context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;

import javax.annotation.Resource;
import javax.annotation.security.RolesAllowed;
import javax.ejb.Stateless;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.jws.soap.SOAPBinding.Style;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.WebServiceException;
import jakarta.xml.ws.handler.MessageContext;

import org.jboss.ejb3.annotation.SecurityDomain;
import org.jboss.ws.common.DOMWriter;
import org.jboss.ws.api.util.DOMUtils;
import org.jboss.ws.api.annotation.AuthMethod;
import org.jboss.ws.api.annotation.TransportGuarantee;
import org.jboss.ws.api.annotation.WebContext;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

@Stateless
@SOAPBinding(style = Style.RPC)
@WebService
(
   name = "Endpoint",
   serviceName="EndpointService",
   targetNamespace = "http://org.jboss.ws/jaxws/context"
)
@WebContext
(
   contextRoot = "/jaxws-samples-context",
   urlPattern = "/*",
   authMethod = AuthMethod.BASIC,
   transportGuarantee = TransportGuarantee.NONE,
   secureWSDLAccess = false
)

// [JBWS-1339] @Security domain vs. <security-domain> 
@SecurityDomain("JBossWS")
@RolesAllowed("friend")
public class EndpointEJB
{
   @Resource
   WebServiceContext wsCtx;

   @WebMethod
   public String testGetMessageContext()
   {
      MessageContext msgContext = (MessageContext)wsCtx.getMessageContext();
      return msgContext == null ? "fail" : "pass";
   }

   @WebMethod
   public String testMessageContextProperties()
   {
      MessageContext msgContext = (MessageContext)wsCtx.getMessageContext();
      if (msgContext == null)
         return "fail";

      // Check standard jaxws properties
      Object wsdl = msgContext.get(MessageContext.WSDL_DESCRIPTION);
      QName service = (QName)msgContext.get(MessageContext.WSDL_SERVICE);
      QName portType = (QName)msgContext.get(MessageContext.WSDL_INTERFACE);
      QName port = (QName)msgContext.get(MessageContext.WSDL_PORT);
      QName operation = (QName)msgContext.get(MessageContext.WSDL_OPERATION);
      
      if (!service.equals(new QName("http://org.jboss.ws/jaxws/context", "EndpointService")))
         throw new WebServiceException("Invalid qname: " + service);
      if (!portType.equals(new QName("http://org.jboss.ws/jaxws/context", "Endpoint")))
         throw new WebServiceException("Invalid qname: " + portType);
      if (!port.equals(new QName("http://org.jboss.ws/jaxws/context", "EndpointPort")))
         throw new WebServiceException("Invalid qname: " + port);
      if (!operation.equals(new QName("http://org.jboss.ws/jaxws/context", "testMessageContextProperties")))
         throw new WebServiceException("Invalid qname: " + operation);
      
      try
      {
         Element root = null;
         if (wsdl instanceof InputSource)
         {
            root = DOMUtils.parse((InputSource)wsdl, getDocumentBuilder());
         }
         else if (wsdl instanceof URI)
         {
            root = DOMUtils.parse(((URI)wsdl).toURL().openStream(), getDocumentBuilder());
         }
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         new DOMWriter(out).setPrettyprint(true).print(root);
         if (!out.toString().contains("http://schemas.xmlsoap.org/wsdl/"))
         {
            throw new WebServiceException("Not a wsdl");
         }
      }
      catch (IOException ex)
      {
         throw new WebServiceException("Cannot parse MessageContext.WSDL_DESCRIPTION", ex);
      }
      
      return "pass";
   }

   @WebMethod
   public String testGetUserPrincipal()
   {
      Principal principal = wsCtx.getUserPrincipal();
      return principal.getName();
   }

   @WebMethod
   public boolean testIsUserInRole(String role)
   {
      return wsCtx.isUserInRole(role);
   }
   
   private DocumentBuilder getDocumentBuilder()
   {
      DocumentBuilderFactory factory = null;
      try
      {
         factory = DocumentBuilderFactory.newInstance();
         factory.setValidating(false);
         factory.setNamespaceAware(true);
         factory.setExpandEntityReferences(false);
         factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
         DocumentBuilder builder = factory.newDocumentBuilder();
         return builder;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Unable to create document builder", e);
      }
   }
}
