
package org.jboss.test.ws.jaxws.benchmark.test.complex.types;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ValidationFault complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ValidationFault">
 *   &lt;complexContent>
 *     &lt;extension base="{http://complex.jaxws.ws.test.jboss.org/}RegistrationFault">
 *       &lt;sequence>
 *         &lt;element name="failedCustomers" type="{http://www.w3.org/2001/XMLSchema}long" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ValidationFault", propOrder = {
    "failedCustomers"
})
public class ValidationFault
    extends RegistrationFault
{

    @XmlElement(type = Long.class)
    protected List<Long> failedCustomers;

    /**
     * Gets the value of the failedCustomers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the failedCustomers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFailedCustomers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Long }
     * 
     * 
     */
    public List<Long> getFailedCustomers() {
        if (failedCustomers == null) {
            failedCustomers = new ArrayList<Long>();
        }
        return this.failedCustomers;
    }

}
