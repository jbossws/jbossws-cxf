def root = new XmlParser().parse(project.properties['inputFile'])

/**
 * Fix logging: remove CONSOLE handler and set a specific log file
 *
 */
def logHandlers = root.profile.subsystem.'root-logger'.handlers[0]
def consoleHandler = logHandlers.find{it.@name == 'CONSOLE'}
logHandlers.remove(consoleHandler)
def file = root.profile.subsystem.'periodic-rotating-file-handler'.file[0]
file.attributes()['path'] = project.properties['serverLog']

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

def securityDomainsA = root.profile.subsystem.'security-domains'[0]
def securityDomainA = securityDomainsA.appendNode('security-domain', ['name':'JBossWS','cache-type':'default'])
def authenticationA = securityDomainA.appendNode('authentication')
def loginModuleA = authenticationA.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleA.appendNode('module-option', ['name':'unauthenticatedIdentity','value':'anonymous'])
loginModuleA.appendNode('module-option', ['name':'usersProperties','value':project.properties['usersPropFile']])
loginModuleA.appendNode('module-option', ['name':'rolesProperties','value':project.properties['rolesPropFile']])


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
 /* security realm for test org.jboss.test.ws.jaxws.samples.wsse.kerberos.KerberosTestCase  and
  * org.jboss.test.ws.jaxws.samples.wssePolicy.UsernameTestCase
  */
def securityRealms = root.management.'security-realms'[0]
def securityRealm = securityRealms.appendNode('security-realm', ['name':'jbws-test-https-realm'])
def serverIdentities = securityRealm.appendNode('server-identities')
def ssl = serverIdentities.appendNode('ssl')
ssl.appendNode('keystore', ['path':project.properties['keystorePath'],'keystore-password':'changeit','alias':'tomcat'])

def server = root.profile.subsystem.server[1]
server.appendNode('https-listener', ['name':'jbws-test-https-listener','socket-binding':'https','security-realm':'jbws-test-https-realm'])

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
 /* security domain for test org.jboss.test.ws.jaxws.samples.wsseDigest.UsernameDigestTestCase */
def securityDomains = root.profile.subsystem.'security-domains'[0]
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
loginModuleDigest.appendNode('module-option', ['name':'usersProperties','value':project.properties['testResourcesDir'] + '/jaxws/samples/wsse/username-digest/WEB-INF/jbossws-users.properties'])
loginModuleDigest.appendNode('module-option', ['name':'rolesProperties','value':project.properties['testResourcesDir'] + '/jaxws/samples/wsse/username-digest/WEB-INF/jbossws-roles.properties'])

/**
 * Save the configuration to a new file
 */
def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(project.properties['outputFile'])
f.write(writer.toString())

