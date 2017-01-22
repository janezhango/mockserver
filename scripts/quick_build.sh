#!/usr/bin/env bash

# java 1.6 build
export MAVEN_OPTS="$MAVEN_OPTS -XX:MaxPermSize=1024m -Xmx2048m"
export JAVA_OPTS="$JAVA_OPTS -XX:MaxPermSize=1024m -Xmx2048m"
# -agentpath:/Applications/jprofiler8/bin/macos/libjprofilerti.jnilib=port=25000
export JAVA_HOME=`/usr/libexec/java_home -v 1.6`
echo
echo "-------------------------"
echo "------- JAVA 1.6  -------"
echo "-------------------------"
echo
/usr/local/Cellar/maven32/3.2.5/bin/mvn clean install $1 -Dmaven-invoker-parallel-threads=4 -Djava.security.egd=file:/dev/./urandom