def root = new XmlParser().parse(project.properties['inputFile'])

/**
 * Fix logging:
 * - remove CONSOLE handler and set a specific log file
 * - set ERROR level for root-logger
 * - add specfic categories with level set to ERROR for cxf and jboss
 *
 */
def logHandlers = root.profile.subsystem.'root-logger'.handlers[0]
def consoleHandler = logHandlers.find{it.@name == 'CONSOLE'}
logHandlers.remove(consoleHandler)
def file = root.profile.subsystem.'periodic-rotating-file-handler'.file[0]
file.attributes()['path'] = project.properties['serverLog']

def rootLogger = root.profile.subsystem.'root-logger'[0]
rootLogger.level.@name = 'ERROR'
def logSubsystem = rootLogger.parent()
def jbossCategory = logSubsystem.appendNode('logger', ['category':'org.jboss'])
jbossCategory.appendNode('level', ['name':'ERROR'])
def apacheCategory = logSubsystem.appendNode('logger', ['category':'org.apache'])
apacheCategory.appendNode('level', ['name':'ERROR'])

/**
 * Save the configuration to a new file
 */

def writer = new StringWriter()
writer.println('<?xml version="1.0" encoding="UTF-8"?>')
new XmlNodePrinter(new PrintWriter(writer)).print(root)
def f = new File(project.properties['outputFile'])
f.write(writer.toString())
