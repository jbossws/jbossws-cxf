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

def subsystems = root.profile.subsystem
def securityDomains = null
for (item in subsystems) {
    if (item.name().getNamespaceURI().contains("urn:jboss:domain:security:")) {
       for (element in item) {
           if (element.name().getLocalPart() == 'security-domains') {
              securityDomains = element
           }
       }
       break
    }
}
def securityDomain = securityDomains.appendNode('security-domain', ['name':'JBossWS','cache-type':'default'])
def authentication = securityDomain.appendNode('authentication')
def loginModule = authentication.appendNode('login-module', ['code':'UsersRoles','flag':'required'])
loginModule.appendNode('module-option', ['name':'unauthenticatedIdentity','value':'anonymous'])
loginModule.appendNode('module-option', ['name':'usersProperties','value':usersPropFile])
loginModule.appendNode('module-option', ['name':'rolesProperties','value':rolesPropFile])

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
ssl.appendNode('keystore', ['path':keystorePath,'keystore-password':'changeit','alias':'tomcat'])

def server = root.profile.subsystem.server[0]
def curHttpsListener = server.'https-listener'[0]
if (curHttpsListener != null) server.remove(curHttpsListener)
server.appendNode('https-listener', ['name':'jbws-test-https-listener','socket-binding':'https','security-realm':'jbws-test-https-realm'])

/**
 * Save the configuration to a new file
 */

def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(outputFile)
f.write(writer.toString())
