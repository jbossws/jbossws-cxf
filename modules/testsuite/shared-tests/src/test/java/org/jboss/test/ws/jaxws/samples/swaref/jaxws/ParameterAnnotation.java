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
package org.jboss.test.ws.jaxws.samples.swaref.jaxws;

import org.jboss.test.ws.jaxws.samples.swaref.DocumentPayload;

import jakarta.activation.DataHandler;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttachmentRef;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlRootElement(name = "parameterAnnotation", namespace = "http://swaref.samples.jaxws.ws.test.jboss.org/")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "parameterAnnotation", namespace = "http://swaref.samples.jaxws.ws.test.jboss.org/", propOrder = {
		"arg0",
		"arg1",
		"arg2"
		})
public class ParameterAnnotation {

	@XmlElement(name = "arg0", namespace = "")	
	private DocumentPayload arg0;

	@XmlElement(name = "arg1", namespace = "")
	private String arg1;

	@XmlElement(name = "arg2", namespace = "")
	@XmlAttachmentRef
	private DataHandler arg2;


	public DocumentPayload getArg0()
	{
		return arg0;
	}

	public void setArg0(DocumentPayload arg0)
	{
		this.arg0 = arg0;
	}

	/**
	 *
	 * @return
	 *     returns DataHandler
	 */
	public DataHandler getArg2() {
		return this.arg2;
	}

	/**
	 *
	 * @param arg2
	 *     the value for the arg0 property
	 */
	public void setArg2(DataHandler arg2) {
		this.arg2 = arg2;
	}

	/**
	 *
	 * @return
	 *     returns String
	 */
	public String getArg1() {
		return this.arg1;
	}

	/**
	 *
	 * @param arg1
	 *     the value for the arg1 property
	 */
	public void setArg1(String arg1) {
		this.arg1 = arg1;
	}

}
