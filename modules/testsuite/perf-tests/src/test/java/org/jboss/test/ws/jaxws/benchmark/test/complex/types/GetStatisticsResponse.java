
package org.jboss.test.ws.jaxws.benchmark.test.complex.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for GetStatisticsResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GetStatisticsResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Statistics" type="{http://extra.complex.jaxws.ws.test.jboss.org/}Statistics"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetStatisticsResponse", propOrder = {
    "statistics"
})
public class GetStatisticsResponse {

    @XmlElement(name = "Statistics", required = true, nillable = true)
    protected Statistics statistics;

    /**
     * Gets the value of the statistics property.
     * 
     * @return
     *     possible object is
     *     {@link Statistics }
     *     
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * Sets the value of the statistics property.
     * 
     * @param value
     *     allowed object is
     *     {@link Statistics }
     *     
     */
    public void setStatistics(Statistics value) {
        this.statistics = value;
    }

}
