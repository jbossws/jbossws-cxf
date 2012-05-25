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
   public static final String JBOSSWS_CXF_SPRING_DD = "jbossws-cxf.xml";
   
   public static final String CXF_QUEUE_PREFIX = "cxf.queue.";
   public static final String CXF_QUEUE_MAX_QUEUE_SIZE_PROP = "maxQueueSize";
   public static final String CXF_QUEUE_INITIAL_THREADS_PROP = "initialThreads";
   public static final String CXF_QUEUE_HIGH_WATER_MARK_PROP = "highWaterMark";
   public static final String CXF_QUEUE_LOW_WATER_MARK_PROP = "lowWaterMark";
   public static final String CXF_QUEUE_DEQUEUE_TIMEOUT_PROP = "dequeueTimeout";
   public static final String CXF_POLICY_ALTERNATIVE_SELECTOR = "cxf.policy.alternativeSelector";
}
