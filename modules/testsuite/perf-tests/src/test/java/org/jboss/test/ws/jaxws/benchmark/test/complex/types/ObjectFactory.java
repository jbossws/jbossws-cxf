
package org.jboss.test.ws.jaxws.benchmark.test.complex.types;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.jboss.test.ws.jaxws.benchmark.test.complex.types package. 
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

    private final static QName _BulkRegister_QNAME = new QName("http://types.complex.jaxws.ws.test.jboss.org/", "BulkRegister");
    private final static QName _GetStatistics_QNAME = new QName("http://types.complex.jaxws.ws.test.jboss.org/", "GetStatistics");
    private final static QName _RegisterResponse_QNAME = new QName("http://types.complex.jaxws.ws.test.jboss.org/", "RegisterResponse");
    private final static QName _RegisterForInvoiceResponse_QNAME = new QName("http://types.complex.jaxws.ws.test.jboss.org/", "RegisterForInvoiceResponse");
    private final static QName _AlreadyRegisteredFault_QNAME = new QName("http://types.complex.jaxws.ws.test.jboss.org/", "AlreadyRegisteredFault");
    private final static QName _GetStatisticsResponse_QNAME = new QName("http://types.complex.jaxws.ws.test.jboss.org/", "GetStatisticsResponse");
    private final static QName _Register_QNAME = new QName("http://types.complex.jaxws.ws.test.jboss.org/", "Register");
    private final static QName _RegisterForInvoice_QNAME = new QName("http://types.complex.jaxws.ws.test.jboss.org/", "RegisterForInvoice");
    private final static QName _ValidationFault_QNAME = new QName("http://types.complex.jaxws.ws.test.jboss.org/", "ValidationFault");
    private final static QName _BulkRegisterResponse_QNAME = new QName("http://types.complex.jaxws.ws.test.jboss.org/", "BulkRegisterResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.jboss.test.ws.jaxws.benchmark.test.complex.types
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetStatisticsResponse }
     * 
     */
    public GetStatisticsResponse createGetStatisticsResponse() {
        return new GetStatisticsResponse();
    }

    /**
     * Create an instance of {@link GetStatistics }
     * 
     */
    public GetStatistics createGetStatistics() {
        return new GetStatistics();
    }

    /**
     * Create an instance of {@link Name }
     * 
     */
    public Name createName() {
        return new Name();
    }

    /**
     * Create an instance of {@link RegisterForInvoiceResponse }
     * 
     */
    public RegisterForInvoiceResponse createRegisterForInvoiceResponse() {
        return new RegisterForInvoiceResponse();
    }

    /**
     * Create an instance of {@link RegisterForInvoice }
     * 
     */
    public RegisterForInvoice createRegisterForInvoice() {
        return new RegisterForInvoice();
    }

    /**
     * Create an instance of {@link RegistrationFault }
     * 
     */
    public RegistrationFault createRegistrationFault() {
        return new RegistrationFault();
    }

    /**
     * Create an instance of {@link Register }
     * 
     */
    public Register createRegister() {
        return new Register();
    }

    /**
     * Create an instance of {@link InvoiceCustomer }
     * 
     */
    public InvoiceCustomer createInvoiceCustomer() {
        return new InvoiceCustomer();
    }

    /**
     * Create an instance of {@link BulkRegisterResponse }
     * 
     */
    public BulkRegisterResponse createBulkRegisterResponse() {
        return new BulkRegisterResponse();
    }

    /**
     * Create an instance of {@link ValidationFault }
     * 
     */
    public ValidationFault createValidationFault() {
        return new ValidationFault();
    }

    /**
     * Create an instance of {@link Statistics }
     * 
     */
    public Statistics createStatistics() {
        return new Statistics();
    }

    /**
     * Create an instance of {@link BulkRegister }
     * 
     */
    public BulkRegister createBulkRegister() {
        return new BulkRegister();
    }

    /**
     * Create an instance of {@link Address }
     * 
     */
    public Address createAddress() {
        return new Address();
    }

    /**
     * Create an instance of {@link PhoneNumber }
     * 
     */
    public PhoneNumber createPhoneNumber() {
        return new PhoneNumber();
    }

    /**
     * Create an instance of {@link AlreadyRegisteredFault }
     * 
     */
    public AlreadyRegisteredFault createAlreadyRegisteredFault() {
        return new AlreadyRegisteredFault();
    }

    /**
     * Create an instance of {@link Customer }
     * 
     */
    public Customer createCustomer() {
        return new Customer();
    }

    /**
     * Create an instance of {@link RegisterResponse }
     * 
     */
    public RegisterResponse createRegisterResponse() {
        return new RegisterResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BulkRegister }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.complex.jaxws.ws.test.jboss.org/", name = "BulkRegister")
    public JAXBElement<BulkRegister> createBulkRegister(BulkRegister value) {
        return new JAXBElement<BulkRegister>(_BulkRegister_QNAME, BulkRegister.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStatistics }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.complex.jaxws.ws.test.jboss.org/", name = "GetStatistics")
    public JAXBElement<GetStatistics> createGetStatistics(GetStatistics value) {
        return new JAXBElement<GetStatistics>(_GetStatistics_QNAME, GetStatistics.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegisterResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.complex.jaxws.ws.test.jboss.org/", name = "RegisterResponse")
    public JAXBElement<RegisterResponse> createRegisterResponse(RegisterResponse value) {
        return new JAXBElement<RegisterResponse>(_RegisterResponse_QNAME, RegisterResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegisterForInvoiceResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.complex.jaxws.ws.test.jboss.org/", name = "RegisterForInvoiceResponse")
    public JAXBElement<RegisterForInvoiceResponse> createRegisterForInvoiceResponse(RegisterForInvoiceResponse value) {
        return new JAXBElement<RegisterForInvoiceResponse>(_RegisterForInvoiceResponse_QNAME, RegisterForInvoiceResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AlreadyRegisteredFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.complex.jaxws.ws.test.jboss.org/", name = "AlreadyRegisteredFault")
    public JAXBElement<AlreadyRegisteredFault> createAlreadyRegisteredFault(AlreadyRegisteredFault value) {
        return new JAXBElement<AlreadyRegisteredFault>(_AlreadyRegisteredFault_QNAME, AlreadyRegisteredFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetStatisticsResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.complex.jaxws.ws.test.jboss.org/", name = "GetStatisticsResponse")
    public JAXBElement<GetStatisticsResponse> createGetStatisticsResponse(GetStatisticsResponse value) {
        return new JAXBElement<GetStatisticsResponse>(_GetStatisticsResponse_QNAME, GetStatisticsResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Register }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.complex.jaxws.ws.test.jboss.org/", name = "Register")
    public JAXBElement<Register> createRegister(Register value) {
        return new JAXBElement<Register>(_Register_QNAME, Register.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegisterForInvoice }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.complex.jaxws.ws.test.jboss.org/", name = "RegisterForInvoice")
    public JAXBElement<RegisterForInvoice> createRegisterForInvoice(RegisterForInvoice value) {
        return new JAXBElement<RegisterForInvoice>(_RegisterForInvoice_QNAME, RegisterForInvoice.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ValidationFault }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.complex.jaxws.ws.test.jboss.org/", name = "ValidationFault")
    public JAXBElement<ValidationFault> createValidationFault(ValidationFault value) {
        return new JAXBElement<ValidationFault>(_ValidationFault_QNAME, ValidationFault.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BulkRegisterResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://types.complex.jaxws.ws.test.jboss.org/", name = "BulkRegisterResponse")
    public JAXBElement<BulkRegisterResponse> createBulkRegisterResponse(BulkRegisterResponse value) {
        return new JAXBElement<BulkRegisterResponse>(_BulkRegisterResponse_QNAME, BulkRegisterResponse.class, null, value);
    }

}
