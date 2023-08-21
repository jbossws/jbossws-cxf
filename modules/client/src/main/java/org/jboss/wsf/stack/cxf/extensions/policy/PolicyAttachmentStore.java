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
package org.jboss.wsf.stack.cxf.extensions.policy;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cxf.annotations.Policy.Placement;
import org.jboss.wsf.stack.cxf.client.ProviderImpl;
import org.jboss.logging.Logger;

/**
 * The store containing pre-defined policy attachments
 * grouped by set name; lazy loaded on first use.
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Jun-2013
 *
 */
public class PolicyAttachmentStore
{
   private static final String POLICY_ATTACHMENT_LOCATION = "META-INF/policies/";
   private final Map<String, List<PolicyAttachment>> attachments;
   private static PolicyAttachmentStore defaultServerInstance;
   
   /**
    * Creates a PolicyAttachmentStore parsing policy sets located
    * using the provided classloader.
    * 
    * @param cl
    */
   public PolicyAttachmentStore(ClassLoader cl) {
      Map<String, List<PolicyAttachment>> map = new HashMap<String, List<PolicyAttachment>>();
      parsePolicyAttachmentResources(cl, map);
      attachments = map;
   }
   
   /**
    * Get a singleton PolicyAttachmentStore getting policy sets using
    * the classloader of JBossWS-CXF JAXWS Provider SPI impl.
    * 
    * @return
    */
   public static synchronized PolicyAttachmentStore getDefaultInstance() {
      if (defaultServerInstance == null) {
         defaultServerInstance = new PolicyAttachmentStore(ProviderImpl.class.getClassLoader());
      }
      return defaultServerInstance;
   }
   
   public void merge(PolicyAttachmentStore pas) {
      this.attachments.putAll(pas.attachments);
   }
   
   public List<PolicyAttachment> get(String setName)
   {
      List<PolicyAttachment> l = attachments.get(setName);
      if (l != null && !l.isEmpty()) {
         return Collections.unmodifiableList(l);
      } else {
         return Collections.emptyList();
      }
   }
   
   public boolean isEmpty() {
      return attachments.isEmpty();
   }
   
   private static void parsePolicyAttachmentResources(ClassLoader cl, Map<String, List<PolicyAttachment>> map) {
      try {
         Enumeration<URL> urls = getResources(cl, POLICY_ATTACHMENT_LOCATION + PolicyAttachmentStore.class.getName());
         if (urls != null) {
            while (urls.hasMoreElements()) {
               parsePolicyAttachmentStore(urls.nextElement(), map);
            }
         }
      } catch (Exception e) {
         throw new RuntimeException(e);
      }
   }
   
   private static void parsePolicyAttachmentStore(URL url, Map<String, List<PolicyAttachment>> map) throws IOException {
      InputStream storeStream = url.openStream();
      if (storeStream != null) {
         String baseUrl = url.toString();
         baseUrl = baseUrl.substring(0, baseUrl.length() - PolicyAttachmentStore.class.getName().length());
         BufferedReader br = new BufferedReader(new InputStreamReader(storeStream, "UTF-8"));
         try {
            String set;
            while ((set = br.readLine()) != null) {
               for (Placement p : Placement.values()) {
                  final String newUrl = baseUrl + set + "-" + p + ".xml";
                  final URL policyAttachmentUrl = new URL(newUrl);
                  InputStream is = null;
                  try {
                     is = policyAttachmentUrl.openStream();
                     if (is != null) {
                        if (map.containsKey(set)) {
                           map.get(set).add(new PolicyAttachment(p, newUrl));
                        } else {
                           List<PolicyAttachment> list = new ArrayList<PolicyAttachment>(4);
                           list.add(new PolicyAttachment(p, newUrl));
                           map.put(set, list);
                        }
                     }
                  } catch (FileNotFoundException fnfe) {
                     //ignore
                     Logger.getLogger(PolicyAttachmentStore.class).trace(fnfe);
                  } finally {
                     if (is != null)
                        is.close();
                  }
               }
            }
         } finally {
            br.close();
         }
      }
   }
   
   private static Enumeration<URL> getResources(final ClassLoader cl, final String filename) throws Exception {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null) {
         return cl.getResources(filename);
      } else {
         return AccessController.doPrivileged(new PrivilegedExceptionAction<Enumeration<URL>>() {
            public Enumeration<URL> run() throws Exception {
               return cl.getResources(filename);
            }
         });
      }
   }
}
