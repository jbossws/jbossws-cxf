package org.jboss.test.ws.jaxws.cxf.jbws3809.dups;

import org.jboss.test.ws.jaxws.cxf.jbws3809.BasicEjb;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * User: rsearls
 * Date: 7/25/14
 */
@WebService(name = "MyDupEjbWebServiceNoServicenameService",
   portName = "DupEjbWebServiceNoServicenameServicePort",
   targetNamespace = "http://org.jboss.ws.test")
@SOAPBinding(style = SOAPBinding.Style.RPC, use = SOAPBinding.Use.LITERAL)
public class EjbWebServiceNoServicename implements BasicEjb {
   @WebMethod(operationName = "getStr")
   public String getStr (@WebParam(name = "str") String str) {
      return "Dup" + this.getClass().getSimpleName() + ": " + str;
   }
}
