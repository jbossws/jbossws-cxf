def root = new XmlParser().parse(project.properties['inputFile'])

/**
 * Fix logging: optionally remove CONSOLE handler and set a specific log file
 *
 */
def logHandlers = root.profile.subsystem.'root-logger'.handlers[0]
def consoleHandler = logHandlers.find{it.@name == 'CONSOLE'}
if (!project.properties['enableServerLoggingToConsole']) logHandlers.remove(consoleHandler)
def file = root.profile.subsystem.'periodic-rotating-file-handler'.file[0]
file.attributes()['path'] = project.properties['serverLog']

/**
 * Add a security-domain block like this:
 *
 * <security-domain name="JAASJBossWS" cache-type="default">
 *   <authentication>
 *     <login-module code="UsersRoles" flag="required">
 *       <module-option name="usersProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-users.properties"/>
 *       <module-option name="unauthenticatedIdentity" value="anonymous"/>
 *       <module-option name="rolesProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-roles.properties"/>
 *     </login-module>
 *   </authentication>
 * </security-domain>
 *
 */

def securityDomains = root.profile.subsystem.'security-domains'[0]
def securityDomain = securityDomains.appendNode('security-domain', ['name':'JAASJBossWS','cache-type':'default'])
def authentication = securityDomain.appendNode('authentication')
def loginModule = authentication.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModule.appendNode('module-option', ['name':'unauthenticatedIdentity','value':'anonymous'])
loginModule.appendNode('module-option', ['name':'usersProperties','value':project.properties['usersPropFile']])
loginModule.appendNode('module-option', ['name':'rolesProperties','value':project.properties['rolesPropFile']])


/**
 * Add a security-domain block like this:
 *
 * <security-domain name="JBossWS" cache-type="default">
 *   <authentication>
 *     <login-module code="UsersRoles" flag="required">
 *       <module-option name="usersProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-users.properties"/>
 *       <module-option name="unauthenticatedIdentity" value="anonymous"/>
 *       <module-option name="rolesProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-roles.properties"/>
 *     </login-module>
 *   </authentication>
 * </security-domain>
 *
 */

def jbsecurityDomain = securityDomains.appendNode('security-domain', ['name':'JBossWS','cache-type':'default'])
def jbauthentication = securityDomain.appendNode('authentication')
def jbloginModule = jbauthentication.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
jbloginModule.appendNode('module-option', ['name':'unauthenticatedIdentity','value':'anonymous'])
jbloginModule.appendNode('module-option', ['name':'usersProperties','value':project.properties['usersPropFile']])
jbloginModule.appendNode('module-option', ['name':'rolesProperties','value':project.properties['rolesPropFile']])

/**
 * Add a security-domain block like this:
 *
 * <security-domain name="JBossWS-trust-sts" cache-type="default">
 *   <authentication>
 *     <login-module code="UsersRoles" flag="required">
 *       <module-option name="usersProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-resources/jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-users.properties"/>
 *       <module-option name="unauthenticatedIdentity" value="anonymous"/>
 *       <module-option name="rolesProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-resources/jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-roles.properties"/>
 *     </login-module>
 *   </authentication>
 * </security-domain>
 *
 */

def securityDomainSts = securityDomains.appendNode('security-domain', ['name':'JBossWS-trust-sts','cache-type':'default'])
def authenticationSts = securityDomainSts.appendNode('authentication')
def loginModuleSts = authenticationSts.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleSts.appendNode('module-option', ['name':'unauthenticatedIdentity','value':'anonymous'])
loginModuleSts.appendNode('module-option', ['name':'usersProperties','value':project.properties['testResourcesDir'] + '/jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-users.properties'])
loginModuleSts.appendNode('module-option', ['name':'rolesProperties','value':project.properties['testResourcesDir'] + '/jaxws/samples/wsse/policy/trust/WEB-INF/jbossws-roles.properties'])

/**
 * Add a security-domain block like this:
 *
 * <security-domain name="ws-basic-domain" cache-type="default">
 *   <authentication>
 *     <login-module code="UsersRoles" flag="required">
 *       <module-option name="usersProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-resources/jaxws/cxf/httpauth/WEB-INF/ws-users.properties"/>
 *       <module-option name="rolesProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-resources/jaxws/cxf/httpauth/WEB-INF/ws-roles.properties"/>
 *       <module-option name="password-stacking" value="useFirstPass"/>
 *     </login-module>
 *   </authentication>
 * </security-domain>
 *
 */

def securityDomainBasicAuth = securityDomains.appendNode('security-domain', ['name':'ws-basic-domain','cache-type':'default'])
def authenticationBasicAuth = securityDomainBasicAuth.appendNode('authentication')
def loginModuleBasicAuth = authenticationBasicAuth.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleBasicAuth.appendNode('module-option', ['name':'usersProperties','value':project.properties['testResourcesDir'] + '/jaxws/cxf/httpauth/WEB-INF/ws-users.properties'])
loginModuleBasicAuth.appendNode('module-option', ['name':'rolesProperties','value':project.properties['testResourcesDir'] + '/jaxws/cxf/httpauth/WEB-INF/ws-roles.properties'])
loginModuleBasicAuth.appendNode('module-option', ['name':'password-stacking','value':'useFirstPass'])

/**
 * Add a security-domain block like this:
 *
 * <security-domain name="ws-digest-domain" cache-type="default">
 *   <authentication>
 *     <login-module code="UsersRoles" flag="required">
 *       <module-option name="hashUserPassword" value="false"/>
 *       <module-option name="usersProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-resources/jaxws/cxf/httpauth/WEB-INF/ws-users.properties"/>
 *       <module-option name="hashAlgorithm" value="MD5"/>
 *       <module-option name="hashEncoding" value="RFC2617"/>
 *       <module-option name="rolesProperties" value="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-resources/jaxws/cxf/httpauth/WEB-INF/ws-roles.properties"/>
 *       <module-option name="storeDigestCallback" value="org.jboss.security.auth.callback.RFC2617Digest"/>
 *       <module-option name="hashStorePassword" value="true"/>
 *     </login-module>
 *   </authentication>
 * </security-domain>
 *
 */

def securityDomainDigestAuth = securityDomains.appendNode('security-domain', ['name':'ws-digest-domain','cache-type':'default'])
def authenticationDigestAuth = securityDomainDigestAuth.appendNode('authentication')
def loginModuleDigestAuth = authenticationDigestAuth.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleDigestAuth.appendNode('module-option', ['name':'hashUserPassword','value':'false'])
loginModuleDigestAuth.appendNode('module-option', ['name':'usersProperties','value':project.properties['testResourcesDir'] + '/jaxws/cxf/httpauth/WEB-INF/ws-users.properties'])
loginModuleDigestAuth.appendNode('module-option', ['name':'hashAlgorithm','value':'MD5'])
loginModuleDigestAuth.appendNode('module-option', ['name':'hashEncoding','value':'RFC2617'])
loginModuleDigestAuth.appendNode('module-option', ['name':'rolesProperties','value':project.properties['testResourcesDir'] + '/jaxws/cxf/httpauth/WEB-INF/ws-roles.properties'])
loginModuleDigestAuth.appendNode('module-option', ['name':'storeDigestCallback','value':'org.jboss.security.auth.callback.RFC2617Digest'])
loginModuleDigestAuth.appendNode('module-option', ['name':'hashStorePassword','value':'true'])

/**
 * Add a security-domain block like this:
 *
 * <security-domain name="JBossWSDigest" cache-type="default">
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

def securityDomainDigest = securityDomains.appendNode('security-domain', ['name':'JBossWSDigest','cache-type':'default'])
def authenticationDigest = securityDomainDigest.appendNode('authentication')
def loginModuleDigest = authenticationDigest.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleDigest.appendNode('module-option', ['name':'hashUserPassword','value':'false'])
loginModuleDigest.appendNode('module-option', ['name':'hashCharset','value':'UTF-8'])
loginModuleDigest.appendNode('module-option', ['name':'hashAlgorithm','value':'SHA'])
loginModuleDigest.appendNode('module-option', ['name':'hashEncoding','value':'BASE64'])
loginModuleDigest.appendNode('module-option', ['name':'storeDigestCallback','value':'org.jboss.wsf.stack.cxf.security.authentication.callback.UsernameTokenCallback'])
loginModuleDigest.appendNode('module-option', ['name':'hashStorePassword','value':'true'])
loginModuleDigest.appendNode('module-option', ['name':'unauthenticatedIdentity','value':'anonymous'])
loginModuleDigest.appendNode('module-option', ['name':'usersProperties','value':project.properties['testResourcesDir'] + '/jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-users.properties'])
loginModuleDigest.appendNode('module-option', ['name':'rolesProperties','value':project.properties['testResourcesDir'] + '/jaxws/samples/wsse/policy/jaas/digest/WEB-INF/jbossws-roles.properties'])

/**
 * Add two security-domain blocks for JASPI tests as below:
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
 
def securityDomainJaspi = securityDomains.appendNode('security-domain', ['name':'jaspi'])
def authenticationJaspi = securityDomainJaspi.appendNode('authentication-jaspi')
def loginModuleStack = authenticationJaspi.appendNode('login-module-stack', ['name':'jaas-lm-stack'])
def loginModuleJaspi = loginModuleStack.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleJaspi.appendNode('module-option', ['name':'usersProperties','value':project.properties['usersPropFile']])
loginModuleJaspi.appendNode('module-option', ['name':'rolesProperties','value':project.properties['rolesPropFile']])
authenticationJaspi.appendNode('auth-module', ['code':'org.jboss.wsf.stack.cxf.jaspi.module.UsernameTokenServerAuthModule','login-module-stack-ref':'jaas-lm-stack'])

def securityDomainJaspiClient = securityDomains.appendNode('security-domain', ['name':'clientJaspi'])
def authenticationJaspiClient = securityDomainJaspiClient.appendNode('authentication-jaspi')
def loginModuleStackClient = authenticationJaspiClient.appendNode('login-module-stack', ['name':'jaas-lm-stack'])
def loginModuleJaspiClient = loginModuleStackClient.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleJaspiClient.appendNode('module-option', ['name':'usersProperties','value':project.properties['usersPropFile']])
loginModuleJaspiClient.appendNode('module-option', ['name':'rolesProperties','value':project.properties['rolesPropFile']])
authenticationJaspiClient.appendNode('auth-module', ['code':'org.jboss.wsf.stack.cxf.jaspi.client.module.SOAPClientAuthModule','login-module-stack-ref':'jaas-lm-stack'])

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

def securityRealms = root.management.'security-realms'[0]
def securityRealm = securityRealms.appendNode('security-realm', ['name':'jbws-test-https-realm'])
def serverIdentities = securityRealm.appendNode('server-identities')
def ssl = serverIdentities.appendNode('ssl')
ssl.appendNode('keystore', ['path':project.properties['keystorePath'],'keystore-password':'changeit','alias':'tomcat'])

def server = root.profile.subsystem.server[0]
def curHttpsListener = server.'https-listener'[0]
if (curHttpsListener != null) server.remove(curHttpsListener)
server.appendNode('https-listener', ['name':'jbws-test-https-listener','socket-binding':'https','security-realm':'jbws-test-https-realm'])


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
def f = new File(project.properties['outputFile'])
f.write(writer.toString())
