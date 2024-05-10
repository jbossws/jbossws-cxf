package org.jboss.test.ws.jaxws.cxf.throttling;
import jakarta.jws.WebService;
import jakarta.xml.ws.WebServiceException;

@WebService
        (
                serviceName = "HelloService",
                endpointInterface = "org.jboss.test.ws.jaxws.cxf.throttling.Hello",
                targetNamespace = "http://org.jboss.ws/jaxws/cxf/throttling/hello"
        )
public class HelloImpl implements Hello
{
    public String sayHello(String input)  {
        if (input.equals("error")) {
            throw new WebServiceException("Error input");
        }
        return input;
    }
}
