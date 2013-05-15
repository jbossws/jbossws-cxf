
package org.jboss.test.ws.jaxws.benchmark.test.complex.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Statistics complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Statistics">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="activationTime" type="{http://www.w3.org/2001/XMLSchema}anySimpleType"/>
 *         &lt;element name="hits" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Statistics", namespace = "http://extra.complex.jaxws.ws.test.jboss.org/", propOrder = {
    "activationTime",
    "hits"
})
public class Statistics {

    @XmlElement(required = true, nillable = true)
    @XmlSchemaType(name = "anySimpleType")
    protected Object activationTime;
    protected long hits;

    /**
     * Gets the value of the activationTime property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getActivationTime() {
        return activationTime;
    }

    /**
     * Sets the value of the activationTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setActivationTime(Object value) {
        this.activationTime = value;
    }

    /**
     * Gets the value of the hits property.
     * 
     */
    public long getHits() {
        return hits;
    }

    /**
     * Sets the value of the hits property.
     * 
     */
    public void setHits(long value) {
        this.hits = value;
    }

}
