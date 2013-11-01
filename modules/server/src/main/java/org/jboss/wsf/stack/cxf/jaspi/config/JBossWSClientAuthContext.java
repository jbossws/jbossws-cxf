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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.ClientAuthContext;
import javax.security.auth.message.module.ClientAuthModule;

import org.jboss.security.config.ControlFlag;

/**
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class JBossWSClientAuthContext implements ClientAuthContext {
   private List<ClientAuthModule> modules = new ArrayList<ClientAuthModule>();
   @SuppressWarnings("rawtypes")
   private Map<String, Map> moduleOptionsByName = new HashMap<String, Map>();
   protected List<ControlFlag> controlFlags = new ArrayList<ControlFlag>();

   @SuppressWarnings("rawtypes")
   public JBossWSClientAuthContext(List<ClientAuthModule> modules,
         Map<String, Map> moduleNameToOptions, CallbackHandler cbh)
         throws AuthException {
      this.modules = modules;
      this.moduleOptionsByName = moduleNameToOptions;
      for (ClientAuthModule cam : modules) {
         cam.initialize(null, null, cbh,
               moduleOptionsByName.get(cam.getClass().getName()));
      }
   }

   @Override
   public void cleanSubject(MessageInfo messageInfo, Subject subject)
         throws AuthException {
      for (ClientAuthModule cam : modules) {
         cam.cleanSubject(messageInfo, subject);
      }

   }

   public void setControlFlags(List<ControlFlag> controlFlags) {
      this.controlFlags = controlFlags;
   }

   @Override
   public AuthStatus secureRequest(MessageInfo messageInfo,
         Subject clientSubject) throws AuthException {
      AuthStatus status = null;
      for (ClientAuthModule sam : modules) {
         status = sam.secureRequest(messageInfo, clientSubject);
         if (status == AuthStatus.FAILURE) {
            break;
         }
      }
      return status;
   }

   @Override
   public AuthStatus validateResponse(MessageInfo messageInfo,
         Subject clientSubject, Subject serviceSubject) throws AuthException {
      AuthStatus status = null;
      for (ClientAuthModule sam : modules) {
         status = sam.secureRequest(messageInfo, clientSubject);
         if (status == AuthStatus.FAILURE) {
            break;
         }
      }
      return status;
   }

}