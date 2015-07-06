/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.wsf.stack.cxf.interceptor;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.cxf.binding.soap.interceptor.EndpointSelectionInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.Interceptor;
import org.apache.cxf.interceptor.OutgoingChainInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.Phase;
import org.jboss.wsf.spi.deployment.Endpoint;
/**
 * Interceptor to get json format endpoint config info. This interceptor is only 
 * responds to get url like http://localhost:8080/context/wsendpoint/management?config
 *@author <a href="mailto:ema@redhat.com>Jim Ma</a>
 *
 */

public class EndpointConfigsGetInterceptor extends AbstractMangementInInterceptor {
    public static final EndpointConfigsGetInterceptor INSTANCE = new EndpointConfigsGetInterceptor();
    public static final String ENDPOINT_CONFIGS = EndpointConfigsGetInterceptor.class.getName() + ".EndpointConfigs";
    public static final Set<String> httpMethods = new HashSet<String>(4);
    private Interceptor<Message> configsOutInteceptor = EndpointConfigsGetOutIntercetpor.INSTANCE;
    private static final String TRANSFORM_SKIP = "transform.skip";
    static {
       httpMethods.add("GET");
       httpMethods.add("POST");
    }
    public EndpointConfigsGetInterceptor() {
        super(Phase.READ);
        this.addAfter(EndpointSelectionInterceptor.class.getName());
    }
    
    public EndpointConfigsGetInterceptor(Interceptor<Message> outInterceptor) {
        this();
        // Let people override the EndpointConfigsGetOutIntercetpor 
        configsOutInteceptor = outInterceptor;
    }
    
    public void handleMessage(Message message) throws Fault {
        if (!isAllowed(message)) {
           return;
        }
        if (isRecognizedQuery(getQueryMap(message))) {
            Message mout = this.createOutMessage(message);
            Endpoint endpoint = message.getExchange().get(Endpoint.class);
            mout.put(ENDPOINT_CONFIGS, endpoint.getAllConfigsMap());
            mout.put(Message.CONTENT_TYPE, "text/xml");
            // just remove the interceptor which should not be used
            cleanUpOutInterceptors(mout);
            // notice this is being added after the purge above, don't swap the order!
            
            mout.getInterceptorChain().add(configsOutInteceptor);
            message.getExchange().put(TRANSFORM_SKIP, Boolean.TRUE);
            // skip the service executor and goto the end of the chain.
            message.getInterceptorChain().doInterceptStartingAt(
                    message,
                    OutgoingChainInterceptor.class.getName());
        }
    }
    

    private boolean isRecognizedQuery(Map<String, String> map) {
        if (map.containsKey("config") && map.size() == 1) {
            return true;
        }
        return false;
    }

   @Override
   Set<String> getAllowedMethod()
   {
     return httpMethods;
   }
    

}