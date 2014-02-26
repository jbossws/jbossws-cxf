/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.wsf.stack.cxf.jaspi.config;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ServerAuthContext;
import javax.security.auth.message.module.ServerAuthModule;
import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.apache.cxf.Bus;
import org.apache.cxf.common.util.StringUtils;
import org.jboss.security.auth.callback.JBossCallbackHandler;
import org.jboss.security.auth.container.config.AuthModuleEntry;
import org.jboss.security.auth.login.JASPIAuthenticationInfo;
import org.jboss.security.auth.message.config.JBossServerAuthConfig;
import org.jboss.security.config.ControlFlag;
import org.jboss.security.plugins.ClassLoaderLocator;
import org.jboss.security.plugins.ClassLoaderLocatorFactory;

/**
 * JBossWS ServerAuthConfig implentation to obtain JBossWSServerAuthContext
 * @see org.jboss.wsf.stack.cxf.jaspi.config.JBossWSServerAuthContext
 * @author <a href="mailto:ema@redhat.com">Jim Ma</a>
 */
public class JBossWSServerAuthConfig extends JBossServerAuthConfig
{
   private final CallbackHandler callbackHandler = new JBossCallbackHandler();

   @SuppressWarnings("rawtypes")
   private final List modules = new ArrayList();

   @SuppressWarnings("rawtypes")
   public JBossWSServerAuthConfig(String layer, String appContext, CallbackHandler handler, Map properties)
   {
      super(layer, appContext, handler, properties);
   }

   @SuppressWarnings(
   {"rawtypes", "unchecked"})
   public ServerAuthContext getAuthContext(String authContextID, Subject serviceSubject, Map properties)
         throws AuthException
   {
      List<ControlFlag> controlFlags = new ArrayList<ControlFlag>();

      Map<String, Map> mapOptionsByName = new HashMap<String, Map>();
      JASPIAuthenticationInfo jai = (JASPIAuthenticationInfo) properties.get("jaspi-policy");
      AuthModuleEntry[] amearr = jai.getAuthModuleEntry();

      ClassLoader moduleCL = null;
      String jbossModule = jai.getJBossModuleName();
      if (jbossModule != null && !jbossModule.isEmpty())
      {
         ClassLoaderLocator locator = ClassLoaderLocatorFactory.get();
         if (locator != null)
            moduleCL = locator.get(jbossModule);
      }

      for (AuthModuleEntry ame : amearr)
      {
         if (ame.getLoginModuleStackHolderName() != null)
         {
            try
            {
               mapOptionsByName.put(ame.getAuthModuleName(), ame.getOptions());
               controlFlags.add(ame.getControlFlag());
               ServerAuthModule sam = this.createSAM(moduleCL, ame.getAuthModuleName(),
                     ame.getLoginModuleStackHolderName());

               Map options = new HashMap();
               Bus bus = (Bus) properties.get(Bus.class);
               options.put(Bus.class, bus);
               javax.xml.ws.Endpoint endpoint = (javax.xml.ws.Endpoint) properties.get(javax.xml.ws.Endpoint.class);
               options.put(javax.xml.ws.Endpoint.class, endpoint);

               sam.initialize(null, null, callbackHandler, options);
               modules.add(sam);
            }
            catch (Exception e)
            {
               throw new AuthException(e.getLocalizedMessage());
            }
         }
         else
         {
            try
            {
               mapOptionsByName.put(ame.getAuthModuleName(), ame.getOptions());
               controlFlags.add(ame.getControlFlag());
               ServerAuthModule sam = this.createSAM(moduleCL, ame.getAuthModuleName());

               Map options = new HashMap();
               sam.initialize(null, null, callbackHandler, options);
               modules.add(sam);
            }
            catch (Exception e)
            {
               throw new AuthException(e.getLocalizedMessage());
            }
         }
      }

      JBossWSServerAuthContext serverAuthContext = new JBossWSServerAuthContext(modules, mapOptionsByName,
            this.callbackHandler);
      serverAuthContext.setControlFlags(controlFlags);
      return serverAuthContext;
   }

   @SuppressWarnings("rawtypes")
   public String getAuthContextID(MessageInfo messageInfo)
   {
      SOAPMessage request = (SOAPMessage) messageInfo.getRequestMessage();
      if (request == null)
      {
         return null;
      }
      String authContext = null;
      MimeHeaders headers = request.getMimeHeaders();
      if (headers != null)
      {
         String[] soapActions = headers.getHeader("SOAPAction");
         if (soapActions != null && soapActions.length > 0)
         {
            authContext = soapActions[0];
            if (!StringUtils.isEmpty(authContext))
            {
               return authContext;
            }
         }
      }

      SOAPPart soapMessage = request.getSOAPPart();
      if (soapMessage != null)
      {
         try
         {
            SOAPEnvelope envelope = soapMessage.getEnvelope();
            if (envelope != null)
            {
               SOAPBody body = envelope.getBody();
               if (body != null)
               {

                  Iterator it = body.getChildElements();
                  while (it.hasNext())
                  {
                     Object o = it.next();
                     if (o instanceof SOAPElement)
                     {
                        QName name = ((SOAPElement) o).getElementQName();
                        return name.getLocalPart();

                     }
                  }
               }
            }
         }
         catch (SOAPException se)
         {
            //ignore;
         }
      }

      return null;
   }

   public boolean isProtected()
   {
      throw new UnsupportedOperationException();
   }

   @SuppressWarnings(
   {"unchecked", "rawtypes"})
   private ServerAuthModule createSAM(ClassLoader moduleCL, String name) throws Exception
   {
      Class clazz = SecurityActions.loadClass(moduleCL, name);
      Constructor ctr = clazz.getConstructor(new Class[0]);
      return (ServerAuthModule) ctr.newInstance(new Object[0]);
   }

   @SuppressWarnings(
   {"unchecked", "rawtypes"})
   private ServerAuthModule createSAM(ClassLoader moduleCL, String name, String lmshName) throws Exception
   {
      Class clazz = SecurityActions.loadClass(moduleCL, name);
      Constructor ctr = clazz.getConstructor(new Class[] {String.class});
      return (ServerAuthModule) ctr.newInstance(new Object[] {lmshName});
   }

}