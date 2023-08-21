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
package org.jboss.test.ws.jaxws.jbws3552;

import java.util.StringTokenizer;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class ComplexObjectMAAdapter extends XmlAdapter<String, ComplexObjectMA> {
    @Override
    public String marshal(ComplexObjectMA v) throws Exception {
        return v.toString();
    }

    @Override
    public ComplexObjectMA unmarshal(String v) throws Exception {
        StringTokenizer st = new StringTokenizer(v, " ");
        return new ComplexObjectMA(st.nextToken(), st.nextToken());
    }
}
