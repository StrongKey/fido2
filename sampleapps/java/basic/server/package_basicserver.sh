#!/bin/bash
#
###################################################################################
# Copyright StrongAuth, Inc. All Rights Reserved.
#
# Use of this source code is governed by the Gnu Lesser General Public License 2.3.
# The license can be found at https://github.com/StrongKey/fido2/LICENSE
###################################################################################

basicserver=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

failure() {
        rm -f $basicserver/basicserverInstall/basicserver.war 
        echo "There was a problem creating the SAMPLEAPPLICATION distribution. Aborting." >&2
        exit 1
}

# If we exit prematurely, goto failure function
trap 'failure' 0

# If any command unexpectantly fails, exit prematurely
set -e

echo "Creating sample application..."
mvn clean install -q 

# Copy the necessary jars, libs, wars, ears into dist
echo "-Copying files..."
cp $basicserver/target/basicserver.war $basicserver/basicserverInstall

# Create archives
echo "-Packaging fidoserver..."
tar zcf basicserver-v0.9-dist.tgz -C $basicserver/basicserverInstall .

# Do not go to the failure function
trap : 0
echo "Success!"

rm -f $basicserver/basicserverInstall/basicserver.war
exit 0
