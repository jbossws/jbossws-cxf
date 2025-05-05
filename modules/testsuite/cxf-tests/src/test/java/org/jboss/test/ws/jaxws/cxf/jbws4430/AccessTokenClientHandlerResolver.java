package org.jboss.test.ws.jaxws.cxf.jbws4430;

import java.util.ArrayList;
import java.util.List;

import org.jboss.logging.Logger;

import javax.enterprise.inject.spi.CDI;
import javax.json.JsonObject;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.handler.PortInfo;

public class AccessTokenClientHandlerResolver implements HandlerResolver {

    private Logger logger = Logger.getLogger(AccessTokenClientHandlerResolver.class);

    String clientId;
    String clientSecret;

    public AccessTokenClientHandlerResolver(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    @Override
    public List<Handler> getHandlerChain(PortInfo portInfo) {

        logger.info("### TCCL in AccessTokenClientHandlerResolver.getHandlerChain = "
                + Thread.currentThread().getContextClassLoader().toString());
        List<Handler> handlers = new ArrayList<>();
        handlers.add(new AccessTokenClientHandler(getAccessToken()));
        return handlers;
    }

    protected String getAccessToken() {

        logger.info("### TCCL in AccessTokenClientHandlerResolver= " + Thread.currentThread().getContextClassLoader().toString());
        logger.info("### this.classloader in AccessTokenClientHandlerResolver= " + this.getClass().getClassLoader());

        logger.info("### CDI.current() = " + CDI.current());

        CredentialsCDIBean credentialsBean = CDI.current().select(CredentialsCDIBean.class).get();

        logger.info("### credentialsBean = " + credentialsBean);

        JsonObject json = credentialsBean.clientCredentials(clientId, clientSecret);
        return json.getString("access_token");
    }
}
