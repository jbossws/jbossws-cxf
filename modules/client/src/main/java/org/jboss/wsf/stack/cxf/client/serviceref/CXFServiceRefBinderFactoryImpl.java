/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.client.serviceref;

import org.jboss.wsf.spi.serviceref.ServiceRefBinder;
import org.jboss.wsf.spi.serviceref.ServiceRefBinderFactory;
import org.jboss.wsf.spi.serviceref.ServiceRefHandler.Type;

/**
 * Binds either JAXRPC or JAXWS Service object in the client's ENC.
 *
 * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
 */
public final class CXFServiceRefBinderFactoryImpl implements ServiceRefBinderFactory
{
   private static final ServiceRefBinder JAXRPC_BINDER = new CXFServiceRefBinderJAXRPC();

   private static final ServiceRefBinder JAXWS_BINDER = new CXFServiceRefBinderJAXWS();

   public ServiceRefBinder newServiceRefBinder(final Type type)
   {
      return type == Type.JAXRPC ? JAXRPC_BINDER : JAXWS_BINDER;
   }
}
