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