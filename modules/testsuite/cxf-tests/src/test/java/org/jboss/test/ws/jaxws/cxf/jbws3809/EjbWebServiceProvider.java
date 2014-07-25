package org.jboss.test.ws.jaxws.cxf.jbws3809;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.WebServiceProvider;

/**
 * User: rsearls
 * Date: 7/25/14
 */
@WebServiceProvider(
   portName = "EjbWebServiceProviderServicePort",
   targetNamespace = "http://org.jboss.ws.test",
   serviceName="MyEjbWebServiceProvider")
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class EjbWebServiceProvider implements BasicEjb {
   @WebMethod(operationName = "getStr")
   public String getStr (@WebParam(name = "str") String str) {
      return this.getClass().getSimpleName() + ": " + str;
   }
}
