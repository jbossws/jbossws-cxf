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
package org.jboss.wsf.stack.cxf.client.jaspi;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.config.ClientAuthContext;
import javax.security.auth.message.module.ClientAuthModule;

import org.jboss.security.auth.container.config.AuthModuleEntry;
import org.jboss.security.auth.login.JASPIAuthenticationInfo;
import org.jboss.security.auth.message.config.JBossClientAuthConfig;
import org.jboss.security.config.ControlFlag;
import org.jboss.security.plugins.ClassLoaderLocator;
import org.jboss.security.plugins.ClassLoaderLocatorFactory;

/**
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JBossWSClientAuthConfig extends JBossClientAuthConfig
{

   @SuppressWarnings("rawtypes")
   private List modules = new ArrayList();
   private CallbackHandler callbackHandler;

   @SuppressWarnings("rawtypes")
   public JBossWSClientAuthConfig(String layer, String appContext, CallbackHandler handler, Map properties)
   {
      super(layer, appContext, handler, properties);
   }

   @SuppressWarnings({ "rawtypes", "unchecked" })
   public ClientAuthContext getAuthContext(String authContextID, Subject clientSubject, Map properties) throws AuthException
   {
      List<ControlFlag> controlFlags = new ArrayList<ControlFlag>();

      Map<String, Map> mapOptionsByName = new HashMap<String, Map>();

      String secDomain = (String)properties.get("security-domain");

      JASPIAuthenticationInfo jai = (JASPIAuthenticationInfo)properties.get("jaspi-policy");
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

         try
         {
            mapOptionsByName.put(ame.getAuthModuleName(), ame.getOptions());
            controlFlags.add(ame.getControlFlag());
            ClientAuthModule sam = this.createCAM(moduleCL, ame.getAuthModuleName());

            Map options = new HashMap();
            sam.initialize(null, null, callbackHandler, options);
            modules.add(sam);
         }
         catch (Exception e)
         {
            throw new AuthException(e.getLocalizedMessage());
         }
      }

      JBossWSClientAuthContext clientAuthContext = new JBossWSClientAuthContext(modules, mapOptionsByName, this.callbackHandler);
      clientAuthContext.setControlFlags(controlFlags);
      return clientAuthContext;
   }

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private ClientAuthModule createCAM(ClassLoader moduleCL, String name) throws Exception
   {
      Class clazz = SecurityActions.loadClass(moduleCL, name);
      Constructor ctr = clazz.getConstructor(new Class[0]);
      return (ClientAuthModule)ctr.newInstance(new Object[0]);
   }

   @SuppressWarnings({ "rawtypes" })
   public List getClientAuthModules()
   {
      return modules;
   }

}