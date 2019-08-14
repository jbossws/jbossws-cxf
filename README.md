 # JBossWS-CXF
 
 [![Build Status](https://api.travis-ci.org/jbossws/jbossws-cxf.svg?branch=master)](https://api.travis-ci.org/jbossws/jbossws-cxf.svg?branch=master)
 
 Building and running the testsuite
------------------------------------

Building and running the testsuite requires Maven version 3.2.2 or higher.

The build follows the usual Maven flow; a wilflyXYZ profile has to be specified to tell the project which target container to use for integration tests; if no wildflyXYZ profile is specified, the integration tests are skipped.

> mvn -PwildflyXYZ integration-test

The '-Dserver.home=/foo/bar' option can be used to run the testsuite against a given local server instance; the server must not be already running, as the build will create various standalone server configurations and start multiple instances.
The '-Dexclude-udp-tests' option can be used to skip UDP tests; that might be needed when running on a network that does not allow UDP broadcast.
The '-Dexclude-ws-discovery-tests' option can be used to skip WS-Discovery tests; that might be needed when running on a network that does not have set multicast properly.
The '-Dnodeploy' option can be used to avoid upgrading the ws stack on the target server container.
The '-Dnoprepare' option can be used to avoid integration tests preparation phase, which includes tuning of the server configurations, wsconsume/wsprovide invocations, etc.
The '-Ddebug' option can be used to turn on surefire debugging of integration tests only.
The '-Djboss.bind.address=x.y.w.z' option can be used to have the started containers bound to the specified network interface address.
The '-Dipv6' option can be used to run the integration testsuite using IPv6 protocol.
The '-Delytron' option can be used to run the integration testsuite against container with Elytron security configuration.
The '-Darquillian.deploymentExportPath=target/foo' option can be used to have Arquillian write the actual test deployments to disk.
The '-DnoLogRedirect' can be used to prevent Surefire from redirecting console logs to test output files.
The '-DenableServerLoggingToConsole' can be used to enable logging of server mesages to console too, otherwise these messages are only redirected to specific log files.
The '-Dts.dist.dependency.skip' can be used to remove dependency to jbossws-cxf-dist module which makes it possible to run tests directly from testsuite module against arbitrary server given by '-Dserver.home'.

The 'fast' profile can also be used to run tests concurrently; run 'mvn -Pfast,wildflyXYZ post-integration-test' in such case to trigger test servers' shutdown and save memory at the end of each testsuite module.


 Updating WS stack
-------------------

In some cases it might be needed to build the ws stack and install it on a specified server instance without running the integration testsuite; this is achieved as follows:

> mvn -PwildflyXYZ -Dserver.home=/foo/bar package

If a server.home property is not provided, the build creates a zip archive with a vanilla WildFly server patched with the current WS stack:

> mvn -PwildflyXYZ package

the zip file path is modules/dist/target/jbossws-cxf-dist-${project.version}-test-server.zip


 Cleaning up
-------------

The project is cleaned up as follows:

> mvn -Pdist,testsuite clean


 Releasing
-----------

Releases are performed using the Maven Release Plugin; no manual modification of artifact versions in pom.xml files is hence required. The release is tagged with the following command:

> mvn -Pwildfly1100 release:prepare

where 'wildfly1100' is one of the supported target containers (preferably not the current WildFly master version).

The release tag can then be checked out, built and deployed to the nexus repository.
To clean the release plugin data (in case of errors), run:

> mvn -Pdist,testsuite release:clean

