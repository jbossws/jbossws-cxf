package org.jboss.test.ws.jaxws.cxf.jbws3516;

import javax.jws.WebService;
import javax.xml.ws.soap.Addressing;
import org.apache.cxf.interceptor.InInterceptors;

@WebService(serviceName = "SOAPService", portName = "SoapPort", 
            endpointInterface = "org.jboss.test.ws.jaxws.cxf.jbws3516.Greeter", 
            targetNamespace = "http://jboss.org/hello_world", 
            wsdlLocation = "WEB-INF/wsdl/hello_world.wsdl")
@Addressing
@InInterceptors(interceptors = { "org.apache.cxf.ws.addressing.soap.DecoupledFaultHandler" })
public class GreeterImpl implements Greeter
{
   public String sayHi(String request) throws SayHiFault
   {
      if (request.equals("fault"))
      {
         throw new SayHiFault("Intended SayHi Fault");
      }
      return request;
   }

   public void pingMe()
   {
      throw new RuntimeException("Intended PingMe Fault");
   }

}
