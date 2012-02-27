/**
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
/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.extensions.security;

import org.apache.cxf.ws.security.policy.SP12Constants;
import org.apache.cxf.ws.security.policy.SPConstants;
import org.apache.cxf.ws.security.policy.WSSPolicyException;
import org.apache.cxf.ws.security.policy.model.AlgorithmSuite;

/**
 * This AlgorithmSuite supports GCM security policies.
 */
public class GCMAlgorithmSuite extends AlgorithmSuite {
    
    public GCMAlgorithmSuite(SPConstants version) {
        super(version);
    }

    public GCMAlgorithmSuite() {
        super(SP12Constants.INSTANCE);
    }
    
    /**
     * Set the algorithm suite
     * 
     * @param algoSuite
     * @throws WSSPolicyException
     */
    @Override
    public void setAlgorithmSuite(String algoSuite) throws WSSPolicyException {
        this.algoSuiteString = algoSuite;
        
        if ("Basic128GCM".equals(algoSuite)) {
            this.digest = SPConstants.SHA1;
            this.encryption = "http://www.w3.org/2009/xmlenc11#aes128-gcm";
            this.symmetricKeyWrap = SPConstants.KW_AES128;
            this.asymmetricKeyWrap = SPConstants.KW_RSA_OAEP;
            this.encryptionKeyDerivation = SPConstants.P_SHA1_L128;
            this.signatureKeyDerivation = SPConstants.P_SHA1_L128;
            this.encryptionDerivedKeyLength = 128;
            this.signatureDerivedKeyLength = 128;
            this.minimumSymmetricKeyLength = 128;
        } else if ("Basic192GCM".equals(algoSuite)) {
            this.digest = SPConstants.SHA1;
            this.encryption = "http://www.w3.org/2009/xmlenc11#aes192-gcm";
            this.symmetricKeyWrap = SPConstants.KW_AES192;
            this.asymmetricKeyWrap = SPConstants.KW_RSA_OAEP;
            this.encryptionKeyDerivation = SPConstants.P_SHA1_L192;
            this.signatureKeyDerivation = SPConstants.P_SHA1_L192;
            this.encryptionDerivedKeyLength = 192;
            this.signatureDerivedKeyLength = 192;
            this.minimumSymmetricKeyLength = 192;
        } else if ("Basic256GCM".equals(algoSuite)) {
            this.digest = SPConstants.SHA1;
            this.encryption = "http://www.w3.org/2009/xmlenc11#aes256-gcm";
            this.symmetricKeyWrap = SPConstants.KW_AES256;
            this.asymmetricKeyWrap = SPConstants.KW_RSA_OAEP;
            this.encryptionKeyDerivation = SPConstants.P_SHA1_L256;
            this.signatureKeyDerivation = SPConstants.P_SHA1_L192;
            this.encryptionDerivedKeyLength = 256;
            this.signatureDerivedKeyLength = 192;
            this.minimumSymmetricKeyLength = 256;
            this.encryptionDerivedKeyLength = 256;
        } 
    }
}
