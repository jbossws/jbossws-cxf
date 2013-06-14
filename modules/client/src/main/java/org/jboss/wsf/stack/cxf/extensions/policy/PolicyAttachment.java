/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.extensions.policy;

import java.io.InputStream;
import java.net.URL;

import javax.xml.stream.XMLStreamReader;

import org.apache.cxf.annotations.Policy.Placement;
import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.staxutils.StaxUtils;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.jboss.wsf.stack.cxf.Messages;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
   private Placement placement;
   private String uri;

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
         StaxUtils.close(reader);
         if (is != null) {
            try {
               is.close();
            } catch (Exception e) {
               //ignore
            }
         }
      }
   }
}
