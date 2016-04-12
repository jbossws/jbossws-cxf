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
 * Add a https connector like this:
 *
 *   <security-realm name="jbws-test-https-realm">
 *       <server-identities>
 *           <ssl>
 *               <keystore path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/test.keystore" keystore-password="changeit" alias="tomcat"/>
 *           </ssl>
 *       </server-identities>
 *       <authentication>
 *           <truststore path="/mnt/ssd/jbossws/stack/cxf/trunk/modules/testsuite/cxf-tests/target/test-classes/test.truststore" keystore-password="changeit"/>
 *       </authentication>
 *   </security-realm>
 *
 *  ...
 *
 *  <https-listener name="jbws-test-https-listener" socket-binding="https" security-realm="jbws-test-https-realm" verify-client="REQUESTED"/>
 */

def securityRealms = root.management.'security-realms'[0]
def securityRealm = securityRealms.appendNode('security-realm', ['name':'jbws-test-https-realm'])
def serverIdentities = securityRealm.appendNode('server-identities')
def ssl = serverIdentities.appendNode('ssl')
ssl.appendNode('keystore', ['path':project.properties['keystorePath'],'keystore-password':'changeit','alias':'tomcat'])
def authentication = securityRealm.appendNode('authentication')
authentication.appendNode('truststore', ['path':project.properties['truststorePath'],'keystore-password':'changeit'])

def server = root.profile.subsystem.server[0]
server.appendNode('https-listener', ['name':'jbws-test-https-listener','socket-binding':'https','security-realm':'jbws-test-https-realm','verify-client':'REQUESTED'])


/**
 * Save the configuration to a new file
 */

def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(project.properties['outputFile'])
f.write(writer.toString())
