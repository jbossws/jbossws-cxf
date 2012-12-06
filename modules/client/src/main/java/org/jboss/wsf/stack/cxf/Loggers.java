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
package org.jboss.wsf.stack.cxf;

import static org.jboss.logging.Logger.Level.DEBUG;
import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.TRACE;
import static org.jboss.logging.Logger.Level.WARN;

import java.net.URL;
import java.util.Collection;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * JBossWS-CXF log messages
 * 
 * @author alessio.soldano@jboss.com
 */
@MessageLogger(projectCode = "JBWS")
public interface Loggers extends BasicLogger
{
    Loggers ROOT_LOGGER = org.jboss.logging.Logger.getMessageLogger(Loggers.class, "org.jboss.ws.cxf");
    Loggers ADDRESS_REWRITE_LOGGER = org.jboss.logging.Logger.getMessageLogger(Loggers.class, "org.jboss.ws.cxf.endpointAddressRewrite");
    Loggers SECURITY_LOGGER = org.jboss.logging.Logger.getMessageLogger(Loggers.class, "org.jboss.ws.cxf.security");
    Loggers METADATA_LOGGER = org.jboss.logging.Logger.getMessageLogger(Loggers.class, "org.jboss.ws.cxf.metadata");
    Loggers DEPLOYMENT_LOGGER = org.jboss.logging.Logger.getMessageLogger(Loggers.class, "org.jboss.ws.cxf.deployment");
    
    @LogMessage(level = INFO)
    @Message(id = 24015, value = "Cannot use the bus associated to the current deployment for starting a new endpoint, creating a new bus...")
    void cannotUseCurrentDepBusForStartingNewEndpoint();
    
    @LogMessage(level = TRACE)
    @Message(id = 24016, value = "Unable to retrieve server config; this is an expected condition for jboss-modules enabled client.")
    void cannotRetrieveServerConfigIgnoreForClients(@Cause Throwable cause);
    
    @LogMessage(level = WARN)
    @Message(id = 24018, value = "Unable to retrieve port QName from %s, trying matching port using endpoint interface name only.")
    void cannotRetrievePortQNameTryingMatchingUsingEpInterface(String portName, @Cause Throwable cause);
    
    @LogMessage(level = WARN)
    @Message(id = 24027, value = "Spring initial application context creation failed using classloader %s, will try again after having switched the current thread context classloader to %s")
    void appContextCreationFailedWillTryWithNewTCCL(ClassLoader currentCL, ClassLoader newCL, @Cause Throwable cause);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24033, value = "Setting new service endpoint address in wsdl: %s")
    void settingNewServiceEndpointAddressInWsdl(String address);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24034, value = "WSDL service endpoint address rewrite required because of server configuration: %s")
    void addressRewriteRequiredBecauseOfServerConf(String address);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24035, value = "WSDL service endpoint address rewrite required because of invalid URL: %s")
    void addressRewriteRequiredBecauseOfInvalidAddress(String address);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24036, value = "WSDL service endpoint address rewrite not required: %s")
    void rewriteNotRequired(String address);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24037, value = "Rewritten new candidate WSDL service endpoint address '%s' to '%s'")
    void addressRewritten(String previousAddress, String address);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24038, value = "Invalid url '%s' provided, using original one without rewriting: %s")
    void invalidAddressProvidedUseItWithoutRewriting(String newAddress, String origAddress);
    
    @LogMessage(level = TRACE)
    @Message(id = 24039, value = "Spring namespace handler resolution: unable to resolve JBossWS specific handler for namespace '%s'; trying default namespace resolution...")
    void unableToResolveJBWSSpringNSHandler(String ns, @Cause Throwable cause);
    
    @LogMessage(level = TRACE)
    @Message(id = 24040, value = "About to authenticate, using security domain %s")
    void aboutToAuthenticate(String securityDomain);
    
    @LogMessage(level = TRACE)
    @Message(id = 24041, value = "Authenticated, principal=%s")
    void authenticated(String principal);
    
    @LogMessage(level = TRACE)
    @Message(id = 24042, value = "Security context propagated for principal %s")
    void securityContextPropagated(String principal);
    
    @LogMessage(level = ERROR)
    @Message(id = 24054, value = "User principal is not available on the current message")
    void userPrincipalNotAvailableOnCurrentMessage();
    
    @LogMessage(level = WARN)
    @Message(id = 24059, value = "%s cannot open stream for resource: %s")
    void cannotOpenStream(String callerClass, String resourcePath);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24060, value = "%s cannot resolve resource: %s")
    void cannotResolveResource(String callerClass, String resourcePath);
    
    @LogMessage(level = INFO)
    @Message(id = 24061, value = "Adding service endpoint metadata: %s")
    void addingServiceEndpointMetadata(Object o);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24062, value = "id %s, overriding portName %s with %s")
    void overridePortName(String id, QName portName, QName newPortName);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24063, value = "id %s, overriding portName %s with %s")
    void overrideServiceName(String id, QName serviceName, QName newServiceName);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24064, value = "id %s, enabling MTOM...")
    void enableMTOM(String id);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24065, value = "id %s, enabling Addressing...")
    void enableAddressing(String id);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24066, value = "id %s, enabling RespectBinding...")
    void enableRespectBinding(String id);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24067, value = "id %s, overriding wsdlFile location with %s")
    void overridingWsdlFileLocation(String id, String wsdlLoc);
    
    @LogMessage(level = WARN)
    @Message(id = 24068, value = "Handler chain deployment descriptor contribution: PortNamePattern, ServiceNamePattern and ProtocolBindings filters not supported; adding handlers anyway.")
    void filtersNotSupported();
    
    @LogMessage(level = WARN)
    @Message(id = 24069, value = "Init params not supported, handler: %s")
    void initParamsSupported(String handlerName);
    
    @LogMessage(level = ERROR)
    @Message(id = 24073, value = "Error registering bus for management: %s")
    void errorRegisteringBus(Bus bus, @Cause Throwable cause);
    
    @LogMessage(level = INFO)
    @Message(id = 24074, value = "WSDL published to: %s")
    void wsdlFilePublished(URL url);
    
    @LogMessage(level = WARN)
    @Message(id = 24077, value = "Cannot get wsdl publish location for null wsdl location and serviceName")
    void cannotGetWsdlPublishLocation();
    
    @LogMessage(level = WARN)
    @Message(id = 24078, value = "WSDL publisher not configured, unable to publish contract for endpoint class %s")
    void unableToPublishContractDueToMissingPublisher(Class<?> clazz);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24079, value = "JBossWS-CXF configuration generated: %s")
    void jbwscxfConfGenerated(URL url);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24080, value = "Actual configuration from file: %s")
    void actualConfFromFile(URL url);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24081, value = "JBossWS-CXF configuration found: %s")
    void jbwscxfConfFound(URL url);
    
    @LogMessage(level = TRACE)
    @Message(id = 24086, value = "Error while getting default WSSConfig")
    void errorGettingWSSConfig(@Cause Throwable cause);
    
    @LogMessage(level = WARN)
    @Message(id = 24087, value = "Could not early initialize security engine")
    void couldNotInitSecurityEngine();
    
    @LogMessage(level = TRACE)
    @Message(id = 24089, value = "Unable to load additional configuration from %s")
    void unableToLoadAdditionalConfigurationFrom(URL url, @Cause Throwable cause);
    
    @LogMessage(level = DEBUG)
    @Message(id = 24091, value = "Could not get WSDL from %s, aborting soap:address rewrite.")
    void abortSoapAddressRewrite(String wsdlLocation, @Cause Throwable cause);
}
