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

/*
 * JBossWS WS-Tools Generated Source
 *
 * Generation Date: Thu Nov 02 21:17:37 CET 2006
 *
 * This generated source code represents a derivative work of the input to
 * the generator that produced it. Consult the input for the copyright and
 * terms of use that apply to this source code.
 */
package org.jboss.test.ws.jaxws.jbws1581;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService (name = "EndpointBean")
@SOAPBinding(style = SOAPBinding.Style.RPC)

/**
 * The SEI that is packaged with the war endpoint may potentially
 * contain different annotations that the one packaged with the EJB client
 */
public interface EndpointInterface
{
   String hello(String msg);
}
