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

  JBossWS CXF has a lot of dependencies, including jbossws subprojects like jbossws-parent,
  jbossws-spi, jbossws-common, jbossws-api, jbossws-common-tools. Please check if the version is still
  a SNAPSHOT and release the subproject if needed. For third-party projects like CXF, please
  make sure it is the official release version. Because we always test the jbossws-cxf project against the
  latest WildFly version, the only SNAPSHOT version allowed before release is the WildFly version.

* Contents checks

  Check the [JBWS JIRA](https://redhat.atlassian.net/browse/JBWS) to make sure all must-have features or issues are included
* Quality / testing gate
    - Make sure the CI is passed/green as expected.
    - Check if major components like Apache CXF or WSS4J have any major issues or CVEs
    - Check if CXF or the JAXB impl have any TCK failures
    - Check if other components have major CVEs that require an upgrade
* PR queue
    - Review the PR queue and check all desired contributions are included
* Branch preparation
    - Branch the codebase in preparation for the release if necessary
* Docker must be running locally (required by the `-Prelease` profile for cloud-tests)
* JDK and Maven to build the release
    - JDK 17+
    - Maven 3.9+

### Source Tagging
JBossWS relies on `maven-release-plugin` to tag and change the development version.

```bash
mvn release:prepare -Prelease -DpushChanges=false -DskipTests \
  -DpreparationGoals="clean install" \
  -DreleaseVersion=<RELEASE_VERSION> \
  -Dtag=jbossws-cxf-<RELEASE_VERSION> \
  -DdevelopmentVersion=<NEXT_SNAPSHOT>
```

Key flags:
* `-Prelease` activates the release profile, which includes all modules (cloud-tests, docbook). Without it, those modules will not have their versions updated.
* `-DpreparationGoals="clean install"` ensures artifacts are installed to the local Maven repository before the feature pack build resolves them.
* `-DpushChanges=false` lets you review commits and tag before pushing.

As CI is passed before we tag the release, we use `-DskipTests` to skip the tests.

After `release:prepare`, verify no old SNAPSHOT references remain:

```bash
grep -r "<old-SNAPSHOT>" --include="pom.xml" . | grep -v target
```

### Publish Artifacts
JBossWS-CXF artifacts are published to the JBoss Nexus staging repository. This requires
properly configured credentials and GPG signing. The deploy command must run in the foreground
for GPG passphrase entry:

```bash
git checkout jbossws-cxf-<RELEASE_VERSION>

mvn deploy -Pjboss-release -Dmaven.install.skip=true \
  -Dmaven.compiler.skip=true -DskipTests
```

After the artifacts are uploaded, validate the staging repository, then promote:

```bash
mvn nxrm3:staging-move -Dmaven.install.skip=true \
  -Dmaven.compiler.skip=true -DskipTests
```

If validation fails and you need to drop the staging repository:

```bash
mvn nxrm3:staging-delete -Dmaven.install.skip=true \
  -Dmaven.compiler.skip=true -DskipTests
```

After successful promotion, clean up the backup files created by the release plugin:

```bash
find . -name "pom.xml.releaseBackup" -delete
rm -f release.properties
```

Then push the release commits and tag to GitHub.

### WildFly Upgrade
If needed, submit PRs to [wildfly/wildfly](https://github.com/wildfly/wildfly) updating
the relevant version properties (e.g. `version.org.jboss.ws.cxf`, `version.org.apache.cxf`).

### Content update
After the artifacts are published, the release note and website content of this release should be updated too. This includes:
* Update JIRA to mark the new version as released and add the next version number
* Generate the JIRA release notes
* Create the blog entry and update the main page to announce the new release in [jbossws.github.io](https://github.com/jbossws/jbossws.github.io)
* Upload the documentation for this release and update the links
* Update the download page: add the download and release notes links for the new release

### JIRA update
Review / update the schedule, possibly re-assign / reschedule issues for the next release cycle.

