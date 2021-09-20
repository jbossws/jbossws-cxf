
package org.jboss.test.ws.jaxws.benchmark.test.complex.types;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for InvoiceCustomer complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="InvoiceCustomer">
 *   &lt;complexContent>
 *     &lt;extension base="{http://complex.jaxws.ws.test.jboss.org/}Customer">
 *       &lt;sequence>
 *         &lt;element name="cycleDay" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InvoiceCustomer", namespace = "http://complex.jaxws.ws.test.jboss.org/", propOrder = {
    "cycleDay"
})
public class InvoiceCustomer
    extends Customer
{

    protected int cycleDay;

    /**
     * Gets the value of the cycleDay property.
     * 
     */
    public int getCycleDay() {
        return cycleDay;
    }

    /**
     * Sets the value of the cycleDay property.
     * 
     */
    public void setCycleDay(int value) {
        this.cycleDay = value;
    }

}
