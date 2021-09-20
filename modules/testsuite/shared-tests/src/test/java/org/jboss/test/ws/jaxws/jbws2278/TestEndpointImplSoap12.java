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
package org.jboss.test.ws.jaxws.jbws2278;

import jakarta.jws.WebService;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.soap.SOAPBinding;

/**
 * Test Endpoint implementation - SOAP 1.2 port.
 * 
 * @author alessio.soldano@jboss.com
 * @since 30-Sep-2008
 */
@WebService(name = "TestEndpoint", portName="TestEndpointSoap12Port",  targetNamespace = "http://org.jboss.test.ws/jbws2278", serviceName="TestService", endpointInterface = "org.jboss.test.ws.jaxws.jbws2278.TestEndpoint", wsdlLocation="WEB-INF/wsdl/Test.wsdl")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class TestEndpointImplSoap12 extends TestEndpointImpl implements TestEndpoint
{

}
