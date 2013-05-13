
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
package org.jboss.test.ws.jaxws.samples.wsse.kerberos.contract;

import javax.xml.ws.WebFault;

@WebFault(name = "DoubleItFault", targetNamespace = "http://www.example.org/schema/DoubleIt")
public class DoubleItFault extends Exception {
    public static final long serialVersionUID = 1L;
    
    private org.jboss.test.ws.jaxws.samples.wsse.kerberos.schema.DoubleItFault doubleItFault;

    public DoubleItFault() {
        super();
    }
    
    public DoubleItFault(String message) {
        super(message);
    }
    
    public DoubleItFault(String message, Throwable cause) {
        super(message, cause);
    }

    public DoubleItFault(String message, org.jboss.test.ws.jaxws.samples.wsse.kerberos.schema.DoubleItFault doubleItFault) {
        super(message);
        this.doubleItFault = doubleItFault;
    }

    public DoubleItFault(String message, org.jboss.test.ws.jaxws.samples.wsse.kerberos.schema.DoubleItFault doubleItFault, Throwable cause) {
        super(message, cause);
        this.doubleItFault = doubleItFault;
    }

    public org.jboss.test.ws.jaxws.samples.wsse.kerberos.schema.DoubleItFault getFaultInfo() {
        return this.doubleItFault;
    }
}
