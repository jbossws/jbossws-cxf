
package org.jboss.test.ws.jaxws.benchmark.test.complex.types;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BulkRegister complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="BulkRegister">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Customers" type="{http://complex.jaxws.ws.test.jboss.org/}Customer" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="When" type="{http://www.w3.org/2001/XMLSchema}anySimpleType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BulkRegister", propOrder = {
    "customers",
    "when"
})
public class BulkRegister {

    @XmlElement(name = "Customers", nillable = true)
    protected List<Customer> customers;
    @XmlElement(name = "When", required = true, nillable = true)
    @XmlSchemaType(name = "anySimpleType")
    protected Object when;

    /**
     * Gets the value of the customers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the customers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCustomers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Customer }
     * 
     * 
     */
    public List<Customer> getCustomers() {
        if (customers == null) {
            customers = new ArrayList<Customer>();
        }
        return this.customers;
    }

    /**
     * Gets the value of the when property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getWhen() {
        return when;
    }

    /**
     * Sets the value of the when property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setWhen(Object value) {
        this.when = value;
    }

}
