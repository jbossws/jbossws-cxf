/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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
package org.jboss.wsf.stack.cxf.jaspi.module;

import java.util.Map.Entry;
import java.util.Properties;

import javax.security.auth.Subject;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.SoapVersion;
import org.apache.cxf.binding.soap.SoapVersionFactory;
import org.apache.cxf.message.Exchange;
import org.apache.cxf.message.ExchangeImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.message.MessageImpl;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngine;
import org.jboss.security.auth.container.modules.AbstractServerAuthModule;
import org.jboss.wsf.stack.cxf.jaspi.config.JBossWSAuthConstants;
import org.jboss.wsf.stack.cxf.jaspi.validator.UsernameTokenValidator;
/** 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class SOAPServerAuthModule extends AbstractServerAuthModule {
	private String securityDomainName = null;
   private WSSConfig wssConfig = WSSConfig.getNewInstance();
   private WSS4JInInterceptor wss4jInterceptor = new WSS4JInInterceptor();
	public SOAPServerAuthModule() {
		supportedTypes.add(Object.class);
		supportedTypes.add(SOAPMessage.class);
	}

	public SOAPServerAuthModule(String lmshName) {
		supportedTypes.add(Object.class);
		this.supportedTypes.add(SOAPMessage.class);
		securityDomainName = lmshName;
	}

	@Override
	public AuthStatus validateRequest(MessageInfo messageInfo,
			Subject clientSubject, Subject serviceSubject) throws AuthException {	   
		return validate(clientSubject, messageInfo) ? AuthStatus.SUCCESS : AuthStatus.FAILURE;
	}
	
	@Override
	protected boolean validate(Subject clientSubject, MessageInfo messageInfo)
			throws AuthException {
		
		SOAPMessage soapMessage = (SOAPMessage)messageInfo.getRequestMessage();
		SoapVersion soapVersion = null;
		try {
			String ns = soapMessage.getSOAPBody().getNamespaceURI();
			soapVersion = SoapVersionFactory.getInstance().getSoapVersion(ns);
		} catch (SOAPException e) {
			throw new AuthException(e.getMessage());
		}
		if (soapVersion == null) {
			throw new AuthException("Invalid soap message");
		}
			

		Exchange exchange = new ExchangeImpl();
        MessageImpl messageImpl = new MessageImpl();
        messageImpl.setExchange(exchange);
		SoapMessage cxfSoapMessage = new SoapMessage(messageImpl);
		cxfSoapMessage.setVersion(soapVersion);
		cxfSoapMessage.setContent(SOAPMessage.class, soapMessage);
		cxfSoapMessage.put(Message.HTTP_REQUEST_METHOD, "POST");

		setJASPICValidator(wssConfig, clientSubject);
		cxfSoapMessage.put(WSSConfig.class.getName(), wssConfig);
	   
		for (Object key : options.keySet()) {
		   cxfSoapMessage.put((String)key, options.get(key));
		}
		//set the wss4j config from messageinfo
		if (messageInfo.getMap().get(JBossWSAuthConstants.WSS4J_CONFIG) != null) {
			Properties props = (Properties)messageInfo.getMap().get(JBossWSAuthConstants.WSS4J_CONFIG);
			for(Entry<Object, Object> e : props.entrySet()) {
	            cxfSoapMessage.put(e.getKey().toString(), e.getValue());
	        }
		}
		wss4jInterceptor.handleMessage(cxfSoapMessage);
		
		return true;
			
	}

	public AuthStatus secureResponse(MessageInfo arg0, Subject arg1)
			throws AuthException {
		throw new UnsupportedOperationException();
	}

	protected void setJASPICValidator(WSSConfig wssconfig, Subject subject) {
		UsernameTokenValidator usernameTokenValidator = new UsernameTokenValidator(subject);
		usernameTokenValidator.setContextName(getSecurityDomainName());
		wssconfig.setValidator(WSSecurityEngine.USERNAME_TOKEN, usernameTokenValidator);
	}

	protected String getSecurityDomainName() {
		if (this.securityDomainName != null)
			return securityDomainName;

		// Check if it is passed in the options
		String domainName = (String) options
				.get("javax.security.auth.login.LoginContext");
		if (domainName == null) {
			domainName = getClass().getName();
		}
		return domainName;
	}

}