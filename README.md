 # JBossWS-CXF
 
 [![Build Status](https://github.com/jbossws/jbossws-cxf/actions/workflows/maven.yml/badge.svg)](https://github.com/jbossws/jbossws-cxf/actions/workflows/maven.yml/badge.svg)
 
 Building and running the testsuite
------------------------------------

Building and running the testsuite requires Maven version 3.2.2 or higher.

The build follows the usual Maven build, and all tests run against the default WildFly version.
```
mvn clean install
```

* The `-Dserver.home=/foo/bar` option can be used to run the testsuite against a given local server instance; the server must not be already running, as the build will create various standalone server configurations and start multiple instances.
* The `-Dexclude-udp-tests` option can be used to skip UDP tests; that might be needed when running on a network that does not allow UDP broadcast.
* The `-Dexclude-ws-discovery-tests` option can be used to skip WS-Discovery tests; that might be needed when running on a network that does not have set multicast properly.
* The `-Dnodeploy` option can be used to avoid upgrading the ws stack on the target server container.
* The `-Dnoprepare` option can be used to avoid integration tests preparation phase, which includes tuning of the server configurations, wsconsume/wsprovide invocations, etc.
* The `-Ddebug` option can be used to turn on surefire debugging of integration tests only.
* The `-Djboss.bind.address=x.y.w.z` option can be used to have the started containers bound to the specified network interface address.
* The `-Dipv6` option can be used to run the integration testsuite using IPv6 protocol.
* The `-Delytron` option can be used to run the integration testsuite against container with Elytron security configuration.
* The `-Darquillian.deploymentExportPath=target/foo` option can be used to have Arquillian write the actual test deployments to disk.
* The `-DnoLogRedirect` can be used to prevent Surefire from redirecting console logs to test output files.
* The `-DenableServerLoggingToConsole` can be used to enable logging of server messages to console too, otherwise these messages are only redirected to specific log files.
* The `-Dts.dist.dependency.skip` can be used to remove dependency to jbossws-cxf-dist module which makes it possible to run tests directly from testsuite module against arbitrary server given by `-Dserver.home`.

The `fast` profile can also be used to run tests concurrently; run following command in such case to trigger test servers' shutdown and save memory at the end of each testsuite module:
```
mvn -Pfast post-integration-test
```

 Updating WS stack
-------------------

In some cases it might be needed to build the ws stack and install it on a specified server instance without running the integration testsuite; this is achieved as follows:
```
mvn -Dserver.home=/foo/bar package
```
If a `server.home` property is not provided, the build creates a zip archive with a vanilla WildFly server patched with the current WS stack:

```
mvn package
```
the zip file path is modules/dist/target/jbossws-cxf-dist-${project.version}-test-server.zip


 Cleaning up
-------------

The project is cleaned up as follows:
```
mvn -Pdist,testsuite clean
```

 Releasing
-----------

### Prerequisites

* Check Resources availability

  JBossWS CXF has a lot of dependencies, this includes jbossws subprojects like jbossws-parent,
  jbossws-spi, jbossws-common, jbossws-api, jbossws-common-tools. Please check if the version is still
  the SNAPSHOT and release the subproject if it is needed. For the third party projects like CXF, please
  make sure it is the official release version. Because we always test the jbossws-cxf project against the
  latest WildFly version, the only SNAPSHOT version is allowed before release is the WildFly version.

* Contents checks

  Check the [JBWS JIRA](https://issues.redhat.com/projects/JBWS/) to make sure all the must-have features or issues are included
* Quality / testing gate
    - Make sure the CI is passed/green as expected.
    - Check if the major component like Apache CXF or WSS4j has some major issue or CVEs
    - Check if the CXF, jaxb impl has some TCK failure.
    - Check if other components have some major CVE and it needs an upgrade
* PR queue
    - Review the PR queue and check all desired contributions are included
* Branch preparation
    - Branch the codebase in preparation for the release if necessary
* JDK and Maven to build the release
    - Check the JDK version to run the build is the latest 11
    - Maven is the latest version

### Source Tagging
JBossWS relies on `maven-release-plugin` to tag and change the development version.

``` mvn release:prepare -Prelease  -DignoreSnapshots=true -DskipTests=true```

This is interactive command, and make sure you input the correct release version number and next
development version before the next step.
The `-DignoreSnapshots=true` is only for WildFly snapshot version, please double-check if there is other
SNAPSHOT dependency before run this command.
As CI is passed before we tag the release, we use `-DskipTests=true` property to skip the tests, but it doesn't matter to tag
release without this property.

### Publish Artifacts
JBossWS-CXF projects publish the artifacts to jboss nexus, and this requires properly configuring the credentials to upload 
artifacts to the jboss nexus. Uploading artifacts with the following commands:

```git checkout new-tag-version```

```mvn deploy```

After the artifacts are all uploaded, go to jboss nexus to publish these artifacts.

### Content update
After the artifacts are published, the release note and website content of this release should be updated too. This includes:
* Update JIRA to mark the new version is released and add the next version number
* Generate the JIRA release note
* Created the blog entry and main page to announce the new release in [jbossws.githut.io](https://github.com/jbossws/jbossws.github.io)
* Upload the documentation for this release and update the links.
* Update the download page: adding the download and release notes link for the new release.


### JIRA update
Review / update the schedule, possibly re-assign / reschedule issues for the next release cycle.

