
package org.jboss.test.ws.jaxws.benchmark.test.complex.types;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for RegisterResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="RegisterResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="RegisteredID" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegisterResponse", propOrder = {
    "registeredID"
})
public class RegisterResponse {

    @XmlElement(name = "RegisteredID")
    protected long registeredID;

    /**
     * Gets the value of the registeredID property.
     * 
     */
    public long getRegisteredID() {
        return registeredID;
    }

    /**
     * Sets the value of the registeredID property.
     * 
     */
    public void setRegisteredID(long value) {
        this.registeredID = value;
    }

}
