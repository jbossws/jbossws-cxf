/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat Middleware LLC, and individual contributors
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

package org.jboss.wsf.stack.cxf.addons.transports.udp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.injection.NoJSR250Annotations;
import org.apache.cxf.common.logging.LogUtils;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.transport.AbstractTransportFactory;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.ConduitInitiator;
import org.apache.cxf.transport.Destination;
import org.apache.cxf.transport.DestinationFactory;
import org.apache.cxf.ws.addressing.AttributedURIType;
import org.apache.cxf.ws.addressing.EndpointReferenceType;

@NoJSR250Annotations
public class UDPTransportFactory extends AbstractTransportFactory
    implements DestinationFactory, ConduitInitiator {
   
    public static final String TRANSPORT_ID = "http://cxf.apache.org/transports/udp";
    public static final List<String> DEFAULT_NAMESPACES 
        = Arrays.asList(TRANSPORT_ID);

    private static final Logger LOG = LogUtils.getL7dLogger(UDPTransportFactory.class);
    private static final Set<String> URI_PREFIXES = new HashSet<String>();
    static {
        URI_PREFIXES.add("udp://");
    }
    
    private Set<String> uriPrefixes = new HashSet<String>(URI_PREFIXES);

    public UDPTransportFactory() {
        this(null);
    }
    public UDPTransportFactory(Bus b) {
        super(DEFAULT_NAMESPACES);
    }
    
    public Destination getDestination(EndpointInfo ei, Bus bus) throws IOException {
        return getDestination(ei, null, bus);
    }

    protected Destination getDestination(EndpointInfo ei,
                                         EndpointReferenceType reference,
                                         Bus bus)
        throws IOException {
        if (reference == null) {
            reference = createReference(ei);
        }
        return new UDPDestination(bus, reference, ei);
    }


    public Conduit getConduit(EndpointInfo ei, Bus bus) throws IOException {
        return getConduit(ei, null, bus);
    }

    public Conduit getConduit(EndpointInfo ei, EndpointReferenceType target, Bus bus) throws IOException {
        LOG.log(Level.FINE, "Creating conduit for {0}", ei.getAddress());
        if (target == null) {
            target = createReference(ei);
        }
        return new UDPConduit(target, bus);
    }


    public Set<String> getUriPrefixes() {
        return uriPrefixes;
    }
    public void setUriPrefixes(Set<String> s) {
        uriPrefixes = s;
    }
    EndpointReferenceType createReference(EndpointInfo ei) {
        EndpointReferenceType epr = new EndpointReferenceType();
        AttributedURIType address = new AttributedURIType();
        address.setValue(ei.getAddress());
        epr.setAddress(address);
        return epr;
    }

}
