package org.jboss.test.ws.jaxws.cxf.throttling;

import jakarta.jws.WebService;

@WebService(targetNamespace = "http://org.jboss.ws/jaxws/cxf/throttling/hello")
public interface Hello
{
    String sayHello(String input);
}