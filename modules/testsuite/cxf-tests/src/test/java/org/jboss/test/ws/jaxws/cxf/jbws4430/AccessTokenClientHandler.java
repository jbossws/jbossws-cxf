package org.jboss.test.ws.jaxws.cxf.jbws4430;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;

import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.handler.soap.SOAPHandler;
import jakarta.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;

public class AccessTokenClientHandler implements SOAPHandler<SOAPMessageContext> {

    private Logger logger = Logger.getLogger(AccessTokenClientHandler.class);

    String accessToken;

    public AccessTokenClientHandler(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public boolean handleMessage(SOAPMessageContext smc) {

        logger.info(
                "### TCCL in AccessTokenClientHandler.handleMessage = " + Thread.currentThread().getContextClassLoader().toString());
        logger.info("### CLI in AccessTokenClientHandler.handleMessage = " + this.getClass().getClassLoader());

        Boolean outboundProperty = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

        if (outboundProperty.booleanValue()) {
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("Authorization", Arrays.asList("Bearer " + accessToken));
            smc.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
        }

        return outboundProperty;
    }

    @Override
    public Set<QName> getHeaders() {
        return Collections.emptySet();
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

    @Override
    public void close(MessageContext context) {
    }
}
