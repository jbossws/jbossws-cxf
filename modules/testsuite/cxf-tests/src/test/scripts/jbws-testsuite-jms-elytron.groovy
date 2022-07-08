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
 * Modify ApplicationRealm security-realm to use custom properties files
 *
 * <security-realm name="ApplicationRealm">
 *   <authentication>
 *     <local default-user="$local" allowed-users="*" skip-group-loading="true"/>
 *     <properties path="jbws-application-users.properties" relative-to="jboss.server.config.dir"/>
 *   </authentication>
 *   <authorization>
 *     <properties path="jbws-application-roles.properties" relative-to="jboss.server.config.dir"/>
 *   </authorization>
 * </security-realm>
 **/
private getSubsystem(root, xmlnsPrefix) {
    for (item in root.profile.subsystem) {
        if (item.name().getNamespaceURI().startsWith(xmlnsPrefix)) {
            return item;
        }
    }
}

def securitySubsystem =  getSubsystem(root, "urn:wildfly:elytron:")
def securityRealms = null
for (element in securitySubsystem) {
    if (element.name().getLocalPart() == 'security-realms') {
        securityRealms = element
    }
}

def propertiesRealm =  securityRealms.find{it.@name == 'ApplicationRealm'}
propertiesRealm.'users-properties'[0].@path = "jbws-application-users.properties"
propertiesRealm.'groups-properties'[0].@path = "jbws-application-roles.properties"

/*** rls removed
 def server = root.profile.subsystem.'server'[0];
 def jmsQueue = server.appendNode('jms-queue', ['name':'testQueue', 'entries':'queue/test java:jboss/exported/jms/queue/test'])
 ***/
def activemqSubsystem = getSubsystem(root, "urn:jboss:domain:messaging-activemq:")
def server = null
for (element in activemqSubsystem) {
    if (element.name().getLocalPart() == 'server') {
        server = element
    }
}
def jmsQueue = server.appendNode('jms-queue', ['name':'testQueue', 'entries':'queue/test java:jboss/exported/jms/queue/test'])
/**
 * Save the configuration to a new file
 */
def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(outputFile)
f.write(writer.toString())

/*
 * copy the preconfigured jbws-application-roles.properties and jbws-application-users.properties
 * files into the standalone/configure directory
 */
new AntBuilder().copy( file:srcUsersProperties,
        tofile:destUsersProperties)

new AntBuilder().copy( file:srcRolesProperties,
        tofile:destRolesProperties)
