#!/bin/sh
# Maven wrapper that invokes the classworlds launcher with Windows-format paths,
# working around the MSYS path issue in the stock mvn shell script.
M2="D:/App/maven/apache-maven-3.9.16-bin/apache-maven-3.9.16"
PROJ="D:/mine_product/forgex/Forgex_MOM/Forgex_Backend"
exec java -Dfile.encoding=UTF-8 \
  -classpath "$M2/boot/plexus-classworlds-2.11.0.jar" \
  "-Dclassworlds.conf=$M2/bin/m2.conf" \
  "-Dmaven.home=$M2" \
  "-Dlibrary.jansi.path=$M2/lib/jansi-native" \
  "-Dmaven.multiModuleProjectDirectory=$PROJ" \
  org.codehaus.plexus.classworlds.launcher.Launcher "$@"
