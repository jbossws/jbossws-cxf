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
package org.jboss.test.ws.jaxws.samples.wsse.policy.trust.sts;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import jakarta.xml.ws.WebServiceProvider;

import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.apache.cxf.interceptor.InInterceptors;
import org.apache.cxf.sts.StaticSTSProperties;
import org.apache.cxf.sts.operation.TokenIssueOperation;
import org.apache.cxf.sts.operation.TokenValidateOperation;
import org.apache.cxf.sts.service.ServiceMBean;
import org.apache.cxf.sts.service.StaticService;
import org.apache.cxf.sts.token.delegation.UsernameTokenDelegationHandler;
import org.apache.cxf.sts.token.provider.SAMLTokenProvider;
import org.apache.cxf.sts.token.validator.SAMLTokenValidator;
import org.apache.cxf.sts.token.validator.UsernameTokenValidator;
import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.shared.WSTrustAppUtils;

@WebServiceProvider(serviceName = "SecurityTokenService",
      portName = "UT_Port",
      targetNamespace = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
      wsdlLocation = "WEB-INF/wsdl/ws-trust-1.4-service.wsdl")
//be sure to have dependency on org.apache.cxf module when on AS7, otherwise Apache CXF annotations are ignored 
@EndpointProperties(value = {
      @EndpointProperty(key = "ws-security.signature.username", value = "mystskey"),
      @EndpointProperty(key = "ws-security.signature.properties", value = "stsKeystore.properties"),
      @EndpointProperty(key = "ws-security.callback-handler", value = "org.jboss.test.ws.jaxws.samples.wsse.policy.trust.sts.STSCallbackHandler"),
      @EndpointProperty(key = "ws-security.validate.token", value = "false") //to let the JAAS integration deal with validation through the interceptor below
})
public class SampleSTS extends SecurityTokenServiceProvider
{
   public SampleSTS() throws Exception
   {
      super();
      
      StaticSTSProperties props = new StaticSTSProperties();
      props.setSignatureCryptoProperties("stsKeystore.properties");
      props.setSignatureUsername("mystskey");
      props.setCallbackHandlerClass(STSCallbackHandler.class.getName());
      props.setIssuer("DoubleItSTSIssuer");
      
      List<ServiceMBean> services = new LinkedList<ServiceMBean>();
      StaticService service = new StaticService();
      String serverHostRegexp = WSTrustAppUtils.getServerHost().replace("[", "\\[").replace("]", "\\]").replace("127.0.0.1", "localhost");
      service.setEndpoints(Arrays.asList(
         "http://" + serverHostRegexp + ":(\\d)*/jaxws-samples-wsse-policy-trust/SecurityService",
         "http://" + serverHostRegexp + ":(\\d)*/jaxws-samples-wsse-policy-trust-actas/ActAsService",
         "http://" + serverHostRegexp + ":(\\d)*/jaxws-samples-wsse-policy-trust-onbehalfof/OnBehalfOfService"
      ));
      services.add(service);
      
      TokenIssueOperation issueOperation = new TokenIssueOperation();
      issueOperation.setServices(services);
      issueOperation.getTokenProviders().add(new SAMLTokenProvider());
      // required for OnBehalfOf
      issueOperation.getTokenValidators().add(new UsernameTokenValidator());
      // added for OnBehalfOf and ActAs
      issueOperation.getDelegationHandlers().add(new UsernameTokenDelegationHandler());
      issueOperation.setStsProperties(props);
      
      TokenValidateOperation validateOperation = new TokenValidateOperation();
      validateOperation.getTokenValidators().add(new SAMLTokenValidator());
      validateOperation.setStsProperties(props);
      
      this.setIssueOperation(issueOperation);
      this.setValidateOperation(validateOperation);
   }
}
