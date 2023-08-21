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
