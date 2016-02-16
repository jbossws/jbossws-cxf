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
def securityDomains = root.profile.subsystem.'security-domains'[0]
def securityDomain = securityDomains.appendNode('security-domain', ['name':'JBossWS','cache-type':'default'])
def authentication = securityDomain.appendNode('authentication')
def loginModule = authentication.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModule.appendNode('module-option', ['name':'unauthenticatedIdentity','value':'anonymous'])
loginModule.appendNode('module-option', ['name':'usersProperties','value':project.properties['usersPropFile']])
loginModule.appendNode('module-option', ['name':'rolesProperties','value':project.properties['rolesPropFile']])

/**
 * Settings for modules/testsuite/shared-tests/src/test/java/org/jboss/test/ws/jaxws/jbws2937/JBWS2937TestCase.java
 * Add a security-domain block like this:
 *
 * <security-domain name="handlerauth-security-domain" cache-type="default">
 *   <authentication>
 *     <login-module code="UsersRoles" flag="required">
 *       <module-option name="usersProperties" value="/jaxws/handlerauth/jbossws-users.properties"/>
 *       <module-option name="rolesProperties" value="/jaxws/handlerauth/jbossws-roles.properties"/>
 *     </login-module>
 *   </authentication>
 * </security-domain>
 */
def securityDomainBasicAuth = securityDomains.appendNode('security-domain', ['name':'handlerauth-security-domain','cache-type':'default'])
def authenticationBasicAuth = securityDomainBasicAuth.appendNode('authentication')
def loginModuleBasicAuth = authenticationBasicAuth.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModuleBasicAuth.appendNode('module-option', ['name':'usersProperties','value':project.properties['testResourcesDir'] + '/jaxws/handlerauth/jbossws-users.properties'])
loginModuleBasicAuth.appendNode('module-option', ['name':'rolesProperties','value':project.properties['testResourcesDir'] + '/jaxws/handlerauth/jbossws-roles.properties'])

/**
 * Settings for modules/testsuite/shared-tests/src/test/java/org/jboss/test/ws/jaxws/samples/securityDomain/PermitAllTestCase.java
 * Add a security-domain block like this:
 *
 * <security-domain name="JBossWSSecurityDomainPermitAllTest" cache-type="default">
 *   <authentication>
 *     <login-module code="UsersRoles" flag="required">
 *       <module-option name="usersProperties" value="/jaxws/samples/securityDomain/jbossws-users.properties"/>
 *       <module-option name="rolesProperties" value="/jaxws/samples/securityDomain/jbossws-roles.properties"/>
 *     </login-module>
 *   </authentication>
 * </security-domain>
 */
def aSecurityDomainBasicAuth = securityDomains.appendNode('security-domain', ['name':'JBossWSSecurityDomainPermitAllTest','cache-type':'default'])
def aAuthenticationBasicAuth = aSecurityDomainBasicAuth.appendNode('authentication')
def aLoginModuleBasicAuth = aAuthenticationBasicAuth.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
aLoginModuleBasicAuth.appendNode('module-option', ['name':'usersProperties','value':project.properties['testResourcesDir'] + '/jaxws/samples/securityDomain/jbossws-users.properties'])
aLoginModuleBasicAuth.appendNode('module-option', ['name':'rolesProperties','value':project.properties['testResourcesDir'] + '/jaxws/samples/securityDomain/jbossws-roles.properties'])

/**
 * Settings for modules/testsuite/shared-tests/src/test/java/org/jboss/test/ws/jaxws/samples/securityDomain/SecurityDomainTestCase.java
 * Add a security-domain block like this:
 *
 * <security-domain name="JBossWSSecurityDomainTest" cache-type="default">
 *   <authentication>
 *     <login-module code="UsersRoles" flag="required">
 *       <module-option name="usersProperties" value="/jaxws/samples/securityDomain/jbossws-users.properties"/>
 *       <module-option name="rolesProperties" value="/jaxws/samples/securityDomain/jbossws-roles.properties"/>
 *     </login-module>
 *   </authentication>
 * </security-domain>
 */
def bSecurityDomainBasicAuth = securityDomains.appendNode('security-domain', ['name':'JBossWSSecurityDomainTest','cache-type':'default'])
def bAuthenticationBasicAuth = bSecurityDomainBasicAuth.appendNode('authentication')
def bLoginModuleBasicAuth = bAuthenticationBasicAuth.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
bLoginModuleBasicAuth.appendNode('module-option', ['name':'usersProperties','value':project.properties['testResourcesDir'] + '/jaxws/samples/securityDomain/jbossws-users.properties'])
bLoginModuleBasicAuth.appendNode('module-option', ['name':'rolesProperties','value':project.properties['testResourcesDir'] + '/jaxws/samples/securityDomain/jbossws-roles.properties'])

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
server.appendNode('https-listener', ['name':'jbws-test-https-listener','socket-binding':'https','security-realm':'jbws-test-https-realm'])

/**
 * Save the configuration to a new file
 */

def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(project.properties['outputFile'])
f.write(writer.toString())
