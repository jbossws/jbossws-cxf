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

import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.annotations.Policy.Placement;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.jboss.logging.Logger;
import org.jboss.wsf.stack.cxf.i18n.Messages;

/**
 * A lazy-loaded Policy attachment reference
 * with the placement point it is meant for.
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Jun-2013
 *
 */
public class PolicyAttachment
{
   private static final Logger log = Logger.getLogger(PolicyAttachment.class);
   private final Placement placement;
   private final String uri;

   public PolicyAttachment(Placement placement, String uri)
   {
      this.placement = placement;
      this.uri = uri;
   }

   public Placement getPlacement()
   {
      return placement;
   }
   
   public Element read(String defName)
   {
      XMLStreamReader reader = null;
      InputStream is = null;
      try
      {
         is = new URL(uri).openStream();
         reader = StaxUtils.createXMLStreamReader(is);
         Document doc = StaxUtils.read(reader);
         Element elem = doc.getDocumentElement();
         String id = elem.getAttributeNS(PolicyConstants.WSU_NAMESPACE_URI, PolicyConstants.WSU_ID_ATTR_NAME);
         if (StringUtils.isEmpty(id))
         {
            Attr att = doc.createAttributeNS(PolicyConstants.WSU_NAMESPACE_URI, "wsu:" + PolicyConstants.WSU_ID_ATTR_NAME);
            att.setNodeValue(defName);
            elem.setAttributeNodeNS(att);
         }
         return elem;
      }
      catch (Exception e)
      {
         throw Messages.MESSAGES.errorParsingPolicyAttachment(uri, e);
      }
      finally
      {
         try {
            StaxUtils.close(reader);
         } catch (Exception e) {
            //ignore
            log.trace(e);
         }
         if (is != null) {
            try {
               is.close();
            } catch (Exception e) {
               //ignore
               log.trace(e);
            }
         }
      }
   }
}
