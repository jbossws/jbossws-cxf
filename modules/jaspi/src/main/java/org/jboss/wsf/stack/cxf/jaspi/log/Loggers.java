/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.wsf.stack.cxf.jaspi.log;

import static org.jboss.logging.Logger.Level.WARN;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * JBossWS-CXF log messages
 *
 * @author alessio.soldano@jboss.com
 */
@SuppressWarnings("deprecation")
@MessageLogger(projectCode = "JBWS")
public interface Loggers extends BasicLogger
{
    Loggers ROOT_LOGGER = org.jboss.logging.Logger.getMessageLogger(Loggers.class, "org.jboss.ws.cxf");
    Loggers DEPLOYMENT_LOGGER = org.jboss.logging.Logger.getMessageLogger(Loggers.class, "org.jboss.ws.cxf.deployment");

    @LogMessage(level = WARN)
    @Message(id = 24201, value = "No application policy found for security domain '%s'")
    void noApplicationPolicy(String securityDomain);
    
    @LogMessage(level = WARN)
    @Message(id = 24202, value = "No JASPIAuthenticationInfo found for security domain '%s'")
    void noJaspiApplicationPolicy(String securityDomain);
    
    @LogMessage(level = WARN)
    @Message(id = 24203, value = "Can not create Jaspi ServerAuthContext for security domain '%s'")
    void cannotCreateServerAuthContext(String securityDomain, @Cause Throwable cause);
    
    @LogMessage(level = WARN)
    @Message(id = 24204, value = "Can not enable Jaspi authentication for '%s' instance")
    void cannotEnableJASPIAuthentication(String classname);
}
