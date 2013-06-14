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
package org.jboss.wsf.stack.cxf.policy;

import static org.jboss.wsf.stack.cxf.Loggers.ROOT_LOGGER;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.wsdl.extensions.UnknownExtensibilityElement;

import org.apache.cxf.annotations.Policy.Placement;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.helpers.DOMUtils;
import org.apache.cxf.service.factory.AbstractServiceFactoryBean;
import org.apache.cxf.service.factory.FactoryBeanListener;
import org.apache.cxf.service.model.AbstractPropertiesHolder;
import org.apache.cxf.service.model.BindingFaultInfo;
import org.apache.cxf.service.model.BindingInfo;
import org.apache.cxf.service.model.BindingOperationInfo;
import org.apache.cxf.service.model.DescriptionInfo;
import org.apache.cxf.service.model.FaultInfo;
import org.apache.cxf.service.model.InterfaceInfo;
import org.apache.cxf.service.model.OperationInfo;
import org.apache.cxf.service.model.ServiceInfo;
import org.apache.cxf.ws.policy.PolicyConstants;
import org.apache.neethi.Constants;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A FactoryBeanListener that adds policies according to a given map of EndpointPolicyAttachment
 * 
 * @author alessio.soldano@jboss.com
 * @since 03-Jun-2013
 */
public class PolicySetsAnnotationListener implements FactoryBeanListener
{
   private final Map<Class<?>, EndpointPolicyAttachments> epaMap = new HashMap<Class<?>, EndpointPolicyAttachments>();
   private PolicyAttachmentStore store;
   
   public PolicySetsAnnotationListener() {
      this.store = PolicyAttachmentStore.getDefaultInstance();
   }
   
   public PolicySetsAnnotationListener(ClassLoader cl) {
      this.store = PolicyAttachmentStore.getDefaultInstance();
      if (cl != null) {
         PolicyAttachmentStore pas = new PolicyAttachmentStore(cl);
         if (!pas.isEmpty()) {
            pas.merge(this.store);
            this.store = pas;
         }
      }
   }
   
   public PolicySetsAnnotationListener(PolicyAttachmentStore store) {
      this.store = store;
   }
   
   protected EndpointPolicyAttachments getEndpointPolicyAttachment(Class<?> clazz) {
      if (epaMap.containsKey(clazz)) {
         return epaMap.get(clazz);
      } else {
         final PolicySets ps = clazz.getAnnotation(PolicySets.class);
         final EndpointPolicyAttachments epa = (ps != null) ? EndpointPolicyAttachments.newInstance(ps.value(), store) : null;
         epaMap.put(clazz, epa);
         return epa;
      }
   }

   public void handleEvent(Event ev, AbstractServiceFactoryBean factory, Object... args)
   {
      switch (ev)
      {
         case ENDPOINT_SELECTED : {
            Class<?> cls = (Class<?>) args[2];
            Class<?> implCls = (Class<?>) args[3];
            Endpoint ep = (Endpoint) args[1];
            addPolicies(factory, ep, cls, implCls);
            break;
         }
         case BINDING_OPERATION_CREATED :
            BindingOperationInfo boi = (BindingOperationInfo) args[1];
            Method m = (Method) args[2];
            addPolicies(factory, boi.getOperationInfo(), m);
            break;
         default :
            //ignore
      }
   }

   private void addPolicies(AbstractServiceFactoryBean factory, OperationInfo inf, Method m)
   {
      if (m == null)
      {
         return;
      }

      final Class<?> cls = m.getDeclaringClass();
      EndpointPolicyAttachments epa = getEndpointPolicyAttachment(cls);
      if (epa != null)
      {
         final ServiceInfo service = inf.getInterface().getService();
         for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.PORT_TYPE_OPERATION)) {
            addPolicy(inf, service, pa, cls, inf.getName().getLocalPart() + "PortTypeOpPolicy");
         }
         for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.PORT_TYPE_OPERATION_INPUT)) {
            addPolicy(inf.getInput(), service, pa, cls, inf.getName().getLocalPart() + "PortTypeOpInputPolicy");
         }
         for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.PORT_TYPE_OPERATION_OUTPUT)) {
            addPolicy(inf.getOutput(), service, pa, cls, inf.getName().getLocalPart() + "PortTypeOpOutputPolicy");
         }
         for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.PORT_TYPE_OPERATION_FAULT)) {
            for (FaultInfo f : inf.getFaults())
            {
               addPolicy(f, service, pa, cls, f.getName().getLocalPart() + "PortTypeOpFaultPolicy");
            }
         }
      }
   }
   
   private void addPolicies(AbstractServiceFactoryBean factory, Endpoint ep, Class<?> seiCls, Class<?> implCls)
   {
      EndpointPolicyAttachments epa = null;
      Class<?> cls = seiCls;
      if (ep.getEndpointInfo().getInterface() != null) {
         epa = getEndpointPolicyAttachment(seiCls);
      }
      if (epa == null) {
         cls = implCls;
         epa = getEndpointPolicyAttachment(implCls);
      }
      if (epa != null) {
         final BindingInfo binf = ep.getBinding().getBindingInfo();
         final ServiceInfo si = ep.getService().getServiceInfos().get(0);
         final InterfaceInfo inf = ep.getEndpointInfo().getInterface();
         
         for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.BINDING)) {
            addPolicy(binf, si, pa, cls, binf.getName().getLocalPart() + "BindingPolicy");
         }
         for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.PORT_TYPE)) {
            addPolicy(inf, si, pa, cls, inf.getName().getLocalPart() + "PortTypePolicy");
         }
         for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.SERVICE)) {
            addPolicy(si, si, pa, cls, si.getName().getLocalPart() + "ServicePolicy");
         }
         for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.SERVICE_PORT)) {
            addPolicy(ep.getEndpointInfo(), si, pa, cls, ep.getEndpointInfo().getName().getLocalPart()
                  + "PortPolicy");
         }
   
         for (BindingOperationInfo binfo : binf.getOperations())
         {
            for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.BINDING_OPERATION)) {
               addPolicy(binfo, si, pa, cls, binfo.getName().getLocalPart() + "BindingOpPolicy");
            }
            for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.BINDING_OPERATION_INPUT)) {
               addPolicy(binfo.getInput(), si, pa, cls, binfo.getName().getLocalPart()
                     + "BindingOpInputPolicy");
            }
            for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.BINDING_OPERATION_OUTPUT)) {
               addPolicy(binfo.getOutput(), si, pa, cls, binfo.getName().getLocalPart()
                     + "BindingOpOutputPolicy");
            }
            for (PolicyAttachment pa : epa.getPolicyAttachments(Placement.BINDING_OPERATION_FAULT)) {
               for (BindingFaultInfo f : binfo.getFaults())
               {
                  addPolicy(f, si, pa, cls, f.getFaultInfo().getName().getLocalPart()
                        + "BindingOpFaultPolicy");
               }
            }
         }
      }
   }

   private void addPolicy(AbstractPropertiesHolder place, ServiceInfo service, PolicyAttachment pa, Class<?> cls, String defName)
   {
      Element el = addPolicy(service, pa, cls, defName);
      UnknownExtensibilityElement uee = new UnknownExtensibilityElement();
      uee.setElement(el);
      uee.setRequired(true);
      uee.setElementType(DOMUtils.getElementQName(el));
      place.addExtensor(uee);
   }

   private Element addPolicy(ServiceInfo service, PolicyAttachment pa, Class<?> cls, String defName)
   {
      Element element = pa.read(defName);
      String refId = getPolicyId(element);
      ROOT_LOGGER.addingPolicyAttachment(pa.getPlacement(), refId, cls);

      if (service.getDescription() == null && cls != null)
      {
         service.setDescription(new DescriptionInfo());
         URL u = cls.getResource("/");
         if (u != null)
         {
            service.getDescription().setBaseURI(u.toString());
         }
      }

      // if not already added to service add it, otherwise ignore 
      // and just create the policy reference.
      if (!isExistsPolicy(service, refId))
      {
         UnknownExtensibilityElement uee = new UnknownExtensibilityElement();
         uee.setElement(element);
         uee.setRequired(true);
         uee.setElementType(DOMUtils.getElementQName(element));
         service.getDescription().addExtensor(uee);
      }

      refId = "#" + refId;

      Document doc = DOMUtils.createDocument();
      Element el = doc.createElementNS(element.getNamespaceURI(), "wsp:" + Constants.ELEM_POLICY_REF);
      Attr att = doc.createAttributeNS(null, "URI");
      att.setValue(refId);
      el.setAttributeNodeNS(att);
      return el;
   }

   private String getPolicyId(Element element)
   {
      return element.getAttributeNS(PolicyConstants.WSU_NAMESPACE_URI, PolicyConstants.WSU_ID_ATTR_NAME);
   }

   private boolean isExistsPolicy(ServiceInfo service, String uri)
   {
      Object exts[] = service.getDescription().getExtensors().get();
      exts = exts == null ? new Object[0] : exts;
      for (Object o : exts)
      {
         if (o instanceof UnknownExtensibilityElement)
         {
            UnknownExtensibilityElement uee = (UnknownExtensibilityElement) o;
            String uri2 = getPolicyId(uee.getElement());
            if (uri.equals(uri2))
            {
               return true;
            }
         }
      }
      return false;
   }
}
