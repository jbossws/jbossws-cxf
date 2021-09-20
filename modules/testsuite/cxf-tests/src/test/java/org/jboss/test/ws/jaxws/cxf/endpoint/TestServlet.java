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
package org.jboss.test.ws.jaxws.cxf.endpoint;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.xml.ws.Endpoint;

import org.jboss.ws.common.utils.AddressUtils;

/**
 * @author Magesh Kumar B <mageshbk@jboss.com> (C) 2011 Red Hat Inc.
 */
public class TestServlet extends HttpServlet
{
    private static final long serialVersionUID = -2137273317393754516L;
    private Endpoint _endpoint;

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        String hostName = toIPv6URLFormat(System.getProperty("jboss.bind.address", "localhost"));
        String serviceURL = "http://" + hostName + ":48084/HelloWorldService";
        _endpoint = Endpoint.publish(serviceURL, new HelloWorldImpl(Thread.currentThread().getContextClassLoader()));
    }

    @Override
    public void destroy()
    {
        _endpoint.stop();
    }
    
    private String toIPv6URLFormat(final String host)
    {
       boolean isIPv6URLFormatted = false;
       if (host.startsWith("[") && host.endsWith("]")) {
          isIPv6URLFormatted = true;
       }
       //return IPv6 URL formatted address
       if (isIPv6URLFormatted) {
          return host;
       } else {
          return AddressUtils.isValidIPv6Address(host) ? "[" + host + "]" : host;
       }
    }
}