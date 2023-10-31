package org.jboss.test.ws.jaxws.cxf.jbws4385;

@javax.jws.WebService(wsdlLocation = "WEB-INF/wsdl/HelloWorld.wsdl")
public interface Hello {

    @javax.jws.WebMethod
    public String hello(String name);
}
