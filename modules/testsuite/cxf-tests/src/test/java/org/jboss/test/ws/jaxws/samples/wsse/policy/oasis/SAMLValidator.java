package org.jboss.test.ws.jaxws.samples.wsse.policy.oasis;

public class SAMLValidator extends org.apache.ws.security.validate.SamlAssertionValidator
{
   public SAMLValidator() {
      super();
      setRequireBearerSignature(false);
   }
}
