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
package org.jboss.wsf.stack.cxf.jaspi.validator;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;

import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.handler.RequestData;
import org.apache.ws.security.message.token.UsernameToken;
import org.apache.ws.security.validate.Credential;
import org.apache.ws.security.validate.JAASUsernameTokenValidator;
/** 
 * @author <a href="ema@redhat.com">Jim Ma</a>
 */
public class UsernameTokenValidator extends JAASUsernameTokenValidator {
	
	private Subject subject;
	
	public UsernameTokenValidator(Subject subject) {
		this.subject = subject;
	}
	
	//wss4j's JAASUsernameTokenValidator only supports plain text password
	//TODO: support other type password
/*    private static org.apache.commons.logging.Log log = 
            org.apache.commons.logging.LogFactory.getLog(UsernameTokenValidator.class);*/
    public Credential validate(Credential credential, RequestData data) throws WSSecurityException {
        if (credential == null || credential.getUsernametoken() == null) {
            throw new WSSecurityException(WSSecurityException.FAILURE, "noCredential");
        }
        
        String user = null;
        String password = null;
        
        UsernameToken usernameToken = credential.getUsernametoken();
        
        user = usernameToken.getName();
        String pwType = usernameToken.getPasswordType();
        
        if (usernameToken.isHashed()) {
            throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION);
        }
        
        password = usernameToken.getPassword();
        
        if (!WSConstants.PASSWORD_TEXT.equals(pwType)) {
            throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION);        	
        }
        
        if (!(user != null && user.length() > 0 && password != null && password.length() > 0)) {
            throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION);
        }
        
        try {
            CallbackHandler handler = getCallbackHandler(user, password);  
            LoginContext ctx = new LoginContext(getContextName(), subject , handler);           
            ctx.login();
            Subject subject = ctx.getSubject();
            credential.setSubject(subject);

        } catch (LoginException ex) {
            throw new WSSecurityException(
                WSSecurityException.FAILED_AUTHENTICATION, null, null, ex
            );
        }
        
        return credential;
        
    }
}
