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

import jakarta.jws.WebService;

import org.jboss.test.ws.jaxws.jbws1702.types.ClassC;
import org.jboss.test.ws.jaxws.jbws1702.types.ResponseWrapperB;
import org.jboss.test.ws.jaxws.jbws1702.types.ResponseWrapperC;

/**
 *
 *
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>Organisation: Marabu EDV</p>
 * @author strauch
 * @version     1.0
 */
@WebService(endpointInterface = "org.jboss.test.ws.jaxws.jbws1702.SampleWSBareSEI")
public class SampleWSWithDocument_Bare implements SampleWSBareSEI
{

  /**
   * Creates a new instance of SampleWSWithDocument_Bare
   */
  public SampleWSWithDocument_Bare() {
  }

  /**
   * In .NET Client (C#) only the content information of ClassB is being submitted.  (--> propC is unknown)
   */
  @Override
public ResponseWrapperB getClassCAsClassB() {
    ClassC classC= new ClassC();
    classC.setPropA("propA");
    classC.setPropB("propB");
    classC.setPropC("propC");

    ResponseWrapperB resp = new ResponseWrapperB();
    resp.setData(classC);
    return resp;
  }

  /**
   * Method that make ClassC available for all clients using this web service.
   */
  @Override
public ResponseWrapperC getClassC() {
    ClassC data = (ClassC) getClassCAsClassB().getData();
    ResponseWrapperC resp = new ResponseWrapperC();
    resp.setData(data);
    return resp;
  }

}
