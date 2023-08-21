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
package org.jboss.test.ws.jaxws.jbws1702;

import org.jboss.test.ws.jaxws.jbws1702.types.ClassC;
import org.jboss.test.ws.jaxws.jbws1702.types.ClassB;

import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;

/**
 *
 *
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organisation: Marabu EDV</p>
 * @author strauch
 * @version     1.0
 */

@WebService(endpointInterface = "org.jboss.test.ws.jaxws.jbws1702.SampleWSWrappedSEI")
@SOAPBinding( style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED )
public class SampleWSWithDocument_Wrapped implements SampleWSWrappedSEI
{

  /**
   * Creates a new instance of SampleWSWithDocument_Wrapped
   */
  public SampleWSWithDocument_Wrapped() {
  }

  /**
   * In .NET Client (C#) only the content information of ClassB is being submitted. (--> propC is unknown)
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
