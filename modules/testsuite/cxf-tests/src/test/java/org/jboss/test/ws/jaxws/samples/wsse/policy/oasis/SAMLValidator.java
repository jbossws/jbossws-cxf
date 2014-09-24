package org.jboss.test.ws.jaxws.samples.wsse.policy.oasis;

public class SAMLValidator extends org.apache.wss4j.dom.validate.SamlAssertionValidator
{
   public SAMLValidator() {
      super();
      setRequireBearerSignature(false);
   }
}
