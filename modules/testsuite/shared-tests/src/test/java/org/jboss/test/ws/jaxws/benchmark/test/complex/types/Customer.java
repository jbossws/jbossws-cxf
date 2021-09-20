
package org.jboss.test.ws.jaxws.benchmark.test.complex.types;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Customer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Customer">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="address" type="{http://complex.jaxws.ws.test.jboss.org/}Address"/>
 *         &lt;element name="contactNumbers" type="{http://complex.jaxws.ws.test.jboss.org/}PhoneNumber" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="id" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="name" type="{http://complex.jaxws.ws.test.jboss.org/}Name"/>
 *         &lt;element name="referredCustomers" type="{http://complex.jaxws.ws.test.jboss.org/}Customer" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Customer", namespace = "http://complex.jaxws.ws.test.jboss.org/", propOrder = {
    "address",
    "contactNumbers",
    "id",
    "name",
    "referredCustomers"
})
@XmlSeeAlso({
    InvoiceCustomer.class
})
public class Customer {

    @XmlElement(required = true, nillable = true)
    protected Address address;
    @XmlElement(nillable = true)
    protected List<PhoneNumber> contactNumbers;
    protected long id;
    @XmlElement(required = true, nillable = true)
    protected Name name;
    @XmlElement(nillable = true)
    protected List<Customer> referredCustomers;

    /**
     * Gets the value of the address property.
     * 
     * @return
     *     possible object is
     *     {@link Address }
     *     
     */
    public Address getAddress() {
        return address;
    }

    /**
     * Sets the value of the address property.
     * 
     * @param value
     *     allowed object is
     *     {@link Address }
     *     
     */
    public void setAddress(Address value) {
        this.address = value;
    }

    /**
     * Gets the value of the contactNumbers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the contactNumbers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getContactNumbers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PhoneNumber }
     * 
     * 
     */
    public List<PhoneNumber> getContactNumbers() {
        if (contactNumbers == null) {
            contactNumbers = new ArrayList<PhoneNumber>();
        }
        return this.contactNumbers;
    }

    /**
     * Gets the value of the id property.
     * 
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     */
    public void setId(long value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link Name }
     *     
     */
    public Name getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link Name }
     *     
     */
    public void setName(Name value) {
        this.name = value;
    }

    /**
     * Gets the value of the referredCustomers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the referredCustomers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getReferredCustomers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Customer }
     * 
     * 
     */
    public List<Customer> getReferredCustomers() {
        if (referredCustomers == null) {
            referredCustomers = new ArrayList<Customer>();
        }
        return this.referredCustomers;
    }

}
