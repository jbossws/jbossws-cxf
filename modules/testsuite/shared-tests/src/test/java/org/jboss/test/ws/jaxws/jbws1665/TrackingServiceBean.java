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
package org.jboss.test.ws.jaxws.jbws1665;

import java.rmi.RemoteException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.FinderException;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@Stateless
@WebService(name = "TrackingService", targetNamespace = "http://fleetworks.acunia.com/fleet/service")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class TrackingServiceBean implements TrackingServiceInterface {  
  @WebMethod
  public TracePollData pollTraces(
     @WebParam(name="customer") String customer, 
     @WebParam(name="mark") String mark) throws NullPointerException
  {
     return null;
  }
  
  @WebMethod
  public void requestTrace(
     @WebParam(name="customer") String customer, 
     @WebParam(name="terminals") String[] terminals) 
  throws NullPointerException
  {
  }
}
