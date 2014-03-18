package org.jboss.test.ws.jaxws.samples.wsse.policy.trust.stsholderofkey;

import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.apache.cxf.sts.StaticSTSProperties;
import org.apache.cxf.sts.operation.TokenIssueOperation;
import org.apache.cxf.sts.operation.TokenValidateOperation;
import org.apache.cxf.sts.service.ServiceMBean;
import org.apache.cxf.sts.service.StaticService;
import org.apache.cxf.sts.token.delegation.HOKDelegationHandler;
import org.apache.cxf.sts.token.provider.SAMLTokenProvider;
import org.apache.cxf.sts.token.validator.SAMLTokenValidator;
import org.apache.cxf.ws.security.sts.provider.SecurityTokenServiceProvider;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.stsbearer.STSBearerCallbackHandler;

import javax.xml.ws.WebServiceProvider;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * User: rsearls
 * Date: 3/14/14
 */
@WebServiceProvider(serviceName = "SecurityTokenService",
   portName = "UT_Port",
   targetNamespace = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/",
   wsdlLocation = "WEB-INF/wsdl/holderofkey-ws-trust-1.4-service.wsdl")
//be sure to have dependency on org.apache.cxf module when on AS7, otherwise Apache CXF annotations are ignored
@EndpointProperties(value = {
//   @EndpointProperty(key = "ws-security.signature.username", value = "mystskey"),
   @EndpointProperty(key = "ws-security.signature.properties", value = "stsKeystore.properties"),
   @EndpointProperty(key = "ws-security.callback-handler", value = "org.jboss.test.ws.jaxws.samples.wsse.policy.trust.stsbearer.STSBearerCallbackHandler")
})
public class SampleSTSHolderOfKey extends SecurityTokenServiceProvider
{

   public SampleSTSHolderOfKey() throws Exception
   {
      super();

      StaticSTSProperties props = new StaticSTSProperties();
      props.setSignatureCryptoProperties("stsKeystore.properties");
      props.setSignatureUsername("mystskey");
      props.setCallbackHandlerClass(STSBearerCallbackHandler.class.getName());
      props.setEncryptionCryptoProperties("stsKeystore.properties");
      props.setEncryptionUsername("myservicekey");
      props.setIssuer("DoubleItSTSIssuer");

      List<ServiceMBean> services = new LinkedList<ServiceMBean>();
      StaticService service = new StaticService();
      service.setEndpoints(Arrays.asList(
         "https://localhost:(\\d)*/jaxws-samples-wsse-policy-trust-holderofkey/HolderOfKeyService",
         "https://\\[::1\\]:(\\d)*/jaxws-samples-wsse-policy-trust-holderofkey/HolderOfKeyService",
         "https://\\[0:0:0:0:0:0:0:1\\]:(\\d)*/jaxws-samples-wsse-policy-trust-holderofkey/HolderOfKeyService"
      ));

      services.add(service);

      TokenIssueOperation issueOperation = new TokenIssueOperation();
      issueOperation.getTokenProviders().add(new SAMLTokenProvider());
      issueOperation.getDelegationHandlers().add(new HOKDelegationHandler());
      issueOperation.setServices(services);
      issueOperation.setStsProperties(props);
      this.setIssueOperation(issueOperation);

      TokenValidateOperation validationOperation = new TokenValidateOperation();
      validationOperation.getTokenValidators().add(new SAMLTokenValidator());
      validationOperation.setStsProperties(props);
      this.setValidateOperation(validationOperation);

   }
}