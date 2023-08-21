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
package org.jboss.test.ws.jaxws.benchmark.test.datatypes.wrapped.pojo;

import javax.xml.namespace.QName;

import org.jboss.test.ws.jaxws.benchmark.test.datatypes.DataTypesPOJOWrappedRPCTest;
import org.jboss.test.ws.jaxws.benchmark.test.datatypes.EndpointWrappedRPC;

/**
 * @author pmacik@redhat.com
 * @since 25-Mar-2010
 */
public class QNameRPCTest extends DataTypesPOJOWrappedRPCTest
{
   public static final QName exp = new QName("qname_value_modified");

   public void performIteration(Object port) throws Exception
   {
      QName ret = ((EndpointWrappedRPC) port).modifyQName(testedQName);
      if (!ret.equals(expectedQName))
      {
         throw new Exception("Unexpected result: " + ret + "\nExpected:" + expectedQName);
      }
   }
}
