package org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey;

import org.apache.cxf.annotations.EndpointProperties;
import org.apache.cxf.annotations.EndpointProperty;
import org.jboss.test.ws.jaxws.samples.wsse.policy.trust.bearer.BearerIface;

import javax.jws.WebService;

@WebService
   (
      portName = "HolderOfKeyServicePort",
      serviceName = "HolderOfKeyService",
      wsdlLocation = "WEB-INF/wsdl/HolderOfKeyService.wsdl",
      targetNamespace = "http://www.jboss.org/jbossws/ws-extensions/holderofkeywssecuritypolicy",
      endpointInterface = "org.jboss.test.ws.jaxws.samples.wsse.policy.trust.holderofkey.HolderOfKeyIface"
   )
@EndpointProperties(value = {
   @EndpointProperty(key = "ws-security.signature.properties", value = "serviceKeystore.properties")
})
public class HolderOfKeyImpl implements HolderOfKeyIface
{
   public String sayHello()
   {
      return "Holder-Of-Key WS-Trust Hello World!";
   }
}