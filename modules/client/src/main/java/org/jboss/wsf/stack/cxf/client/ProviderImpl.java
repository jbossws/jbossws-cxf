/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.client;

import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.spi.ServiceDelegate;

import org.apache.cxf.Bus;
import org.apache.cxf.BusFactory;

/**
 * 
 * @author alessio.soldano@jboss.com
 * @since 04-Apr-2011
 * 
 */
public class ProviderImpl extends org.apache.cxf.jaxws.spi.ProviderImpl {
	public ServiceDelegate createServiceDelegate(URL url, QName qname, Class cls) {
		setValidThreadDefaultBus();
		return super.createServiceDelegate(url, qname, cls);
	}

	public ServiceDelegate createServiceDelegate(URL wsdlDocumentLocation,
												 QName serviceName,
												 Class serviceClass,
												 WebServiceFeature... features) {
		setValidThreadDefaultBus();
		return super.createServiceDelegate(wsdlDocumentLocation, serviceName, serviceClass, features);
	}

	static Bus setValidThreadDefaultBus() {
		// we need to prevent using the default bus when the current
		// thread is not already associated to a bus. In those situations we
		// create
		// a new bus from scratch instead and link that to the thread.
		Bus bus = BusFactory.getThreadDefaultBus(false);
		if (bus == null) {
			bus = BusFactory.newInstance().createBus(); // this also set thread
														// local bus internally
														// as it's not set yet
		}
		return bus;
	}
}
