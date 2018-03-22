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

import java.io.File;
import java.net.URL;

import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.ws.WebServiceException;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.wsf.spi.WSFException;
import org.jboss.wsf.spi.deployment.Deployment;

/**
 * JBossWS-CXF exception messages
 * 
 * @author alessio.soldano@jboss.com
 */
@MessageBundle(projectCode = "JBWS")
public interface Messages {

    Messages MESSAGES = org.jboss.logging.Messages.getBundle(Messages.class);
    
    @Message(id = 24000, value = "Could not make directory: %s")
    IllegalStateException couldNotMakeDirectory(String dir);
    
    @Message(id = 24001, value = "Class not found: %s")
    IllegalArgumentException classNotFound(String clazz);
    
    @Message(id = 24002, value = "Failed to invoke %s")
    String failedToInvoke(String s);
    
    @Message(id = 24003, value = "Unsupported value '%s' for target, using default value '%s'")
    String unsupportedTargetUsingDefault(String target, String defTarget);
    
    @Message(id = 24004, value = "SOAP message could not be sent")
    SOAPException soapMessageCouldNotBeSent(@Cause Throwable cause);
    
    @Message(id = 24005, value = "GET request could not be sent")
    SOAPException getRequestCouldNotBeSent(@Cause Throwable cause);
    
    @Message(id = 24006, value = "Connection already closed!")
    SOAPException connectionAlreadyClosed();
    
    @Message(id = 24007, value = "Address object of type %s is not supported")
    SOAPException addressTypeNotSupported(Class<?> clazz);
    
    @Message(id = 24008, value = "No ConduitInitiator is available for %s")
    SOAPException noConduitInitiatorAvailableFor(String s);
    
    @Message(id = 24009, value = "No ConduitInitiator is available for %s")
    SOAPException noConduitInitiatorAvailableFor2(String s, @Cause Throwable cause);
    
    @Message(id = 24010, value = "SOAP message could not be read")
    SOAPException soapMessageCouldNotBeRead(@Cause Throwable cause);
    
    @Message(id = 24011, value = "Cannot send messages using a previously closed connection")
    SOAPException cantSendMessagesOnClosedConnection();
    
    @Message(id = 24012, value = "Unsupported MAPEndpoint: %s")
    IllegalArgumentException unsupportedMapEndpoin(Object o);
    
    @Message(id = 24013, value = "Invalid null endpoint reference")
    IllegalArgumentException invalidNullEndpointReference();
    
    @Message(id = 24014, value = "Unsupported MAP: %s")
    IllegalArgumentException unsupportedMap(Object o);
    
    @Message(id = 24017, value = "Unknown feature error: %s")
    WebServiceException unknownFeature(String f);
    
    @Message(id = 24019, value = "Apache CXF does not support JAX-RPC and a JAX-RPC service ref is requested with it; something is likely wrong with the user configuration or classpath")
    UnsupportedOperationException jaxrpcServiceRefNotSupported();
    
    @Message(id = 24020, value = "Handler config file not found: %s")
    WebServiceException handlerConfigFileNotFound(String file);
    
    @Message(id = 24021, value = "Error parsing %s, %s element expected, but found %s")
    WebServiceException differentElementExpected(String file, String exp, String found);
    
    @Message(id = 24022, value = "No handler-chain found while parsing: %s")
    WebServiceException noHandlerChainFound(String file, @Cause Throwable cause);
    
    @Message(id = 24023, value = "Error parsing %s, invalid element in handler: %s")
    WebServiceException invalidElementInHandler(String file, String el);
    
    @Message(id = 24024, value = "Error parsing %s, %s is not a valid QName pattern")
    WebServiceException notAQNamePattern(String file, String pattern);
    
    @Message(id = 24025, value = "Cannot resolve handler file %s on %s")
    WebServiceException cannotResolveHandlerFile(String file, String className);
    
    @Message(id = 24026, value = "%s is not a BusExtension instance")
    RuntimeException notABusExtensionInstance(Object obj);
    
    @Message(id = 24028, value = "Cannot obtain %s")
    ServletException cannotObtainRegistry(String registryInterfaceName);
    
    @Message(id = 24029, value = "Cannot obtain destination for %s")
    ServletException cannotObtainDestinationFor(String requestURI);
    
    @Message(id = 24030, value = "Cannot obtain destination factory for http transport")
    ServletException cannotObtainDestinationFactoryForHttpTransport(@Cause Throwable cause);
    
    @Message(id = 24031, value = "Cannot load class %s")
    IllegalStateException cannotLoadClass(String clazz);
    
    @Message(id = 24032, value = "Cannot obtain endpoint %s")
    WebServiceException cannotObtainEndpoint(ObjectName on);
    
//    @Message(id = 24043, value = "Class %s for namespace %s does not implement the %s interface")
//    Exception nsHandlerInterfaceNotImplemented(String s1, String s2, String s3);
    
//    @Message(id = 24044, value = "NamespaceHandler class %s for namespace %s not found")
//    Exception nsHandlerClassNotFound(String s1, String s2, @Cause Throwable cause);
    
//    @Message(id = 24045, value = "Invalid NamespaceHandler class %s for namespace %s : problem with handler class file or dependent class")
//    Exception nsHandlerInvalidClass(String s1, String s2, @Cause Throwable cause);
    
//    @Message(id = 24046, value = "Unable to load NamespaceHandler mappings from location [%s]")
//    IllegalStateException unableToLoadNSHandler(String location, @Cause Throwable cause);
    
    @Message(id = 24047, value = "Authentication failed, principal=%s")
    SecurityException authenticationFailed(String principal);
    
    @Message(id = 24048, value = "Request rejected since a stale timestamp has been provided: %s")
    SecurityException requestRejectedTimeStamp(String created);
    
    @Message(id = 24049, value = "Request rejected since a message with the same nonce has been recently received; nonce = %s")
    SecurityException requestRejectedSameNonce(String nonce);
    
    @Message(id = 24050, value = "DateTime value does not follow the format '[-]yyyy-mm-ddThh:mm:ss[.s+][timezone]': expected 'T' but got %s")
    IllegalArgumentException invalidDateTimeFormat(char c);
    
    @Message(id = 24051, value = "Date value does not follow the format '-'? yyyy '-' mm '-' dd: %s")
    IllegalArgumentException invalidDateValueFormat(String value);
    
    @Message(id = 24052, value = "Time value does not follow the format 'hh:mm:ss.[s+]': %s")
    IllegalArgumentException invalidTimeValueFormat(String value);
    
    @Message(id = 24053, value = "Timezone value does not follow the format ([+/-]HH:MM): %s")
    NumberFormatException invalidTimeZoneValueFormat(String value);
    
    @Message(id = 24055, value = "Unsupported token type: %s")
    SecurityException unsupportedTokenType(Object tokenType);
    
    @Message(id = 24056, value = "Could not get subject info neither from Security Token in the current message nor directly from computed SecurityContext")
    SecurityException couldNotGetSubjectInfo();
    
    @Message(id = 24057, value = "Failed Authentication : Subject has not been created")
    SecurityException authenticationFailedSubjectNotCreated(@Cause Throwable cause);
    
    @Message(id = 24058, value = "Failed Authentication : Invalid Subject")
    SecurityException authenticationFailedSubjectInvalid();
    
    @Message(id = 24070, value = "Runtime loader cannot be null; deployment: %s")
    IllegalStateException runtimeLoaderCannotBeNull(Deployment dep);
    
    @Message(id = 24071, value = "@WebService annotation not found on %s")
    RuntimeException webserviceAnnotationNotFound(String sei);
    
    @Message(id = 24072, value = "@WebService cannot have attribute 'portName', 'serviceName', 'endpointInterface' on %s")
    RuntimeException webserviceAnnotationSEIAttributes(String sei);
    
    @Message(id = 24075, value = "WSDL 2.0 not supported")
    RuntimeException wsdl20NotSupported();
    
    @Message(id = 24076, value = "Service %s, cannot publish wsdl to: %s")
    RuntimeException cannotPublishWSDLTo(QName serviceName, File file, @Cause Throwable cause);
    
    @Message(id = 24083, value = "Endpoint %s is not defined in jbossws-cxf.xml")
    IllegalStateException endpointNotDefineInJbwsCxf(String ep);
    
    @Message(id = 24084, value = "Underlying bus is already configured for JBossWS use: %s")
    IllegalStateException busAlreadyConfigured(Object ctx);
    
    @Message(id = 24085, value = "Unable to load configuration from %s")
    RuntimeException unableToLoadConfigurationFrom(URL url, @Cause Throwable cause);
    
    @Message(id = 24088, value = "Cannot load additional config from null location")
    IllegalArgumentException unableToLoadAdditionalConfigFromNull();
    
    @Message(id = 24093, value = "Error parsing policy attachment: %s")
    RuntimeException errorParsingPolicyAttachment(String uri, @Cause Throwable cause);
    
    @Message(id = 24094, value = "Authorization failed, principal=%s")
    SecurityException authorizationFailed(String principal);
    
    @Message(id = 24096, value = "Multiple incompatible JAXWS client Bus features provided")
    IllegalArgumentException incompatibleJAXWSClientBusFeatureProvided();

    @Message(id = 24104, value = "Service class %s is missing required JAX-WS 2.2 additional constructors")
    WSFException missingJAXWS22ServiceConstructor(String className, @Cause Throwable cause);

    @Message(id = 24108, value = "Invalid request received:bindingOperation and dispatched method are missing for service implementation invocation")
    IllegalArgumentException missingBindingOpeartionAndDispatchedMethod();

    @Message(id = 24109, value = "Could not get WSDL contract for endpoint %s at %s")
    WSFException couldNotFetchWSDLContract(String endpoint, String wsdlLocation);

    @Message(id = 24111, value = "Could not load modules %s")
    String couldNotLoadModules(String msg);
}
