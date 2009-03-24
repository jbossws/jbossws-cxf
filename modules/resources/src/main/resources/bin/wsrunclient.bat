@echo off

@if not "%ECHO%" == ""  echo %ECHO%
@if "%OS%" == "Windows_NT"  setlocal

set DIRNAME=.\
if "%OS%" == "Windows_NT" set DIRNAME=%~dp0%
set PROGNAME=run.bat
if "%OS%" == "Windows_NT" set PROGNAME=%~nx0%

if not [%1] == [] goto start
    echo %PROGNAME% is a command line tool that invokes a JBossWS JAX-WS Web Service client.
    echo It builds the correct classpath and endorsed libs for you. Feel free to use
    echo the code for this script to make your own shell scripts. It is open source
    echo after all.
    echo.
    echo usage: %PROGNAME% [-classpath ^<additional class path^>] ^<java-main-class^> [arguments...]
    goto EOF 
:start 
set ARGS=
:loop
if [%1] == [] goto endloop
    if not %1 == -classpath goto argset 
      set WSRUNCLIENT_CLASSPATH=%2
      shift 
      shift
      goto loop
    :argset
      set ARGS=%ARGS% %1 
      shift
      goto loop
:endloop

set JAVA=%JAVA_HOME%\bin\java
if "%JBOSS_HOME%" = "" set JBOSS_HOME=%DIRNAME%\..

rem Setup the java endorsed dirs
set JBOSS_ENDORSED_DIRS=%JBOSS_HOME%\lib\endorsed

rem Setup the tool classpath
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/activation.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/javassist.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jaxb-api.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jaxb-impl.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jbossall-client.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jboss-xml-binding.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jbossws-common.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jbossws-framework.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jbossws-spi.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/FastInfoset.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/log4j.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/mail.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/stax-api.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/xmlsec.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/wsdl4j.jar"
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jaxws-api.jar"

rem stack specific libs
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/asm.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jbossws-cxf-client.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-api.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-common-utilities.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-corba.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-http.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-xml.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-object.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-bindings-soap.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-core.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-databinding-jaxb.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-frontend-jaxws.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-frontend-jaxrs.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-frontend-simple.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-javascript.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-management.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-transports-http.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-transports-http-jetty.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-transports-jms.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-transports-local.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-ws-addr.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-ws-policy.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-rt-ws-rm.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-tools-java2ws.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-tools-common.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/cxf-wstx-msv-validation.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/commons-collections.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/commons-lang.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/commons-logging.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/FastInfoset.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/geronimo-ws-metadata_2.0_spec.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jaxws-api.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jboss-ejb3x.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/jboss-javaee.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/neethi.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/spring-beans.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/spring-context.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/spring-core.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/saaj-api.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/saaj-impl.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/stax-api.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/velocity.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/XmlSchema.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/xml-resolver.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/wsdl4j.jar
set WSRUNCLIENT_CLASSPATH=%WSRUNCLIENT_CLASSPATH%;%JBOSS_HOME%/client/wstx.jar

rem Execute the JVM
"%JAVA%" %JAVA_OPTS% -Djava.endorsed.dirs="%JBOSS_ENDORSED_DIRS%" -classpath "%WSRUNCLIENT_CLASSPATH%" %ARGS%
:EOF
