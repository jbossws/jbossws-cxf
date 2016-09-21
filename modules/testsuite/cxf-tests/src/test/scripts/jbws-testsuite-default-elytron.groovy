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
  *                <security-domain name="ws-basic-domain" default-realm="ws-basic-domain" permission-mapper="login-permission-mapper" role-mapper="combined-role-mapper">
 *                   <realm name="ws-basic-domain" role-decoder="groups-to-roles"/>
 *               </security-domain>
 *           </security-domains>
 * 
 *
 */

def securityDomains = root.profile.subsystem.'security-domains'[0]
def securityDomain = securityDomains.appendNode('security-domain', ['name':'JBossWS','default-realm':'JBossWS','permission-mapper':'login-permission-mapper','role-mapper':'combined-role-mapper'])
def realm = securityDomain.appendNode('realm',['name':'JBossWS','role-decoder':'groups-to-roles'])

def basicsecurityDomain = securityDomains.appendNode('security-domain', ['name':'ws-basic-domain','default-realm':'ws-basic-domain','permission-mapper':'login-permission-mapper','role-mapper':'combined-role-mapper'])
def basicrealm = basicsecurityDomain.appendNode('realm',['name':'ws-basic-domain','role-decoder':'groups-to-roles'])


def digestDomain = securityDomains.appendNode('security-domain', ['name':'ws-digest-domain','default-realm':'ws-digest-domain','permission-mapper':'login-permission-mapper','role-mapper':'combined-role-mapper'])
def digestRefRealm = digestDomain.appendNode('realm',['name':'ws-digest-domain','role-decoder':'groups-to-roles'])



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
def propertiesRealm = securityRealms.appendNode('properties-realm', ['name':'JBossWS', 'plain-text':'true'])
def usersProperties = propertiesRealm.appendNode('users-properties',['path':project.properties['usersPropFile']])
def groupsProperties = propertiesRealm.appendNode('groups-properties',['path':project.properties['rolesPropFile']])


def basicPropertiesRealm = securityRealms.appendNode('properties-realm', ['name':'ws-basic-domain','plain-text':'true'])
def basicUsersProperties = basicPropertiesRealm.appendNode('users-properties',['path': project.properties['testResourcesDir'] + '/jaxws/cxf/httpauth/WEB-INF/ws-users.properties'])
def basicGroupsProperties = basicPropertiesRealm.appendNode('groups-properties',['path': project.properties['testResourcesDir'] + '/jaxws/cxf/httpauth/WEB-INF/ws-roles.properties'])


def digestRealm = securityRealms.appendNode('properties-realm', ['name':'ws-digest-domain'])
def digestUserProperties = digestRealm.appendNode('users-properties',['path': project.properties['testResourcesDir'] + '/jaxws/cxf/httpauth/WEB-INF/ws-digest-users.properties'])
def digestGroupsProperties = digestRealm.appendNode('groups-properties',['path': project.properties['testResourcesDir'] + '/jaxws/cxf/httpauth/WEB-INF/ws-roles.properties'])




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

def httpAuthen = root.profile.subsystem.'http'[0]
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




/**
 *           <application-security-domains>
 *               <application-security-domain name="JBossWS" http-authentication-factory="JBossWS"/>
 *               <application-security-domain name="ws-basic-domain" http-authentication-factory="JBossWS"/>
 
 *           </application-security-domains>
 */
def appSecurityDomains = root.profile.subsystem.'application-security-domains'[1]

def appSecurityDomain = appSecurityDomains.appendNode('application-security-domain', ['name':'JBossWS','http-authentication-factory':'JBossWS'])
def basicAppSecurityDomain = appSecurityDomains.appendNode('application-security-domain', ['name':'ws-basic-domain','http-authentication-factory':'ws-basic-domain'])
def digestAppSecurityDomain = appSecurityDomains.appendNode('application-security-domain', ['name':'ws-digest-domain','http-authentication-factory':'ws-digest-domain'])

/**
 * Save the configuration to a new file
 */

def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(project.properties['outputFile'])
f.write(writer.toString())
