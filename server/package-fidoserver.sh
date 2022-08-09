#!/bin/bash
#
###################################################################################
# /**
# * Copyright StrongAuth, Inc. All Rights Reserved.
# *
# * Use of this source code is governed by the GNU Lesser General Public License v2.1
# * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
# */
###################################################################################

JAVA_COMPILE_VERSION=11         # Values: 8,11,17

fidoserver=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
version=$(sed -nr 's|^version=(.*)|\1|p' $fidoserver/common/src/main/resources/resources/appliance/appliance-version.properties)
resources=$fidoserver/common/src/main/resources/resources/appliance
messages=$(sed -n '31,$p' $resources/appliance-messages.properties)

skceresources=$fidoserver/common/src/main/resources/resources/skce
skcemessages=$(sed -n '31,$p' $skceresources/skce-messages.properties)

cryptoresources=$fidoserver/crypto/src/main/resources/resources
cryptomessages=$(sed -n '31,$p' $cryptoresources/crypto-messages.properties)

failure() {
        rm -f $fidoserver/fidoserverInstall/fidoserver.ear
        echo "There was a problem creating the FIDOSERVER distribution. Aborting." >&2
        exit 1
}

check_java() {
        if [ "$1" = "8" ]; then
                if [ -d /usr/lib/jvm/java-1.8.0-openjdk ]; then
                        JAVA_HOME=/usr/lib/jvm/java-1.8.0-openjdk
                else
                        echo "OpenJDK 8 not found. Please install it to compile."
                        echo "Run:"
                        echo "sudo yum install -y java-1.8.0-openjdk-devel"
                        exit 1
                fi
        elif [ "$1" = "11" ]; then
                if [ -d /usr/lib/jvm/java-11-openjdk ]; then
                        JAVA_HOME=/usr/lib/jvm/java-11-openjdk
                else
                        echo "OpenJDK 11 not found. Please install it to compile."
                        echo "Run:"
                        echo "sudo yum install -y java-11-openjdk-devel"
                        exit 1
                fi
        elif [ "$1" = "17" ]; then
                if [ -d /usr/lib/jvm/java-17-openjdk ]; then
                        JAVA_HOME=/usr/lib/jvm/java-17-openjdk
                else
                        echo "OpenJDK 17 not found. Please install it to compile."
                        echo "Run:"
                        echo "sudo yum install -y java-latest-openjdk-devel"
                        exit 1
                fi
        fi
}

# If we exit prematurely, goto failure function
trap 'failure' 0

# If any command unexpectantly fails, exit prematurely
set -e

echo "Creating fidoserver..."

# Create dist
# This cd is important for mvn to work
cd $fidoserver
check_java $JAVA_COMPILE_VERSION
echo "Creating fidoserver build using JDK: $JAVA_HOME"
mvn -q install:install-file -Dfile=$fidoserver/lib/bc-fips-1.0.2.1.jar -DgroupId=org.bouncycastle -DartifactId=bc-fips -Dversion=1.0.2.1 -Dpackaging=jar
mvn -q install:install-file -Dfile=$fidoserver/lib/bcpkix-fips-1.0.0.jar -DgroupId=org.bouncycastle -DartifactId=bcpkix-fips -Dversion=1.0.0 -Dpackaging=jar
echo "-Clean and building source..."
mvn clean install -q

# Copy the necessary jars, libs, wars, ears into dist
echo "-Copying files..."
cp $fidoserver/fidoserverEAR/target/fidoserver.ear $fidoserver/fidoserverInstall
mkdir $fidoserver/fidoserverInstall/keymanager
cp -R $fidoserver/keymanager/target/dist/* $fidoserver/fidoserverInstall/keymanager

mkdir $fidoserver/fidoserverInstall/skfsclient
cp -R $fidoserver/skfsclient/target/dist/* $fidoserver/fidoserverInstall/skfsclient

# Create archives
echo "-Packaging fidoserver..."
tar zcf fido2server-v${version}-dist.tgz -C $fidoserver/fidoserverInstall .

# Do not go to the failure function
trap : 0
echo "Success!"

rm -f $fidoserver/fidoserverInstall/fidoserver.ear
rm -rf $fidoserver/fidoserverInstall/keymanager
rm -rf $fidoserver/fidoserverInstall/skfsclient
exit 0
