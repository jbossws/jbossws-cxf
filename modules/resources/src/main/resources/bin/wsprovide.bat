@echo off

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%
set PROGNAME=run.bat
if "%OS%" == "Windows_NT" set PROGNAME=%~nx0%

if "x%JAVA_HOME%" == "x" (
  set  JAVA=java
  echo JAVA_HOME is not set. Unexpected results may occur.
  echo Set JAVA_HOME to the directory of your local JDK to avoid this message.
) else (
  set "JAVA=%JAVA_HOME%\bin\java"
  if exist "%JAVA_HOME%\lib\tools.jar" (
    set "JAVAC_JAR=%JAVA_HOME%\lib\tools.jar"
  )
)
set JBOSS_HOME=%DIRNAME%\..
set JBOSS_ENDORSED_DIRS=%JBOSS_HOME%\lib\endorsed

rem shared libs
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JAVAC_JAR%
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossws-spi.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossws-common.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossws-framework.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/activation.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/getopt.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossall-client.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/log4j.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/mail.jar

rem CXF XJC patched plugins
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-xjc-bug671.jar

rem shared jaxws libs
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jaxb-api.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jaxb-impl.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jaxb-xjc.jar

rem stack specific libs
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jbossws-cxf-client.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-api.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-common-utilities.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-corba.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-http.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-xml.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-object.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-soap.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-core.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-databinding-jaxb.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-frontend-jaxws.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-frontend-jaxrs.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-frontend-simple.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-javascript.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-management.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-transports-http.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-transports-http-jetty.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-transports-jms.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-transports-local.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-ws-addr.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-ws-policy.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-ws-rm.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-ws-security.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-tools-java2ws.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/cxf-tools-common.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/commons-collections.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/commons-lang.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/commons-logging.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/FastInfoset.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/geronimo-ws-metadata_2.0_spec.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jaxws-api.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/jboss-javaee.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/neethi.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/spring-beans.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/spring-context.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/spring-core.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/saaj-api.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/saaj-impl.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/stax-api.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/velocity.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/XmlSchema.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/xml-resolver.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/wsdl4j.jar
set WSPROVIDE_CLASSPATH=%WSPROVIDE_CLASSPATH%;%JBOSS_HOME%/client/wstx.jar

rem Execute the command
"%JAVA%" %JAVA_OPTS% -Djava.endorsed.dirs="%JBOSS_ENDORSED_DIRS%" -classpath "%WSPROVIDE_CLASSPATH%" org.jboss.wsf.spi.tools.cmd.WSProvide %*
