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
/**
 *  <security-realms>
 *     <properties-realm name="JBossWS">
 *        <users-properties path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-users.properties"/>
 *        <groups-properties path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/jbossws-roles.properties"/>
 *     </properties-realm>
*/
def securityRealms = root.profile.subsystem.'security-realms'[0]
def propertiesRealm = securityRealms.appendNode('properties-realm', ['name':'JBossWS'])
def usersProperties = propertiesRealm.appendNode('users-properties',['path':usersPropFile, 'plain-text':'true'])
def groupsProperties = propertiesRealm.appendNode('groups-properties',['path':rolesPropFile])

 /*   <http>
 *      <http-authentication-factory name="JBossWS" http-server-mechanism-factory="global" security-domain="JBossWS">
 *         <mechanism-configuration>
 *             <mechanism mechanism-name="BASIC">
 *                 <mechanism-realm realm-name="JBossWS Realm"/>
 *             </mechanism>
 *     </mechanism-configuration>
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
def elytronSubsystem =  getSubsystem(root, "urn:wildfly:elytron:")
def tls = null
for (element in elytronSubsystem) {
    if (element.name().getLocalPart() == 'tls') {
        tls = element
    }
}
if (tls == null) {
    tls = elytronSubsystem.appendNode('tls')
}

def keyStores = null
for (element in tls) {
    if (element.name().getLocalPart() == 'key-stores') {
        keyStores = element
    }
}
if (keyStores == null) {
    keyStores = tls.appendNode('key-stores')
}

def keyStore = keyStores.appendNode('key-store', ['name':'jbwsTestHttpsRealmKS', 'alias-filter':'tomcat'])
def credentialReference = keyStore.appendNode('credential-reference', ['clear-text':'changeit'])
def implementation = keyStore.appendNode('implementation',['type':'JKS'])
def filePath = keyStore.appendNode('file',['path':keystorePath])

def keyManagers = null
for (element in tls) {
    if (element.name().getLocalPart() == 'key-managers') {
        keyManagers = element
    }
}
if (keyManagers == null) {
    keyManagers = tls.appendNode('key-managers')
}

def keyManager = keyManagers.appendNode('key-manager',
        ['name':'jbwsTestHttpsRealmKM','key-store':'jbwsTestHttpsRealmKS'])
def credentialReferenceKM = keyManager.appendNode(
        'credential-reference',['clear-text':'changeit'])

def serverSslContexts = null
for (element in tls) {
    if (element.name().getLocalPart() == 'server-ssl-contexts') {
        serverSslContexts = element
    }
}
if (serverSslContexts == null) {
    serverSslContexts = tls.appendNode('server-ssl-contexts')
}

def serverSslContext = serverSslContexts.appendNode('server-ssl-context',
        ['name':'jbwsTestHttpsRealmSSC','key-manager':'jbwsTestHttpsRealmKM'])

def undertowSubsystem = getSubsystem(root, "urn:jboss:domain:undertow:")
def server = null
for (element in undertowSubsystem) {
    if (element.name().getLocalPart() == 'server') {
        server = element
    }
}

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
