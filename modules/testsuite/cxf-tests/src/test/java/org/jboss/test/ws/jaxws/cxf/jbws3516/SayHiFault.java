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
package org.jboss.test.ws.jaxws.cxf.jbws3516;

import jakarta.xml.ws.WebFault;
@WebFault(name = "faultDetail", targetNamespace = "http://jboss.org/hello_world/types")
public class SayHiFault extends Exception {
   private static final long serialVersionUID = 1L;
   private org.jboss.test.ws.jaxws.cxf.jbws3516.FaultDetail faultDetail;

    public SayHiFault() {
        super();
    }
    
    public SayHiFault(String message) {
        super(message);
    }
    
    public SayHiFault(String message, Throwable cause) {
        super(message, cause);
    }

    public SayHiFault(String message, org.jboss.test.ws.jaxws.cxf.jbws3516.FaultDetail faultDetail) {
        super(message);
        this.faultDetail = faultDetail;
    }

    public SayHiFault(String message, org.jboss.test.ws.jaxws.cxf.jbws3516.FaultDetail faultDetail, Throwable cause) {
        super(message, cause);
        this.faultDetail = faultDetail;
    }

    public org.jboss.test.ws.jaxws.cxf.jbws3516.FaultDetail getFaultInfo() {
        return this.faultDetail;
    }
}
