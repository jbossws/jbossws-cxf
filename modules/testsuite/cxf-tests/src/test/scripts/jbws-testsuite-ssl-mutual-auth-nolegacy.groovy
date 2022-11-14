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
 * Add tls configuration:
 *
 *   <tls>
 *      <key-stores>
 *          <key-store name="twoWayKS" alias-filter="tomcat">
 *              <credential-reference clear-text="changeit"/>
 *              <implementation type="JKS"/>
 *              <file path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/test.keystore.jks"/>
 *          </key-store>
 *          <key-store name="twoWayTS">
 *              <credential-reference clear-text="changeit"/>
 *              <implementation type="JKS"/>
 *              <file path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/test.truststore.jks"/>
 *          </key-store>
 *      </key-stores>
 *      <key-managers>
 *          <key-manager name="twoWayKM" key-store="twoWayKS">
 *              <credential-reference clear-text="secret"/>
 *          </key-manager>
 *      </key-managers>
 *      <trust-managers>
 *          <trust-manager name="twoWayTM" key-store="twoWayTS"/>
 *      </trust-managers>
 *      <server-ssl-contexts>
 *          <server-ssl-context name="twoWaySSC" protocols="TLSv1.2" need-client-auth="true" key-manager="twoWayKM" trust-manager="twoWayTM"/>
 *      </server-ssl-contexts>
 *  </tls>
 */

def securitySubsystem =  getSubsystem(root, "urn:wildfly:elytron:")
def tls = securitySubsystem.tls[0]

def keyStores = tls.'key-stores'[0]
println "This is kyestore : " + keyStores
def keyStore = keyStores.appendNode('key-store', ['name':'twoWayKS'])
def credentialReference = keyStore.appendNode('credential-reference',['clear-text':'changeit'])
def implementation = keyStore.appendNode('implementation',['type':'JKS'])
def filePath = keyStore.appendNode('file',['path':keystorePath])

def keyStore2 = keyStores.appendNode('key-store', ['name':'twoWayTS'])
def credentialReference2 = keyStore2.appendNode('credential-reference',['clear-text':'changeit'])
def implementation2 = keyStore2.appendNode('implementation',['type':'JKS'])
def filePath2 = keyStore2.appendNode('file',['path':truststorePath])

def keyManagers = tls.'key-managers'[0]
def keyManager = keyManagers.appendNode('key-manager', ['name':'twoWayKM','key-store':'twoWayKS'])
def credentialReferenceKM = keyManager.appendNode('credential-reference',['clear-text':'changeit'])

def trustManagers = tls.appendNode('trust-managers');
def trustManager = trustManagers.appendNode('trust-manager', ['name':'twoWayTM','key-store':'twoWayTS'])

def serverSslContexts = tls.'server-ssl-contexts'[0]
def serverSslContext = serverSslContexts.appendNode('server-ssl-context', ['name':'twoWaySSC','protocols':'TLSv1.2','need-client-auth':'true', 'key-manager':'twoWayKM', 'trust-manager':'twoWayTM'])

/**
 * Configure HTTPS listener like this:
 * <https-listener name="https" socket-binding="https" ssl-context="twoWaySSC" enable-http2="true"/>
 */

def undertowSubsystem =  getSubsystem(root, "urn:jboss:domain:undertow:")
def server = null
for (element in undertowSubsystem) {
    if (element.name().getLocalPart() == 'server') {
        server = element
    }
}

def currentHttpListener = null
for (element in server) {
    if (element.name().getLocalPart() == 'https-listener') {
        currentHttpListener = element
    }
}
server.remove(currentHttpListener)

def newHttpsListener = server.appendNode('https-listener', ['name':'https','socket-binding':'https','ssl-context':'twoWaySSC','enable-http2':'true'])

/**
 * Save the configuration to a new file
 */

def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(outputFile)
f.write(writer.toString())
