package org.jboss.test.ws.jaxws.cxf.jbws4453;

@jakarta.jws.WebService(wsdlLocation = "WEB-INF/wsdl/HelloWorld.wsdl")
public interface Hello {

    @jakarta.jws.WebMethod
    public String hello(String name);
}
