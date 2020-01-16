#### FIDO2 Server, Community Edition 
# README

## Overview
StrongKey's Certified FIDO2 Server, Community Edition is an open-source solution designed for DIY coders who want password-less FIDO2  logins for any application. Download the code and integrate it with your own web login, or study the OpenAPI documentation and contribute with your own code submissions.

![StrongKey FIDO Certificate](https://github.com/StrongKey/fido2/raw/master/docs/images/fido2certified.png)

**ATTENTION**: This distributions is NOT a FIDO2 "server library". It is a full-blown, enterprise FIDO Certified FIDO2 Server that goes above and beyond a "server library". Specifically, the StrongKey FIDO2 Enterprise Server has the following features:

- It is separate from the RP's business web application, using its own database, web services, and processing environment so it does not impact the resources of the business web application
- It is intended to be shared as a centralized service by many or all business web applications within the enterprise
- It is designed to support different parts of the enterprise through different "domains"--where domains can be separated by geography, business division, regulation, etc.
- It provides HA/DR as a standard feature through clustered servers,working with third-party load balancers, regardless of whether the web application has HA/DR capability or not
- It provides static and dynamic configuration tools to customize policies of the enterprise FIDO2 server
- It provides a separate software FIDO2 Authenticator simulator for developers to perform large-scale performance testing
- It provides monitoring tools and instrumentation to manage the FIDO2 server independent of business web applications (in development)
- It provides a dashboard for administering the FIDO2 server (in development)
- ... and more

The following links provide some background on FIDO, the FIDO Alliance, and FIDO2:

* [FIDO Alliance Home](https://fidoalliance.org)
* [What is FIDO?](https://fidoalliance.org/what-is-fido/)
* [The FIDO2 Project](https://fidoalliance.org/fido2/)

## Installation
1) Follow [the installation instructions](docs/Installation_Guide_Linux.md) to download the FIDO2 Server and get it running as a stand-alone server.
2) Follow [the clustering instructions](docs/Clustering_Guide_Linux.md) to download the FIDO2 Server and get it running as a cluster.

## Sample Applications
Sample code is provided with a brief explanation of what each sample does:

* Java Samples
  * [DEMO](https://fido2.strongkey.com): A basic Java application demonstrating FIDO2 registration and authentication
  * [Basic](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/basic/): Basic Java sample application
  * [PoC](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/poc/): Proof of concept (PoC) Java application
  * [Android](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/android/): Proof of concept Android application

## API docs
[Interactive OpenAPI documentation for FIDO2 Server](https://strongkey.github.io/fido2/)

## Contributing
If you would like to contribute to the FIDO2 Server, Community Edition project, please read [CONTRIBUTING.md](CONTRIBUTING.md), then sign and return the [Contributor License Agreement (CLA)](https://cla-assistant.io/StrongKey/fido2).

## Licensing
This project is currently licensed under the [GNU Lesser General Public License v2.1](LICENSE).

Bouncy Castle Federal Information Processing Standards (BC FIPS) is included with permission from the Legion of the Bouncy Castle, Inc. Source and other details for the module, as well as any updates, are available from the Legion's website at https://www.bouncycastle.org/fips-java.
