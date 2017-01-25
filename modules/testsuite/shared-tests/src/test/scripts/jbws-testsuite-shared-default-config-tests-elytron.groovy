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
 *        <subsystem xmlns="urn:wildfly:elytron:1.0">
 *           <security-domains>
 *                <security-domain name="JBossWS" default-realm="JBossWS" permission-mapper="login-permission-mapper" role-mapper="combined-role-mapper">
 *                   <realm name="JBossWS" role-decoder="groups-to-roles"/>
 *               </security-domain>
 *           </security-domains>
 * 
 *
 */
def securityDomains = root.profile.subsystem.'security-domains'[0]
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
def usersProperties = propertiesRealm.appendNode('users-properties',['path':project.properties['usersPropFile'], 'plain-text':'true'])
def groupsProperties = propertiesRealm.appendNode('groups-properties',['path':project.properties['rolesPropFile']])

 /*   <http>
 *      <http-authentication-factory name="JBossWS" http-server-mechanism-factory="global" security-domain="JBossWS">
 *         <mechanism-configuration>
 *             <mechanism mechanism-name="BASIC">
 *                 <mechanism-realm realm-name="JBossWS Realm"/>
 *             </mechanism>
 *     </mechanism-configuration>
 */
def httpAuthen = root.profile.subsystem.'http'[0]
def httpAuthenticationFactory = httpAuthen.appendNode('http-authentication-factory', ['name':'JBossWS','http-server-mechanism-factory':'global', 'security-domain':'JBossWS'])
def mechanismConfiguration = httpAuthenticationFactory.appendNode('mechanism-configuration')
def mechanism = mechanismConfiguration.appendNode('mechanism',['mechanism-name':'BASIC'])
def mechanismRealm=mechanism.appendNode('mechanism-realm',['realm-name':'JBossWS'])


//add to undertow
def undertowns = new groovy.xml.Namespace('urn:jboss:domain:undertow:4.0')
def wflyns = new groovy.xml.Namespace('urn:jboss:domain:5.0')
def undertowSubsystem = root[wflyns.profile][undertowns.subsystem][0]

//TODO: is there better create node as sibling in groovy
def undertowChildren = undertowSubsystem.children()
def undertowAppSecurityDomains = new groovy.util.Node(null, 'application-security-domains', [])
undertowChildren.add(5, undertowAppSecurityDomains)

def appSecurityDomain = undertowAppSecurityDomains.appendNode('application-security-domain', ['name':'JBossWS','http-authentication-factory':'JBossWS'])

/**
 * Save the configuration to a new file
 */

def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(project.properties['outputFile'])
f.write(writer.toString())
