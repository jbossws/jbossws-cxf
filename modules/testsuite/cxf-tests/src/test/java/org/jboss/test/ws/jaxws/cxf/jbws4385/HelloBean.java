package org.jboss.test.ws.jaxws.cxf.jbws4385;


import javax.xml.parsers.DocumentBuilderFactory;

@javax.jws.WebService(targetNamespace = "http://test.ws.jboss.org/",
        wsdlLocation = "WEB-INF/wsdl/HelloWorld.wsdl")
public class HelloBean {
    public HelloBean() {
    }

    @javax.jws.WebMethod
    public String hello(String name) {
        String xercesJar = DocumentBuilderFactory.newInstance().getClass().
                getProtectionDomain().getCodeSource().getLocation().toString();
        if (xercesJar.contains("WEB-INF")) {
            xercesJar = xercesJar.substring(xercesJar.indexOf("WEB-INF"));
        } else {
            xercesJar = "WFLY modules";
        }
        return "Hello " + name + "and the DocumentBuilderFactory is loaded from " + xercesJar;
    }
}
