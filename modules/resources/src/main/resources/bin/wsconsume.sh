#!/bin/sh

DIRNAME=`dirname $0`
PROGNAME=`basename $0`

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

# shared jaxws libs
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jaxws-tools.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jaxws-rt.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/stax-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jaxb-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jaxb-impl.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jaxb-xjc.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/streambuffer.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/stax-ex.jar"

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
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-tools-common.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-tools-validator.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-tools-wsdlto-core.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-tools-wsdlto-databinding-jaxb.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-tools-wsdlto-frontend-jaxws.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-xjc-dv.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-xjc-ts.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/cxf-wstx-msv-validation.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/geronimo-ws-metadata_2.0_spec.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jaxws-api.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jboss-ejb3x.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/jboss-javaee.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/velocity.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/XmlSchema.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/xml-resolver.jar"
WSCONSUME_CLASSPATH="$WSCONSUME_CLASSPATH:$LIBDIR/wsdl4j.jar"

###
# Execute the JVM
###

# For Cygwin, switch paths to Windows format before running java
if $cygwin; then
    JBOSS_HOME=`cygpath --path --windows "$JBOSS_HOME"`
    JAVA_HOME=`cygpath --path --windows "$JAVA_HOME"`
    WSCONSUME_CLASSPATH=`cygpath --path --windows "$WSCONSUME_CLASSPATH"`
    JBOSS_ENDORSED_DIRS=`cygpath --path --windows "$JBOSS_ENDORSED_DIRS"`
fi

"$JAVA" $JAVA_OPTS \
   -Djava.endorsed.dirs="$JBOSS_ENDORSED_DIRS" \
   -Dlog4j.configuration=wstools-log4j.xml \
   -classpath "$WSCONSUME_CLASSPATH" \
   org.jboss.wsf.spi.tools.cmd.WSConsume "$@"
