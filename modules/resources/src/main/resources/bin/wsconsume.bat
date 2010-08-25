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
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JAVAC_JAR%
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/activation.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/getopt.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/wstx.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/jbossall-client.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/log4j.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/mail.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/jbossws-spi.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/jbossws-common.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/jbossws-framework.jar

rem CXF XJC patched plugins
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-xjc-boolean.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-xjc-bug671.jar

rem shared jaxws libs
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/stax-api.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/jaxb-api.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/jaxb-impl.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/jaxb-xjc.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/jsr181-api.jar

rem stack specific libs
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/jbossws-cxf-client.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/commons-collections.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/commons-lang.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-api.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-common-utilities.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-corba.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-http.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-object.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-soap.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-xml.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-core.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-management.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-transports-jms.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-transports-local.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-transports-http.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-ws-addr.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-ws-policy.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-ws-rm.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-ws-security.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-tools-common.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-tools-validator.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-tools-wsdlto-core.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-tools-wsdlto-databinding-jaxb.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-tools-wsdlto-frontend-jaxws.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-xjc-dv.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/cxf-xjc-ts.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/jaxws-api.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/jboss-javaee.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/velocity.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/XmlSchema.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/xml-resolver.jar
set WSCONSUME_CLASSPATH=%WSCONSUME_CLASSPATH%;%JBOSS_HOME%/client/wsdl4j.jar

rem Execute the command
"%JAVA%" %JAVA_OPTS% -Dlog4j.configuration=log4j.xml -Djava.endorsed.dirs="%JBOSS_ENDORSED_DIRS%" -classpath "%WSCONSUME_CLASSPATH%" org.jboss.wsf.spi.tools.cmd.WSConsume %*
