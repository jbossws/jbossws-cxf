
package org.jboss.test.ws.jaxws.benchmark.test.complex.types;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegisterForInvoice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegisterForInvoice">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InvoiceCustomer" type="{http://complex.jaxws.ws.test.jboss.org/}InvoiceCustomer"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegisterForInvoice", propOrder = {
    "invoiceCustomer"
})
public class RegisterForInvoice {

    @XmlElement(name = "InvoiceCustomer", required = true, nillable = true)
    protected InvoiceCustomer invoiceCustomer;

    /**
     * Gets the value of the invoiceCustomer property.
     * 
     * @return
     *     possible object is
     *     {@link InvoiceCustomer }
     *     
     */
    public InvoiceCustomer getInvoiceCustomer() {
        return invoiceCustomer;
    }

    /**
     * Sets the value of the invoiceCustomer property.
     * 
     * @param value
     *     allowed object is
     *     {@link InvoiceCustomer }
     *     
     */
    public void setInvoiceCustomer(InvoiceCustomer value) {
        this.invoiceCustomer = value;
    }

}
