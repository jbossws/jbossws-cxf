#!/bin/sh

# Extract the directory and the program name
# takes care of symlinks
PRG="$0"
while [ -h "$PRG" ] ; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG="`dirname "$PRG"`/$link"
  fi
done
DIRNAME=`dirname "$PRG"`
PROGNAME=`basename "$PRG"`

# OS specific support (must be 'true' or 'false').
cygwin=false;
case "`uname`" in
    CYGWIN*)
        cygwin=true
        ;;
esac

# For Cygwin, ensure paths are in UNIX format before anything is touched
if $cygwin ; then
    [ -n "$JBOSS_HOME" ] &&
        JBOSS_HOME=`cygpath --unix "$JBOSS_HOME"`
    [ -n "$JAVA_HOME" ] &&
        JAVA_HOME=`cygpath --unix "$JAVA_HOME"`
fi

# Setup JBOSS_HOME
if [ "x$JBOSS_HOME" = "x" ]; then
    # get the full path (without any relative bits)
    JBOSS_HOME=`cd $DIRNAME/..; pwd`
fi
export JBOSS_HOME

# Setup the JVM
if [ "x$JAVA" = "x" ]; then
    if [ "x$JAVA_HOME" != "x" ]; then
	JAVA="$JAVA_HOME/bin/java"
    else
	JAVA="java"
    fi
fi

#JPDA options. Uncomment and modify as appropriate to enable remote debugging .
#JAVA_OPTS="-classic -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,address=8787,server=y,suspend=y $JAVA_OPTS"

# Setup JBoss sepecific properties
JAVA_OPTS="$JAVA_OPTS"

# Setup the java endorsed dirs
JBOSS_ENDORSED_DIRS="$JBOSS_HOME/lib/endorsed"

###
# Setup the LIBDIR
# This script maybe used form within the jbossws distribution
# or installed under JBOSS_HOME/bin
###

PARENT=`cd $DIRNAME/..; pwd`
if [ -d $PARENT/client ]; then
	LIBDIR=$JBOSS_HOME/client
else
	LIBDIR=$PARENT/lib
fi

###
# Setup the wsprovide classpath
# The classpath is dynamically build depending on the stack that
# is deployed. See $JBOSSWS_NATIVE above.
###

# shared libs
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$JAVA_HOME/lib/tools.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/jbossws-spi.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/jbossws-common.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/jbossws-framework.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/activation.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/getopt.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/jbossall-client.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/log4j.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/mail.jar"

# CXF XJC patched plugins
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-xjc-boolean.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-xjc-bug671.jar"

# shared jaxws libs
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/jaxb-api.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/jaxb-impl.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/jaxb-xjc.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/jsr181-api.jar"

# stack specific dependencies
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/jbossws-cxf-client.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-api.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-common-utilities.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-bindings-corba.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-bindings-xml.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-bindings-http.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-bindings-object.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-bindings-soap.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-core.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-databinding-jaxb.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-frontend-jaxws.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-frontend-jaxrs.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-frontend-simple.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-javascript.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-management.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-transports-http.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-transports-jms.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-transports-local.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-ws-addr.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-ws-policy.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-ws-rm.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-rt-ws-security.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-tools-java2ws.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/cxf-tools-common.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/commons-collections.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/commons-lang.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/commons-logging.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/FastInfoset.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/jaxws-api.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/jboss-javaee.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/neethi.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/spring-aop.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/spring-asm.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/spring-beans.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/spring-context.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/spring-core.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/spring-expression.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/stax-api.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/velocity.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/XmlSchema.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/resolver.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/wsdl4j.jar"
WSPROVIDE_CLASSPATH="$WSPROVIDE_CLASSPATH:$LIBDIR/wstx.jar"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JBOSS_HOME=`cygpath --path --windows "$JBOSS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    WSPROVIDE_CLASSPATH=`cygpath --path --windows "$WSPROVIDE_CLASSPATH"`
    JBOSS_ENDORSED_DIRS=`cygpath --path --windows "$JBOSS_ENDORSED_DIRS"`
fi

# Execute the command
"$JAVA" $JAVA_OPTS \
   -Dlog4j.configuration=log4j.xml \
   -Djava.endorsed.dirs="$JBOSS_ENDORSED_DIRS" \
   -classpath "$WSPROVIDE_CLASSPATH" \
   org.jboss.wsf.spi.tools.cmd.WSProvide "$@"
