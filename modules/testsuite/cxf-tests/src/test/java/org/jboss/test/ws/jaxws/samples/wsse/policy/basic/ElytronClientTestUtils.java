/*
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
package org.jboss.test.ws.jaxws.samples.wsse.policy.basic;

import org.wildfly.security.auth.client.AuthenticationContext;
import org.wildfly.security.auth.client.ElytronXmlParser;
import org.wildfly.security.auth.client.InvalidAuthenticationConfigurationException;

import java.security.PrivilegedAction;

import static java.security.AccessController.doPrivileged;

/**
 * Util class for working with Elytron client configuration
 * @author dvilkola@redhat.com
 * @since 2019
 */
public class ElytronClientTestUtils {

   // When Arquillian creates a deployment, it checks server-state using wildfly-controller-client. This action sets authentication context for JVM.
   // To test different client configurations, this method sets new authentication context (parsed from given file) to the context manager.
   public static void setElytronClientConfig(String configurationFilePath) {
      try {
         System.setProperty("wildfly.config.url", configurationFilePath);
         AuthenticationContext context = doPrivileged((PrivilegedAction<AuthenticationContext>) () -> {
            try {
               return ElytronXmlParser.parseAuthenticationClientConfiguration().create();
            } catch (Throwable t) {
               throw new InvalidAuthenticationConfigurationException(t);
            }
         });

         AuthenticationContext.getContextManager().setGlobalDefault(context);
      } finally {
         System.clearProperty("wildfly.config.url");
      }
   }
}
