package org.jboss.test.ws.jaxws.cxf.jbws4430;

import jakarta.json.Json;
import jakarta.json.JsonObject;

import org.jboss.logging.Logger;

public class CredentialsCDIBean {

    private Logger logger = Logger.getLogger(CredentialsCDIBean.class);

    public JsonObject clientCredentials(String clientId, String clientSecret) {

        logger.info(
                "### TCCL in CredentialsCDIBean.clientCredentials = " + Thread.currentThread().getContextClassLoader().toString());

        return Json.createObjectBuilder().add("access_token", "exampleaccesstoken").build();
    }
}
