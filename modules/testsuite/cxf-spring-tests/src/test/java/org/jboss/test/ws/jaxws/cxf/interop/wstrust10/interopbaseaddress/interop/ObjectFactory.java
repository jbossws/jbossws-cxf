
package org.jboss.test.ws.jaxws.cxf.interop.wstrust10.interopbaseaddress.interop;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the interopbaseaddress.interop package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _EchoResponseEchoResult_QNAME = new QName("http://InteropBaseAddress/interop", "echoResult");
    private final static QName _EchoRequest_QNAME = new QName("http://InteropBaseAddress/interop", "request");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: interopbaseaddress.interop
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EchoResponse }
     * 
     */
    public EchoResponse createEchoResponse() {
        return new EchoResponse();
    }

    /**
     * Create an instance of {@link Echo }
     * 
     */
    public Echo createEcho() {
        return new Echo();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://InteropBaseAddress/interop", name = "echoResult", scope = EchoResponse.class)
    public JAXBElement<String> createEchoResponseEchoResult(String value) {
        return new JAXBElement<String>(_EchoResponseEchoResult_QNAME, String.class, EchoResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://InteropBaseAddress/interop", name = "request", scope = Echo.class)
    public JAXBElement<String> createEchoRequest(String value) {
        return new JAXBElement<String>(_EchoRequest_QNAME, String.class, Echo.class, value);
    }

}
