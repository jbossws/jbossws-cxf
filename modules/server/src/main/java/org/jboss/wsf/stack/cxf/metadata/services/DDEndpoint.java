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
package org.jboss.wsf.stack.cxf.metadata.services;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Metadata model for cxf.xml 
 *
 * @author Thomas.Diesler@jboss.org
 * @author ropalka@redhat.com
 * @author alessio.soldano@jboss.com
 */
public class DDEndpoint
{
   private String id;

   private String address;
   
   private String publishedEndpointUrl;

   private String implementor;

   private String invoker;

   private boolean mtomEnabled;
   
   private int mtomThreshold;
   
   private boolean addressingEnabled;
   
   private boolean addressingRequired;
   
   private String addressingResponses;
   
   private boolean respectBindingEnabled;
   
   private String wsdlLocation;
   
   private QName portName;
   
   private QName serviceName;
   
   private List<String> handlers;
   
   private Map<String, Object> properties;
   
   //additional fields
   private Class<?> epClass;
   
   private String annotationWsdlLocation;
   
   public QName getPortName()
   {
      return portName;
   }

   public void setPortName(QName portName)
   {
      this.portName = portName;
   }

   public QName getServiceName()
   {
      return serviceName;
   }

   public void setServiceName(QName serviceName)
   {
      this.serviceName = serviceName;
   }
   
   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   public String getAddress()
   {
      return address;
   }

   public void setAddress(String address)
   {
      this.address = address;
   }

   public String getPublishedEndpointUrl()
   {
      return publishedEndpointUrl;
   }

   public void setPublishedEndpointUrl(String publishedEndpointUrl)
   {
      this.publishedEndpointUrl = publishedEndpointUrl;
   }

   public String getImplementor()
   {
      return implementor;
   }

   public void setImplementor(String implementor)
   {
      this.implementor = implementor;
   }

   public String getWsdlLocation()
   {
      return wsdlLocation;
   }

   public void setWsdlLocation(String wsdlLocation)
   {
      this.wsdlLocation = wsdlLocation;
   }

   public String getAnnotationWsdlLocation()
   {
      return annotationWsdlLocation;
   }

   public void setAnnotationWsdlLocation(String annotationWsdlLocation)
   {
      this.annotationWsdlLocation = annotationWsdlLocation;
   }

   public Class<?> getEpClass()
   {
      return epClass;
   }

   public void setEpClass(Class<?> epClass)
   {
      this.epClass = epClass;
   }

   public String getInvoker()
   {
      return invoker;
   }
   
   public List<String> getHandlers()
   {
      return handlers;
   }

   public boolean isMtomEnabled()
   {
      return mtomEnabled;
   }

   public void setInvoker(String invoker)
   {
      this.invoker = invoker;
   }
   
   public void setHandlers(List<String> handlers)
   {
      this.handlers = handlers;
   }

   public void setMtomEnabled(boolean mtomEnabled)
   {
      this.mtomEnabled = mtomEnabled;
   }

   public void setMtomThreshold(final int mtomThreshold)
   {
      this.mtomThreshold = mtomThreshold;
   }
   
   public int getMtomThreshold() {
      return this.mtomThreshold;
   }
   
   public void setRespectBindingEnabled(final boolean respectBindingEnabled) {
      this.respectBindingEnabled = respectBindingEnabled;
   }
   
   public boolean isRespectBindingEnabled() {
      return this.respectBindingEnabled;
   }
   
   
   public void setAddressingEnabled(final boolean addressingEnabled) {
      this.addressingEnabled = addressingEnabled;
   }
   
   public boolean isAddressingEnabled() {
      return this.addressingEnabled;
   }

   public void setAddressingRequired(final boolean addressingRequired) {
      this.addressingRequired = addressingRequired;
   }
   
   public boolean isAddressingRequired() {
      return this.addressingRequired;
   }
   
   public void setAddressingResponses(final String responsesTypes)
   {
      this.addressingResponses = responsesTypes;
   }
   
   public String getAddressingResponses() {
      return this.addressingResponses;
   }
   
   public Map<String, Object> getProperties()
   {
      return properties;
   }

   public void setProperties(Map<String, Object> properties)
   {
      this.properties = properties;
   }   
   
   private StringBuilder basicToString()
   {
      StringBuilder str = new StringBuilder();
      str.append("id=" + this.id);
      str.append("\n address=" + this.address);
      str.append("\n implementor=" + this.implementor);
      str.append("\n serviceName=" + this.serviceName);
      str.append("\n portName=" + this.portName);
      str.append("\n annotationWsdlLocation=" + this.annotationWsdlLocation);
      str.append("\n wsdlLocationOverride=" + this.wsdlLocation);
      str.append("\n mtomEnabled=" + this.mtomEnabled);
      if (this.handlers != null && !this.handlers.isEmpty()) {
         str.append("\n handlers=[");
         for (Iterator<String> it = this.handlers.iterator(); it.hasNext();) {
            str.append(it.next());
            str.append(it.hasNext() ? "," : "]");
         }
      }
      return str;
   }
   
   public String toString()
   {
      return basicToString().toString();
   }

   public String toStringExtended()
   {
      StringBuilder str = basicToString();
      str.append("\n publishedEndpointUrl=" + this.publishedEndpointUrl);
      str.append("\n invoker=" + this.invoker);
      if (this.properties != null && !this.properties.isEmpty())
      {
         str.append("\n properties=[");
         for (Iterator<String> it = this.properties.keySet().iterator(); it.hasNext();)
         {
            final String p = it.next();
            str.append(p + " -> " + this.properties.get(p));
            str.append(it.hasNext() ? "," : "]");
         }
      }
      return str.toString();
   }
}
