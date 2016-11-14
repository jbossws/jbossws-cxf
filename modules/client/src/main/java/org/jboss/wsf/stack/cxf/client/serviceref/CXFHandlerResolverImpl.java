/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
package org.jboss.wsf.stack.cxf.client.serviceref;

import static org.jboss.wsf.stack.cxf.Loggers.DEPLOYMENT_LOGGER;
import static org.jboss.wsf.stack.cxf.Messages.MESSAGES;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.PortInfo;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.handler.HandlerChainBuilder;
import org.apache.cxf.jaxws.handler.types.PortComponentHandlerType;
import org.jboss.logging.Logger;
import org.jboss.ws.common.DOMUtils;
import org.jboss.wsf.spi.metadata.ParserConstants;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Handler resolver for CXF integration
 * @author richard.opalka@jboss.com
 */
final class CXFHandlerResolverImpl extends HandlerChainBuilder implements HandlerResolver
{
   private static Logger log = Logger.getLogger(CXFHandlerResolverImpl.class);

   @SuppressWarnings("rawtypes")
   private final Map<PortInfo, List<Handler>> handlerMap = new HashMap<PortInfo, List<Handler>>();
   private final String handlerFile;
   private final Class<?> clazz;
   private final ClassLoader classLoader;

   public CXFHandlerResolverImpl(Bus bus, String handlerFile, Class<?> clazz)
   {
      super(bus);
      this.handlerFile = handlerFile;
      this.clazz = clazz;
      this.classLoader = clazz.getClassLoader();
   }
   
   @SuppressWarnings("rawtypes")
   public List<Handler> getHandlerChain(PortInfo portInfo)
   {
      synchronized (handlerMap)
      {
         List<Handler> handlerChain = handlerMap.get(portInfo);
         if (handlerChain == null) {
            QName portQName = portInfo.getPortName();
            QName serviceQName = portInfo.getServiceName();
            String bindingId = portInfo.getBindingID();
            handlerChain = createHandlerChain(portInfo, portQName, serviceQName, bindingId);
            handlerMap.put(portInfo, handlerChain);
         }
         return handlerChain;
      }
   }
   
   private InputStream getInputStream()
   {
      return this.getInputStream(this.handlerFile, this.clazz);
   }
   
   @SuppressWarnings("rawtypes")
   protected List<Handler> createHandlerChain(PortInfo portInfo, QName portQName, QName serviceQName, String bindingID) {
      List<Handler> chain = new ArrayList<Handler>();
      InputStream is = getInputStream();
      try {

         if (is == null) {
            throw MESSAGES.handlerConfigFileNotFound(handlerFile);
         }

         Element el = DOMUtils.parse(is, Holder.builder);
         if (!ParserConstants.JAVAEE_NS.equals(el.getNamespaceURI()) 
               || !ParserConstants.HANDLER_CHAINS.equals(el.getLocalName())) {
            throw MESSAGES.differentElementExpected(handlerFile, "{" + ParserConstants.JAVAEE_NS + "}"
                  + ParserConstants.HANDLER_CHAINS, "{" + el.getNamespaceURI() + "}" + el.getLocalName());
         }
         Node node = el.getFirstChild();
         while (node != null) {
            if (node instanceof Element) {
               el = (Element)node;
               if (!el.getNamespaceURI().equals(ParserConstants.JAVAEE_NS) 
                     || !el.getLocalName().equals(ParserConstants.HANDLER_CHAIN)) {
                  throw MESSAGES.differentElementExpected(handlerFile, "{" + ParserConstants.JAVAEE_NS + "}"
                        + ParserConstants.HANDLER_CHAIN, "{" + el.getNamespaceURI() + "}" + el.getLocalName());
               }
               processHandlerChainElement(el, chain, portQName, serviceQName, bindingID);
            }
            node = node.getNextSibling();
         }
      } catch (WebServiceException e) {
         throw e;
      } catch (Exception e) {
         throw MESSAGES.noHandlerChainFound(handlerFile, e);
      }
      assert chain != null;
      return sortHandlers(chain);
   }

   private void processHandlerChainElement(Element el, @SuppressWarnings("rawtypes") List<Handler> chain,
         QName portQName, QName serviceQName, String bindingID) {
      Node node = el.getFirstChild();
      while (node != null) {
         Node cur = node;
         node = node.getNextSibling();            
         if (cur instanceof Element) {
            el = (Element)cur;
            if (!el.getNamespaceURI().equals(ParserConstants.JAVAEE_NS)) {
               String xml = "{" + el.getNamespaceURI() + "}" + el.getLocalName();
               throw MESSAGES.invalidElementInHandler(handlerFile, xml);
            }
            String name = el.getLocalName();
            if ("port-name-pattern".equals(name)) {
               if (!patternMatches(el, portQName)) {
                  return;
               }
            } else if ("service-name-pattern".equals(name)) {
               if (!patternMatches(el, serviceQName)) {
                  return;
               }
            } else if ("protocol-bindings".equals(name)) {
               if (!protocolMatches(el, bindingID)) {
                  return;
               }
            } else if ("handler".equals(name)) {
               processHandlerElement(el, chain);
            }
         }
      }        
   }
   private boolean protocolMatches(Element el, String id) {
      if (id == null) {
         return true;
      }
      String name = el.getTextContent().trim();
      if ("##SOAP11_HTTP".equals(name)) {
         return "http://schemas.xmlsoap.org/wsdl/soap/http".contains(id)
         || "http://schemas.xmlsoap.org/soap/".contains(id);
      } else if ("##SOAP11_HTTP_MTOM".equals(name)) {
         return "http://schemas.xmlsoap.org/wsdl/soap/http?mtom=true".contains(id)
         || "http://schemas.xmlsoap.org/soap/?mtom=true".contains(id);
      } else if ("##SOAP12_HTTP".equals(name)) {
         return "http://www.w3.org/2003/05/soap/bindings/HTTP/".contains(id)
         || "http://schemas.xmlsoap.org/wsdl/soap12/".contains(id);
      } else if ("##SOAP12_HTTP_MTOM".equals(name)) {
         return "http://www.w3.org/2003/05/soap/bindings/HTTP/?mtom=true".contains(id)
         || "http://schemas.xmlsoap.org/wsdl/soap12/?mtom=true".contains(id);
      } else if ("##XML_HTTP".equals(name)) {
         name = "http://www.w3.org/2004/08/wsdl/http";
      }
      return name.contains(id);
   }
   private boolean patternMatches(Element el, QName comp) {
      if (comp == null) {
         return true;
      }
      final String namePattern = el.getTextContent().trim();
      if ("*".equals(namePattern)) {
         return true;
      }
      final int idx = namePattern.indexOf(':');
      if (idx < 0) {
         throw MESSAGES.notAQNamePattern(handlerFile, namePattern);
      }
      String localPart = namePattern.substring(idx + 1, namePattern.length());
      String pfx = namePattern.substring(0, idx);
      String ns = el.lookupNamespaceURI(pfx);
      if (ns == null) {
         ns = pfx;
      }
      if (!ns.equals(comp.getNamespaceURI())) {
         return false;
      }
      if (localPart.contains("*")) {
         //wildcard pattern matching
         return Pattern.matches(localPart, comp.getLocalPart());
      } else if (!localPart.equals(comp.getLocalPart())) {
         return false;
      }
      return true;
   }

   @SuppressWarnings("rawtypes")
   public List<Handler> sortHandlers(List<Handler> handlers) {

      final int size = handlers.size();
      List<Handler> logicalHandlers = new ArrayList<Handler>(size);
      List<Handler> protocolHandlers = new ArrayList<Handler>(Math.min(10, size));

      for (Handler handler : handlers) {
         if (handler instanceof LogicalHandler) {
            logicalHandlers.add(handler);
         } else {
            protocolHandlers.add(handler);
         }
      }

      if (!protocolHandlers.isEmpty()) {
         logicalHandlers.addAll(protocolHandlers);
      }
      return logicalHandlers;
   }

   private InputStream getInputStream(String filename, Class<?> wsClass)
   {
      URL fileURL = null;
      // Try the filename as URL
      try
      {
         fileURL = new URL(filename);
      }
      catch (MalformedURLException ex)
      {
         // ignore
         log.trace(ex);
      }

      // Try the filename as File
      if (fileURL == null)
      {
         try
         {
            File file = new File(filename);
            if (file.exists())
               fileURL = file.toURI().toURL();
         }
         catch (MalformedURLException e)
         {
            // ignore
            log.trace(e);
         }
      }

      // Try the filename as Resource
      if (fileURL == null)
      {
         fileURL = wsClass.getClassLoader().getResource(filename);
      }

      // Try the filename relative to class
      if (fileURL == null)
      {
         String filepath = filename;
         String packagePath = wsClass.getPackage().getName().replace('.', '/');
         String resourcePath = packagePath + "/" + filepath;
         while (filepath.startsWith("../"))
         {
            packagePath = packagePath.substring(0, packagePath.lastIndexOf("/"));
            filepath = filepath.substring(3);
            resourcePath = packagePath + "/" + filepath;
         }
         fileURL = wsClass.getClassLoader().getResource(resourcePath);
      }

      if (fileURL == null)
         throw MESSAGES.cannotResolveHandlerFile(filename, wsClass.getName());

      try
      {
         return fileURL.openStream();
      }
      catch (IOException ioe)
      {
         throw new WebServiceException(ioe);
      }
   }
   
   private void processHandlerElement(Element el, @SuppressWarnings("rawtypes") List<Handler> chain) {
      try {
          JAXBContext ctx = Holder.context;
          PortComponentHandlerType pt = ctx.createUnmarshaller()
              .unmarshal(el, PortComponentHandlerType.class).getValue();
          chain.addAll(buildHandlerChain(pt, classLoader));
      } catch (JAXBException e) {
         DEPLOYMENT_LOGGER.unableToProcessHandlerElement(el, e);
      }
  }

   private static class Holder {
      
      static final DocumentBuilder builder = getDocumentBuilder();
      static final JAXBContext context = getContextForPortComponentHandlerType();
      
      private static DocumentBuilder getDocumentBuilder()
      {
         final ClassLoader classLoader = SecurityActions.getContextClassLoader();
         SecurityActions.setContextClassLoader(CXFHandlerResolverImpl.class.getClassLoader());
         DocumentBuilder builder;
         try
         {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            factory.setExpandEntityReferences(false);
            try
            {
               factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            }
            catch (ParserConfigurationException pce)
            {
               DEPLOYMENT_LOGGER.error(pce);
            }
            builder = DOMUtils.newDocumentBuilder(factory);
         }
         finally
         {
            SecurityActions.setContextClassLoader(classLoader);
         }
         return builder;
      }
      
      private static JAXBContext getContextForPortComponentHandlerType() {
         JAXBContext context = null;
         try {
            context = JAXBContext.newInstance(PortComponentHandlerType.class);
         } catch (JAXBException e) {
            DEPLOYMENT_LOGGER.error(e);
         }
         return context;
      }
   }
}