/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jboss.wsf.stack.cxf.addons.transports.udp;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.jboss.logging.Logger;

import org.apache.cxf.Bus;
import org.apache.cxf.common.injection.NoJSR250Annotations;
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

    private static final Logger LOG = Logger.getLogger(UDPTransportFactory.class);
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
        LOG.debugv("Creating conduit for {0}", ei.getAddress());
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
