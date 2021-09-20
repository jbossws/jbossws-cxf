
package org.jboss.test.ws.jaxws.benchmark.test.complex.types;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for AlreadyRegisteredFault complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AlreadyRegisteredFault">
 *   &lt;complexContent>
 *     &lt;extension base="{http://complex.jaxws.ws.test.jboss.org/}RegistrationFault">
 *       &lt;sequence>
 *         &lt;element name="existingId" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AlreadyRegisteredFault", propOrder = {
    "existingId"
})
public class AlreadyRegisteredFault
    extends RegistrationFault
{

    protected long existingId;

    /**
     * Gets the value of the existingId property.
     * 
     */
    public long getExistingId() {
        return existingId;
    }

    /**
     * Sets the value of the existingId property.
     * 
     */
    public void setExistingId(long value) {
        this.existingId = value;
    }

}
