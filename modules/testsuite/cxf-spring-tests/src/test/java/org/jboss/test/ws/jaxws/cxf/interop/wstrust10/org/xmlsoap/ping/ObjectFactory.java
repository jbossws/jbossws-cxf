
package org.jboss.test.ws.jaxws.cxf.interop.wstrust10.org.xmlsoap.ping;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.xmlsoap.ping package. 
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

    private final static QName _Ping_QNAME = new QName("http://xmlsoap.org/Ping", "Ping");
    private final static QName _PingResponse_QNAME = new QName("http://xmlsoap.org/Ping", "PingResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.xmlsoap.ping
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link PingResponseBody }
     * 
     */
    public PingResponseBody createPingResponseBody() {
        return new PingResponseBody();
    }

    /**
     * Create an instance of {@link Ping }
     * 
     */
    public Ping createPing() {
        return new Ping();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Ping }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xmlsoap.org/Ping", name = "Ping")
    public JAXBElement<Ping> createPing(Ping value) {
        return new JAXBElement<Ping>(_Ping_QNAME, Ping.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PingResponseBody }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xmlsoap.org/Ping", name = "PingResponse")
    public JAXBElement<PingResponseBody> createPingResponse(PingResponseBody value) {
        return new JAXBElement<PingResponseBody>(_PingResponse_QNAME, PingResponseBody.class, null, value);
    }

}
