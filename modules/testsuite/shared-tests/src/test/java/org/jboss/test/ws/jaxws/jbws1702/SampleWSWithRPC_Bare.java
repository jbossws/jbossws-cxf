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
package org.jboss.test.ws.jaxws.jbws1702;

import org.jboss.test.ws.jaxws.jbws1702.types.ClassB;
import org.jboss.test.ws.jaxws.jbws1702.types.ClassC;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.soap.SOAPBinding;

/**
 *
 *
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organisation: Marabu EDV</p>
 * @author strauch
 * @version     1.0
 */

@WebService()
@SOAPBinding(
  style = SOAPBinding.Style.RPC,
  use = SOAPBinding.Use.LITERAL,
  parameterStyle = SOAPBinding.ParameterStyle.BARE
)
public class SampleWSWithRPC_Bare implements SampleWSRpcSEI
{

  /**
   * Creates a new instance of SampleWSWithDocument_Wrapped
   */
  public SampleWSWithRPC_Bare() {
  }

  /**
   * In .NET Client (C#) the follow error occurs:
   * "The specified type was not recognized: name='classC', namespace='', at <return xmlns=''>."
   */
  public ClassB getClassCAsClassB() {
    ClassC classC= new ClassC();
    classC.setPropA("propA");
    classC.setPropB("propB");
    classC.setPropC("propC");
    return classC;
  }

  /**
   * Method that make ClassC available for all clients using this web service.
   * !! Is there another possibility to make inherited classes available? In J2EE4 styled endpoints you could
   * declare additional Classes in a seperate xml descriptor file. !!
   */  
  public ClassC getClassC() {
    return new ClassC();
  }

}
