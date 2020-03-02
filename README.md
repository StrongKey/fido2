#### FIDO2 Server, Community Edition 
# README

## Overview
StrongKey's Certified FIDO2 Server, Community Edition is an open-source solution designed for DIY coders who want password-less FIDO2  logins for any application. Download the code and integrate it with your own web login, or study the OpenAPI documentation and contribute with your own code submissions.

![StrongKey FIDO Certificate](https://github.com/StrongKey/fido2/raw/master/docs/images/fido2certified.png)

The following links provide some background on FIDO, the FIDO Alliance, and FIDO2:

* [FIDO Alliance Home](https://fidoalliance.org)
* [What is FIDO?](https://fidoalliance.org/what-is-fido/)
* [The FIDO2 Project](https://fidoalliance.org/fido2/)

## Installation
* Follow [the installation instructions](docs/Installation_Guide_Linux.md) to download the FIDO2 Server and get it running as a stand-alone server.
* Follow [the clustering instructions](docs/Clustering_Guide_Linux.md) to download the FIDO2 Server and get it running as a cluster.

## Upgrade
Follow [the upgrade instructions](docs/Upgrade_Guide_Linux.md) to upgrade your current version of fido server to the latest.

## Sample Applications
Sample code is provided with a brief explanation of what each sample does:

* Java Samples
  * [DEMO](https://fido2.strongkey.com): A basic Java application demonstrating FIDO2 registration and authentication
  * [Basic](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/basic/): Basic Java sample application
  * [PoC](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/poc/): Proof of concept (PoC) Java application

## Sample Client
StrongKey FIDO2 Server client offers examples of the various API calls using different available methods. Read the [skfsclient docs](https://github.com/StrongKey/fido2/blob/master/server/skfsclient/skfsclient.md) for commands to test FIDO2 functionality against your sandbox.

The skfsclient uses a FIDO2 simulator instead of an actual authenticator to demonstrate the web services on the command line. Feel free to download the [simulator source code](https://github.com/StrongKey/fido2/tree/master/server/FIDO2Simulator) for your own use.

## API docs
[Interactive OpenAPI documentation for FIDO2 Server](https://strongkey.github.io/fido2/)

## Contributing
If you would like to contribute to the FIDO2 Server, Community Edition project, please read [CONTRIBUTING.md](CONTRIBUTING.md), then sign and return the [Contributor License Agreement (CLA)](https://cla-assistant.io/StrongKey/fido2).

## Licensing
This project is currently licensed under the [GNU Lesser General Public License v2.1](LICENSE).

Bouncy Castle Federal Information Processing Standards (BC FIPS) is included with permission from the Legion of the Bouncy Castle, Inc. Source and other details for the module, as well as any updates, are available from the Legion's website at https://www.bouncycastle.org/fips-java.
