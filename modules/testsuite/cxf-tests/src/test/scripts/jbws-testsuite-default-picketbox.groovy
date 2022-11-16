def root = new XmlParser().parse(inputFile)

/**
 * Fix logging: optionally remove CONSOLE handler and set a specific log file
 *
 */

def logHandlers = root.profile.subsystem.'root-logger'.handlers[0]
def consoleHandler = logHandlers.find{it.@name == 'CONSOLE'}
if (!session.userProperties['enableServerLoggingToConsole'] && !project.properties['enableServerLoggingToConsole']) logHandlers.remove(consoleHandler)
def file = root.profile.subsystem.'periodic-rotating-file-handler'.file[0]
file.attributes()['path'] = serverLog

/** -------------------- legacy security subsystem ------------------------
 * NOTE: that subsystem will be removed in future versions.
 *
 *  Config for test WSTrustTestCase
 *  <security-domain name="JBossWS-trust-sts" cache-type="default">
 *   <authentication>
 *     <login-module code="UsersRoles" flag="required">
 *       <module-option name="usersProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-resources/jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-users.properties"/>
 *       <module-option name="unauthenticatedIdentity" value="anonymous"/>
 *       <module-option name="rolesProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-resources/jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-roles.properties"/>
 *     </login-module>
 *   </authentication>
 * </security-domain>
 */
def subsystemsLegacy = root.profile.subsystem
def securityDomainsLegacy = null
for (item in subsystemsLegacy) {
    if (item.name().getNamespaceURI().contains("urn:jboss:domain:security:")) {
        for (element in item) {
            if (element.name().getLocalPart() == 'security-domains') {
                securityDomainsLegacy = element
            }
        }
        break
    }
}
def securityDomainSts = securityDomainsLegacy.appendNode('security-domain', ['name':'JBossWS-trust-sts','cache-type':'default'])
def authenticationSts = securityDomainSts.appendNode('authentication')
def loginModuleSts = authenticationSts.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleSts.appendNode('module-option', ['name':'unauthenticatedIdentity','value':'anonymous'])
loginModuleSts.appendNode('module-option', ['name':'usersProperties','value':testResourcesDir + '/jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-users.properties'])
loginModuleSts.appendNode('module-option', ['name':'rolesProperties','value':testResourcesDir + '/jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-roles.properties'])

/** -------------------- legacy security subsystem ------------------------
 * NOTE: that subsystem will be removed in future versions.
 *
 * Add two security-domain blocks for JASPI test JaspiAuthenticationTestCase.
 *
 * <security-domain name="jaspi">
 *   <authentication-jaspi>
 *     <login-module-stack name="jaas-lm-stack">
 *       <login-module code="UsersRoles" flag="required">
 *         <module-option name="usersProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-users.properties"/>
 *         <module-option name="rolesProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-roles.properties"/>
 *       </login-module>
 *     </login-module-stack>
 *     <auth-module code="org.jboss.wsf.stack.cxf.jaspi.module.UsernameTokenServerAuthModule" login-module-stack-ref="jaas-lm-stack"/>
 *   </authentication-jaspi>
 * </security-domain>
 * <security-domain name="clientJaspi">
 *   <authentication-jaspi>
 *     <login-module-stack name="jaas-lm-stack">
 *       <login-module code="UsersRoles" flag="required">
 *         <module-option name="usersProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-users.properties"/>
 *         <module-option name="rolesProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-roles.properties"/>
 *       </login-module>
 *     </login-module-stack>
 *   <auth-module code="org.jboss.wsf.stack.cxf.jaspi.client.module.SOAPClientAuthModule" login-module-stack-ref="jaas-lm-stack"/>
 * </authentication-jaspi>
 */
def securityDomainJaspi = securityDomainsLegacy.appendNode('security-domain', ['name':'jaspi'])
def authenticationJaspi = securityDomainJaspi.appendNode('authentication-jaspi')
def loginModuleStack = authenticationJaspi.appendNode('login-module-stack', ['name':'jaas-lm-stack'])
def loginModuleJaspi = loginModuleStack.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleJaspi.appendNode('module-option', ['name':'usersProperties','value':usersPropFile])
loginModuleJaspi.appendNode('module-option', ['name':'rolesProperties','value':rolesPropFile])
authenticationJaspi.appendNode('auth-module', ['code':'org.jboss.wsf.stack.cxf.jaspi.module.UsernameTokenServerAuthModule','login-module-stack-ref':'jaas-lm-stack'])

def securityDomainJaspiClient = securityDomainsLegacy.appendNode('security-domain', ['name':'clientJaspi'])
def authenticationJaspiClient = securityDomainJaspiClient.appendNode('authentication-jaspi')
def loginModuleStackClient = authenticationJaspiClient.appendNode('login-module-stack', ['name':'jaas-lm-stack'])
def loginModuleJaspiClient = loginModuleStackClient.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleJaspiClient.appendNode('module-option', ['name':'usersProperties','value':usersPropFile])
loginModuleJaspiClient.appendNode('module-option', ['name':'rolesProperties','value':rolesPropFile])
authenticationJaspiClient.appendNode('auth-module', ['code':'org.jboss.wsf.stack.cxf.jaspi.client.module.SOAPClientAuthModule','login-module-stack-ref':'jaas-lm-stack'])

/** -------------------- legacy security subsystem ------------------------
 * NOTE: that subsystem will be removed in future versions.
 * Add a security-domain block for test PicketboxUsernameAuthorizationDigestEjbTestCase
 *
 * <security-domain name="PicketboxJBossWSDigest" cache-type="default">
 *   <authentication>
 *     <login-module code="UsersRoles" flag="required">
 *       <module-option name="hashUserPassword" value="false"/>
 *       <module-option name="hashCharset" value="UTF-8"/>
 *       <module-option name="usersProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-resources/jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-users.properties"/>
 *       <module-option name="hashAlgorithm" value="SHA"/>
 *       <module-option name="unauthenticatedIdentity" value="anonymous"/>
 *       <module-option name="hashEncoding" value="BASE64"/>
 *       <module-option name="rolesProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-resources/jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-roles.properties"/>
 *       <module-option name="storeDigestCallback" value="org.jboss.wsf.stack.cxf.security.authentication.callback.UsernameTokenCallback"/>
 *       <module-option name="hashStorePassword" value="true"/>
 *     </login-module>
 *   </authentication>
 * </security-domain>
 *
 */
def securityDomainDigest = securityDomainsLegacy.appendNode('security-domain', ['name':'PicketboxJBossWSDigest','cache-type':'default'])
def authenticationDigest = securityDomainDigest.appendNode('authentication')
def loginModuleDigest = authenticationDigest.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleDigest.appendNode('module-option', ['name':'hashUserPassword','value':'false'])
loginModuleDigest.appendNode('module-option', ['name':'hashCharset','value':'UTF-8'])
loginModuleDigest.appendNode('module-option', ['name':'hashAlgorithm','value':'SHA'])
loginModuleDigest.appendNode('module-option', ['name':'hashEncoding','value':'BASE64'])
loginModuleDigest.appendNode('module-option', ['name':'storeDigestCallback','value':'org.jboss.wsf.stack.cxf.security.authentication.callback.UsernameTokenCallback'])
loginModuleDigest.appendNode('module-option', ['name':'hashStorePassword','value':'true'])
loginModuleDigest.appendNode('module-option', ['name':'unauthenticatedIdentity','value':'anonymous'])
loginModuleDigest.appendNode('module-option', ['name':'usersProperties','value':testResourcesDir + '/jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-users.properties'])
loginModuleDigest.appendNode('module-option', ['name':'rolesProperties','value':testResourcesDir + '/jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-roles.properties'])





/**
 * Add a https connector like this:
 *
 * <security-realm name="jbws-test-https-realm">
 *    <server-identities>
 *        <ssl>
 *             <keystore path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/test.keystore" keystore-password="changeit" alias="tomcat"/>
 *        </ssl>
 *    </server-identities>
 * </security-realm>
 *
 */
def mgtSecurityRealms = root.management.'security-realms'[0]
def mgtSecurityRealm = mgtSecurityRealms.appendNode('security-realm', ['name':'jbws-test-https-realm'])
def mgtServerIdentities = mgtSecurityRealm.appendNode('server-identities')
def mgtSsl = mgtServerIdentities.appendNode('ssl')
mgtSsl.appendNode('keystore', ['path':keystorePath,'keystore-password':'changeit','alias':'tomcat'])

/**
 * Helper method to get subsystem element by xmlns prefix
 */
private getSubsystem(root, xmlnsPrefix) {
  for (item in root.profile.subsystem) {
    if (item.name().getNamespaceURI().startsWith(xmlnsPrefix)) {
      return item;
    }
  }
}

/**
 * Add a security-domain block like this:
 *
 *        <subsystem xmlns="urn:wildfly:elytron:1.0">
 *           <security-domains>
 *                <security-domain name="JBossWS" default-realm="JBossWS" permission-mapper="login-permission-mapper" role-mapper="combined-role-mapper">
 *                   <realm name="JBossWS" role-decoder="groups-to-roles"/>
 *               </security-domain>
 *               <security-domain name="ws-basic-domain" default-realm="ws-basic-domain" permission-mapper="login-permission-mapper" role-mapper="combined-role-mapper">
 *                   <realm name="ws-basic-domain" role-decoder="groups-to-roles"/>
 *               </security-domain>
 *           </security-domains>
 * 
 *
 */
def securitySubsystem =  getSubsystem(root, "urn:wildfly:elytron:")
def securityDomains = null
for (element in securitySubsystem) {
  if (element.name().getLocalPart() == 'security-domains') {
    securityDomains = element
  }
}
def securityDomain = securityDomains.appendNode('security-domain', ['name':'JBossWS','default-realm':'JBossWS','permission-mapper':'default-permission-mapper'])
def realm = securityDomain.appendNode('realm',['name':'JBossWS','role-decoder':'groups-to-roles'])

def basicsecurityDomain = securityDomains.appendNode('security-domain', ['name':'ws-basic-domain','default-realm':'ws-basic-domain','permission-mapper':'default-permission-mapper'])
def basicrealm = basicsecurityDomain.appendNode('realm',['name':'ws-basic-domain','role-decoder':'groups-to-roles'])

def digestDomain = securityDomains.appendNode('security-domain', ['name':'ws-digest-domain','default-realm':'ws-digest-domain','permission-mapper':'default-permission-mapper'])
def digestRefRealm = digestDomain.appendNode('realm',['name':'ws-digest-domain','role-decoder':'groups-to-roles'])

def legacyDomain = securityDomains.appendNode('security-domain', ['name':'JAASJBossWS','default-realm':'JAASJBossWSRealm','permission-mapper':'default-permission-mapper'])
def jaasJBossWSRealm = legacyDomain.appendNode('realm',['name':'JAASJBossWSRealm','role-decoder':'groups-to-roles'])

def legacyDigestDomain = securityDomains.appendNode('security-domain', ['name':'JBossWSDigest','default-realm':'JAASJBossWSDigestRealm','permission-mapper':'default-permission-mapper'])
def jaasJBossWDigestRealm = legacyDigestDomain.appendNode('realm',['name':'JAASJBossWSDigestRealm','role-decoder':'groups-to-roles'])


/**
 *            <security-realms>
 *               <properties-realm name="JBossWS">
 *                   <users-properties path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-users.properties"/>
 *                   <groups-properties path="application-roles.properties" relative-to="jboss.server.config.dir"/>
 *               </properties-realm>
 *               <properties-realm name="ws-basic-domain">
 *                   <users-properties path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/ws-users.properties"/>
 *                   <groups-properties path="application-roles.properties"/>
 *               </properties-realm>
 *            </security-realms>
 *
 *
 */
def securityRealms = root.profile.subsystem.'security-realms'[0]

def propertiesRealm = securityRealms.appendNode('properties-realm', ['name':'JBossWS'])
def usersProperties = propertiesRealm.appendNode('users-properties',['path': testResourcesDir + '/jaxws/samples/wsse/policy/jaas/ejb-digest/META-INF/jbossws-users.properties', 'plain-text':'true'])
def groupsProperties = propertiesRealm.appendNode('groups-properties',['path': testResourcesDir + '/jaxws/samples/wsse/policy/jaas/ejb-digest/META-INF/jbossws-roles.properties'])

def basicPropertiesRealm = securityRealms.appendNode('properties-realm', ['name':'ws-basic-domain'])
def basicUsersProperties = basicPropertiesRealm.appendNode('users-properties',['path': testResourcesDir + '/jaxws/cxf/httpauth/WEB-INF/ws-users.properties', 'plain-text':'true'])
def basicGroupsProperties = basicPropertiesRealm.appendNode('groups-properties',['path': testResourcesDir + '/jaxws/cxf/httpauth/WEB-INF/ws-roles.properties'])

def digestRealm = securityRealms.appendNode('properties-realm', ['name':'ws-digest-domain'])
def digestUserProperties = digestRealm.appendNode('users-properties',['path': testResourcesDir + '/jaxws/cxf/httpauth/WEB-INF/ws-digest-users.properties'])
def digestGroupsProperties = digestRealm.appendNode('groups-properties',['path': testResourcesDir + '/jaxws/cxf/httpauth/WEB-INF/ws-roles.properties'])

def jbossWSDigestRealm = securityRealms.appendNode('properties-realm', ['name':'JAASJBossWSDigestRealm'])
def jbossWSDigestUserProperties = jbossWSDigestRealm.appendNode('users-properties',['path': testResourcesDir + '/jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-users.properties', 'plain-text':'true'])
def jbossWSDigestGroupsProperties = jbossWSDigestRealm.appendNode('groups-properties',['path': testResourcesDir + '/jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-roles.properties'])

def jAASJBossWSRealm = securityRealms.appendNode('properties-realm', ['name':'JAASJBossWSRealm'])
def jAASJBossWSRealmUserProperties = jAASJBossWSRealm.appendNode('users-properties',['path':usersPropFile, 'plain-text':'true'])
def jAASJBossWSRealmGroupsProperties = jAASJBossWSRealm.appendNode('groups-properties',['path':rolesPropFile])

/**
 *             <http>
 *               <http-authentication-factory name="JBossWS" http-server-mechanism-factory="global" security-domain="JBossWS">
 *                   <mechanism-configuration>
 *                       <mechanism mechanism-name="BASIC">
 *                           <mechanism-realm realm-name="JBossWS Realm"/>
 *                       </mechanism>
 *                   </mechanism-configuration>
 *               </http-authentication-factory>
 *               <http-authentication-factory name="ws-basic-domain" http-server-mechanism-factory="global" security-domain="ws-basic-domain">
 *                   <mechanism-configuration>
 *                       <mechanism mechanism-name="BASIC">
 *                           <mechanism-realm realm-name="ws-basic-domain"/>
 *                       </mechanism>
 *                   </mechanism-configuration>
 *               </http-authentication-factory>
 *
 *
 */
def httpAuthen = null
for (element in securitySubsystem) {
    if (element.name().getLocalPart() == 'http') {
       httpAuthen = element
       break
    }
}
def httpAuthenticationFactory = httpAuthen.appendNode('http-authentication-factory', ['name':'JBossWS','http-server-mechanism-factory':'global', 'security-domain':'JBossWS'])
def mechanismConfiguration = httpAuthenticationFactory.appendNode('mechanism-configuration')
def mechanism = mechanismConfiguration.appendNode('mechanism',['mechanism-name':'BASIC'])
def mechanismRealm=mechanism.appendNode('mechanism-realm',['realm-name':'JBossWS'])

def basicHttpAuthenticationFactory = httpAuthen.appendNode('http-authentication-factory', ['name':'ws-basic-domain','http-server-mechanism-factory':'global', 'security-domain':'ws-basic-domain'])
def basicMechanismConfiguration = basicHttpAuthenticationFactory.appendNode('mechanism-configuration')
def basicMechanism = basicMechanismConfiguration.appendNode('mechanism',['mechanism-name':'BASIC'])
def basicmechanismRealm = basicMechanism.appendNode('mechanism-realm',['realm-name':'ws-basic-domain'])

def digestHttpAuthenticationFactory = httpAuthen.appendNode('http-authentication-factory', ['name':'ws-digest-domain','http-server-mechanism-factory':'global', 'security-domain':'ws-digest-domain'])
def digestMechanismConfiguration = digestHttpAuthenticationFactory.appendNode('mechanism-configuration')
def digestMechanism = digestMechanismConfiguration.appendNode('mechanism',['mechanism-name':'DIGEST'])
def digestMechanismRealm = digestMechanism.appendNode('mechanism-realm',['realm-name':'ws-digest-domain'])


def wsDigestHttpAuthenticationFactory = httpAuthen.appendNode('http-authentication-factory', ['name':'JBossWSDigest','http-server-mechanism-factory':'global', 'security-domain':'JBossWSDigest'])
def wsDigestMechanismConfiguration = wsDigestHttpAuthenticationFactory.appendNode('mechanism-configuration')
def wsDigestMechanism = wsDigestMechanismConfiguration.appendNode('mechanism',['mechanism-name':'BASIC'])
def wsDigestMechanismRealm = wsDigestMechanism.appendNode('mechanism-realm',['realm-name':'JAASJBossWSDigestRealm'])


def jassWsHttpAuthenticationFactory = httpAuthen.appendNode('http-authentication-factory', ['name':'JAASJBossWS','http-server-mechanism-factory':'global', 'security-domain':'JAASJBossWS'])
def jassWsMechanismConfiguration = jassWsHttpAuthenticationFactory.appendNode('mechanism-configuration')
def jassWsMechanism = jassWsMechanismConfiguration.appendNode('mechanism',['mechanism-name':'BASIC'])
def jassWsMechanismRealm = jassWsMechanism.appendNode('mechanism-realm',['realm-name':'JAASJBossWSRealm'])


/**
 *           <application-security-domains>
 *               <application-security-domain name="JBossWS" http-authentication-factory="JBossWS"/>
 *               <application-security-domain name="ws-basic-domain" http-authentication-factory="JBossWS"/>
 *           </application-security-domains>
 */
def ejbSubsystem = getSubsystem(root, "urn:jboss:domain:ejb3:")
def appSecurityDomains = null
for (element in ejbSubsystem) {
    if (element.name().getLocalPart() == 'application-security-domains') {
        appSecurityDomains = element
    }
}
if (appSecurityDomains == null) {
    appSecurityDomains = ejbSubsystem.appendNode('application-security-domains')
}

def ejbSecurityDomain1 = appSecurityDomains.appendNode('application-security-domain', ['name':'JBossWS','security-domain':'JBossWS'])
def ejbSecurityDomain3 = appSecurityDomains.appendNode('application-security-domain', ['name':'ws-basic-domain','security-domain':'ws-basic-domain'])

//add to undertow
def undertowSubsystem = getSubsystem(root, "urn:jboss:domain:undertow:")

def undertowChildren = undertowSubsystem.children()
def undertowAppSecurityDomains = new groovy.util.Node(null, 'application-security-domains', [])
undertowChildren.add(undertowAppSecurityDomains)

def appSecurityDomain = undertowAppSecurityDomains.appendNode('application-security-domain', ['name':'JBossWS','http-authentication-factory':'JBossWS'])
def basicAppSecurityDomain = undertowAppSecurityDomains.appendNode('application-security-domain', ['name':'ws-basic-domain','http-authentication-factory':'ws-basic-domain'])
def digestAppSecurityDomain = undertowAppSecurityDomains.appendNode('application-security-domain', ['name':'ws-digest-domain','http-authentication-factory':'ws-digest-domain'])
def wsDigestAppSecurityDomain = undertowAppSecurityDomains.appendNode('application-security-domain', ['name':'JBossWSDigest','http-authentication-factory':'JBossWSDigest'])
def jaasWsAppSecurityDomain = undertowAppSecurityDomains.appendNode('application-security-domain', ['name':'JAASJBossWS','http-authentication-factory':'JAASJBossWS'])

def server = null
for (element in undertowSubsystem) {
    if (element.name().getLocalPart() == 'server') {
        server = element
        break
    }
}

def curHttpsListener = server.'https-listener'[0]
if (curHttpsListener != null) {
    server.remove(curHttpsListener)
}

server.appendNode('https-listener', ['name':'jbws-test-https-listener','socket-binding':'https','security-realm':'jbws-test-https-realm'])

def tls = null
for (element in securitySubsystem) {
    if (element.name().getLocalPart() == 'tls') {
        tls = element
    }
}

tls.'key-stores'.'key-store'[0].'credential-reference'.@'clear-text' = "changeit"
tls.'key-stores'.'key-store'[0].file.@path = keystorePath
tls.'key-stores'.'key-store'[0].file[0].attributes().remove('relative-to')

tls.'key-managers'.'key-manager'[0].'credential-reference'.@'clear-text' = "changeit"
tls.'key-managers'.'key-manager'[0].@'alias-filter' = "tomcat"

/**
 *
 * Add a system property for JBWS-3628 testcase
 */
root.children().add(1, new Node(null, 'system-properties', null)) //add system-properties node after the extensions
def systemProperties = root.'system-properties'[0]
systemProperties.appendNode('property', ['name':'org.jboss.wsf.test.JBWS3628TestCase.policy','value':'WS-Addressing_policy'])

/**
 * Save the configuration to a new file
 */

def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(outputFile)
f.write(writer.toString())
