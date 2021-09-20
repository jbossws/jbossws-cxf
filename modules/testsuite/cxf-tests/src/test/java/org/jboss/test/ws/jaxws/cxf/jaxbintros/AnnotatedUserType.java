/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.test.ws.jaxws.cxf.jaxbintros;
       
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlAttribute;
import javax.xml.namespace.QName;

/**
 * <p>Java class for UserType complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="UserType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="string" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="qname" type="{http://www.w3.org/2001/XMLSchema}QName"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 *
 *
 */
@XmlRootElement(namespace = "http://org.jboss.ws/provider", name = "user")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserType", propOrder = { "string", "qname" })
public class AnnotatedUserType
{

   @XmlAttribute(required = true)
   protected String string;
   @XmlElement(required = true)
   protected QName qname;

   /**
    * Gets the value of the string property.
    *
    * @return
    *     possible object is
    *     {@link String }
    *
    */
   public String getString()
   {
      return string;
   }

   /**
    * Sets the value of the string property.
    *
    * @param value
    *     allowed object is
    *     {@link String }
    *
    */
   public void setString(String value)
   {
      this.string = value;
   }

   /**
    * Gets the value of the qname property.
    *
    * @return
    *     possible object is
    *     {@link javax.xml.namespace.QName }
    *
    */
   public QName getQname()
   {
      return qname;
   }

   /**
    * Sets the value of the qname property.
    *
    * @param value
    *     allowed object is
    *     {@link javax.xml.namespace.QName }
    *
    */
   public void setQname(QName value)
   {
      this.qname = value;
   }

}

