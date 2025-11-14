package org.jboss.test.ws.jaxws.cxf.jbws4453;

@jakarta.jws.WebService(targetNamespace = "http://test.ws.jboss.org/",
        wsdlLocation = "WEB-INF/wsdl/HelloWorld.wsdl")
public class HelloBean {
    public HelloBean() {
    }

    @jakarta.jws.WebMethod
    public String hello(String name) {
        return "Hello " + name;
    }
}
