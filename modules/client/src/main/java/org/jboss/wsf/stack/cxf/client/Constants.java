/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.client;

/**
 * JBossWS-CXF integration constants
 * 
 * @author alessio.soldano@jboss.com
 * @since 27-Aug-2010
 *
 */
public class Constants
{
   public static final String DEPLOYMENT_BUS = "deployment-bus";
   
   public static final String CXF_QUEUE_PREFIX = "cxf.queue.";
   public static final String CXF_QUEUE_MAX_QUEUE_SIZE_PROP = "maxQueueSize";
   public static final String CXF_QUEUE_INITIAL_THREADS_PROP = "initialThreads";
   public static final String CXF_QUEUE_HIGH_WATER_MARK_PROP = "highWaterMark";
   public static final String CXF_QUEUE_LOW_WATER_MARK_PROP = "lowWaterMark";
   public static final String CXF_QUEUE_DEQUEUE_TIMEOUT_PROP = "dequeueTimeout";
   public static final String CXF_POLICY_ALTERNATIVE_SELECTOR_PROP = "cxf.policy.alternativeSelector";
   public static final String CXF_IN_INTERCEPTORS_PROP = "cxf.interceptors.in";
   public static final String CXF_OUT_INTERCEPTORS_PROP = "cxf.interceptors.out";
   public static final String CXF_IN_FAULT_INTERCEPTORS_PROP = "cxf.interceptors.infault";
   public static final String CXF_OUT_FAULT_INTERCEPTORS_PROP = "cxf.interceptors.outfault";
   public static final String CXF_FEATURES_PROP = "cxf.features";
   public static final String CXF_MANAGEMENT_ENABLED = "cxf.management.enabled";
   public static final String CXF_MANAGEMENT_INSTALL_RESPONSE_TIME_INTERCEPTORS = "cxf.management.installResponseTimeInterceptors";
   public static final String CXF_WS_DISCOVERY_ENABLED = "cxf.ws-discovery.enabled";
   public static final String JBWS_CXF_DISABLE_HANDLER_AUTH_CHECKS = "org.jboss.ws.cxf.disableHandlerAuthChecks";
   public static final String JBWS_CXF_NO_LOCAL_BC = "org.jboss.ws.cxf.noLocalBC";
   public static final String CXF_CLIENT_ALLOW_CHUNKING = "cxf.client.allowChunking";
   public static final String CXF_CLIENT_CHUNKING_THRESHOLD = "cxf.client.chunkingThreshold";
   public static final String CXF_CLIENT_CONNECTION_TIMEOUT = "cxf.client.connectionTimeout";
   public static final String CXF_CLIENT_RECEIVE_TIMEOUT = "cxf.client.receiveTimeout";
   public static final String CXF_CLIENT_CONNECTION = "cxf.client.connection";
   public static final String CXF_TLS_CLIENT_DISABLE_CN_CHECK = "cxf.tls-client.disableCNCheck";
   
   public static final String JBWS_CXF_JAXWS_CLIENT_BUS_STRATEGY = "org.jboss.ws.cxf.jaxws-client.bus.strategy";
   public static final String THREAD_BUS_STRATEGY = "THREAD_BUS";
   public static final String NEW_BUS_STRATEGY = "NEW_BUS";
   public static final String TCCL_BUS_STRATEGY = "TCCL_BUS";
   public static final String JBWS_CXF_JAXWS_CLIENT_BUS_SELECTOR = "org.jboss.ws.cxf.jaxws-client.bus.selector";
   public static final String JBWS_CXF_DISABLE_DEPLOYMENT_USER_DEFAULT_THREAD_BUS = "org.jboss.ws.cxf.disable-deployment-user-default-thread-bus";
   public static final String JBWS_CXF_DISABLE_SCHEMA_CACHE = "org.jboss.ws.cxf.disableSchemaCache";
}
