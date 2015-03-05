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
 * Add a JMS queue like this
 *
 *  <subsystem xmlns="urn:jboss:domain:messaging:2.0">
 *      <hornetq-server>
 *          <jms-destinations>
 *              <jms-queue name="testQueue">
 *                  <entry name="queue/test"/>
 *                  <entry name="java:jboss/exported/jms/queue/test"/>
 *              </jms-queue>
 *          </jms-destinations>
 *      </hornetq-server>
 *  </subsystem>
 **/
def hornetqServer = root.profile.subsystem.'hornetq-server'[0]
def jmsDestinations = hornetqServer.'jms-destinations'[0]
if (jmsDestinations == null) {
    jmsDestinations = hornetqServer.appendNode('jms-destinations');
}
def jmsQueue = jmsDestinations.appendNode('jms-queue', ['name':'testQueue'])
jmsQueue.appendNode('entry', ['name':'queue/test'])
jmsQueue.appendNode('entry', ['name':'java:jboss/exported/jms/queue/test'])

/**
 * Save the configuration to a new file
 */
def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(project.properties['outputFile'])
f.write(writer.toString())

/*
 * copy the preconfigured application-roles.properties and application-users.properties
 * files into the standalone/configure directory
 */
def srcUsersProperties = project.properties['srcUsersProperties']
def destUsersProperties = project.properties['destUsersProperties']
new AntBuilder().copy( file:srcUsersProperties,
    tofile:destUsersProperties)

def srcRolesProperties = project.properties['srcRolesProperties']
def destRolesProperties = project.properties['destRolesProperties']
new AntBuilder().copy( file:srcRolesProperties,
    tofile:destRolesProperties)
