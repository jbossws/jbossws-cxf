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
# Setup the wsconsume classpath
###

# shared libs
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$JAVA_HOME/lib/tools.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/activation.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/getopt.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/wstx.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jbossall-client.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/log4j.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/mail.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jbossws-spi.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jbossws-common.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jbossws-framework.jar"

# CXF XJC patched plugins
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-xjc-boolean.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-xjc-bug671.jar"

# shared jaxws libs
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/stax-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jaxb-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jaxb-impl.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jaxb-xjc.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jsr181-api.jar"

# Stack specific dependencies
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jbossws-cxf-client.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/commons-collections.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/commons-lang.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-common-utilities.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-bindings-corba.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-bindings-http.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-bindings-object.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-bindings-soap.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-bindings-xml.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-core.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-management.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-transports-jms.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-transports-local.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-transports-http.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-ws-addr.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-ws-policy.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-ws-rm.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-rt-ws-security.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-tools-common.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-tools-validator.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-tools-wsdlto-core.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-tools-wsdlto-databinding-jaxb.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-tools-wsdlto-frontend-jaxws.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-xjc-dv.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-xjc-ts.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jaxws-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jboss-javaee.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/velocity.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/XmlSchema.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/resolver.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/wsdl4j.jar"

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JBOSS_HOME=`cygpath --path --windows "$JBOSS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    WSCONSUME_CLASSPATH=`cygpath --path --windows "$WSCONSUME_CLASSPATH"`
    JBOSS_ENDORSED_DIRS=`cygpath --path --windows "$JBOSS_ENDORSED_DIRS"`
fi

# Execute the command
"$JAVA" $JAVA_OPTS \
   -Dlog4j.configuration=log4j.xml \
   -Djava.endorsed.dirs="$JBOSS_ENDORSED_DIRS" \
   -classpath "$WSCONSUME_CLASSPATH" \
   org.jboss.wsf.spi.tools.cmd.WSConsume "$@"
