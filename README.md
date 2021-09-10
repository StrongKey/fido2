#### StrongKey FIDO Server (SKFS), Community Edition 
# README

***************************************************************************
**PLEASE NOTE THE LOCATION OF THE API DOCS HAS CHANGED, AND IS NOW [HERE](https://demo4.strongkey.com/getstarted/#/openapi/fido).**

***************************************************************************

## Overview
The FIDO(R) Certified StrongKey FIDO Server (SKFS), Community Edition is an open-source solution designed for DIY coders who want passwordless FIDO2 logins for any application. Download the code and integrate it with your own web login, or study the OpenAPI documentation and contribute with your own code submissions.

![StrongKey FIDO Certificate](https://github.com/StrongKey/fido2/raw/master/docs/images/fido2certified.png)

[![StrongKey Android API](https://github.com/StrongKey/fido2/blob/master/docs/images/StrongKey+Android=Protection.png)](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/sacl/mobile/android)


The following links provide some background on FIDO, the FIDO Alliance, and FIDO2:

* [FIDO Alliance Home](https://fidoalliance.org)
* [What is FIDO?](https://fidoalliance.org/what-is-fido/)
* [The FIDO2 Project](https://fidoalliance.org/fido2/)

## [Documentation](https://docs.strongkey.com/)

For the latest version:
* [Installation instructions](https://docs.strongkey.com/index.php/skfs-home/skfs-installation/skfs-installation-standalone): Download SKFS and get it running as a stand-alone server
  * [Clustering instructions](https://docs.strongkey.com/index.php/skfs-home/skfs-installation/skfs-installation-clustered): Download SKFS and get it running as a cluster
  * [Dockerized instructions](https://docs.strongkey.com/index.php/skfs-home/skfs-installation/skfs-installation-dockerized): Deploy SKFS in a container
  * [Upgrade instructions](https://docs.strongkey.com/index.php/skfs-home/skfs-installation/skfs-upgrading): Upgrade SKFS to the latest version*
* [Administration](https://docs.strongkey.com/index.php/skfs-home/skfs-administration)
  * [Operations](https://docs.strongkey.com/index.php/skfs-home/skfs-administration/skfs-operations)
  * [Security](https://docs.strongkey.com/index.php/skfs-home/skfs-administration/skfs-security)
  * [Policy](https://docs.strongkey.com/index.php/skfs-home/skfs-administration/skfs-policy)
  * [Testing](https://docs.strongkey.com/index.php/skfs-home/skfs-administration/skfs-test-v3-api)
* [Application Development](https://docs.strongkey.com/index.php/skfs-home/skfs-developers)
  * [Registration](https://docs.strongkey.com/index.php/skfs-home/skfs-developers/skfs-fido2-enabling-a-web-application/skfs-enabling-initial-registration) and [Authentication](https://docs.strongkey.com/index.php/skfs-home/skfs-developers/skfs-fido2-enabling-a-web-application/skfs-enabling-authentication) step-by-step breakdown with code examples
  * Two SKFS v3 API choices: [REST](https://docs.strongkey.com/index.php/skfs-home/skfs-developers/skfs-rest) and [SOAP](https://docs.strongkey.com/index.php/skfs-home/skfs-developers/skfs-soap)
* [Usage](https://docs.strongkey.com/index.php/skfs-home/skfs-usage): How users register and authenticate with SKFS
* [Troubleshooting](https://docs.strongkey.com/index.php/skfs-home/skfs-troubleshooting) Error messages and Known Issues
* [Release Notes](https://docs.strongkey.com/index.php/skfs-home/skfs-release-notes) Starting with KA 3.x and moving forward

## Sample Applications
Sample code is provided with a brief explanation of what each sample does:

* Java Samples
  * [DEMO](https://demo5.strongkey.com): A basic Java application demonstrating FIDO2 registration and authentication
  * [Basic](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/basic/): Basic Java sample application
  * [PoC](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/poc/): Proof of concept (PoC) Java application
  * [SSO](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/sacl/): FIDO-enabled sample applications demonstrating SSO
  * [Android](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/sacl/mobile/android): FIDO-enabled sample Android application and Android client library to perform FIDO transactions

## Sample Client
SKFS client offers examples of the various API calls using different available methods. Read the [skfsclient docs](https://github.com/StrongKey/fido2/blob/master/server/skfsclient/skfsclient.md) for commands to test FIDO2 functionality against your sandbox.

The _skfsclient_ uses a FIDO2 simulator instead of an actual authenticator to demonstrate the web services on the command line. Feel free to download the [simulator source code](https://github.com/StrongKey/fido2/tree/master/server/FIDO2Simulator) for your own use.

## API docs
[Interactive OpenAPI documentation for SKFS](https://demo4.strongkey.com/getstarted/#/openapi/fido)

## Contributing
If you would like to contribute to the SKFS, Community Edition project, please read [CONTRIBUTING.md](CONTRIBUTING.md), then sign and return the [Contributor License Agreement (CLA)](https://cla-assistant.io/StrongKey/fido2).

## Archives
Older SKFS versions can be located [here](https://github.com/StrongKey/fido2/releases).

## Licensing
This project is currently licensed under the [GNU Lesser General Public License v2.1](LICENSE).

Bouncy Castle Federal Information Processing Standards (BC FIPS) is included with permission from the Legion of the Bouncy Castle, Inc. Source and other details for the module, as well as any updates, are available from the Legion's website at https://www.bouncycastle.org/fips-java.
