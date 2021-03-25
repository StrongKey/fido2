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

sfaeco=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

failure() {
        rm -f $sfaeco/sfaecoInstall/sfaeco.war
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
cp $sfaeco/sfaeco-ear/target/sfaeco-ear-1.0.ear $sfaeco/sfaecoInstall

# Create archives
echo "-Packaging fidoserver..."
tar zcf sfaeco-v1.0-dist.tgz -C $sfaeco/sfaecoInstall .

# Do not go to the failure function
trap : 0
echo "Success!"

rm -f $sfaeco/sfaecoInstall/sfaeco-ear-1.0.ear
exit 0
