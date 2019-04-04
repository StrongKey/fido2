# Basic Java Sample application
This project is a basic Relying Party (RP) web application written in Java to work with StrongKey's [FIDO2 Server, Community Edition](https://github.com/StrongKey/fido2/server). This project also includes sample javascript files prividing a basic user interface to test FIDO2 registration and authentication.

The goals of this project are to demonstrate how to call StrongKey's FIDO2 Server APIs and how to properly manage users' FIDO2 keys. It is meant to serve as a reference implementation of a project that leverages StrongKey's FIDO2 Server to enable FIDO2 authentication. **If you are an application developer looking to FIDO2-enable an application, this code uses examples of the FIDO2 API calls.**

The cybersecurity term, "Relying Party," originating from the jurisprudential term, was intended to represent legal entities that have a standing within a court of law where disputes related to digital signatures and non-repudiation could be argued; *a web application has no legal standing in court, but its owner does*. For additional information on WebAuthn Relying Parties, visit the technical specification:

- [Definition of WebAuthn Relying Party](https://www.w3.org/TR/webauthn/#webauthn-relying-party)
- [Complete WebAuthn specification](https://www.w3.org/TR/webauthn)
- [A useful diagram of WebAuthn functional flow](https://www.w3.org/TR/webauthn/#api)

For more information on the originating jargon and related terms, visit the Internet Engineering Task Force (IETF) Request for Comments (RFC):

- The definition of Relying Party is in the [second paragraph of 1.1. Background](https://tools.ietf.org/html/rfc3647#section-1.1)

Follow the instructions below to install this sample.

## Prerequisites

- This Relying Party web application example must have a means of connecting with a StrongKey FIDO2 Server. You can install a FIDO2 Server either on the same machine as your RP web application or a different one.
- You must have a Java web application server. These instructions assume you are using Payara (GlassFish).
- The instructions assume the default ports for all the applications installed; Payara runs HTTPS on port 8181 by default, so make sure all firewall rules allow that port to be accessible.
- **The sample commands below assume you are installing this RP web application on the same machine where StrongKey FIDO2 Server has been installed.** If you are installing on a separate machine, you may have to adjust the commands accordingly.

## Installation Instructions

1. Switch to (or login as) the _strongkey_ user. The default password for the _strongkey_ user is _ShaZam123_.

    ```sh
    su - strongkey
    ```

2. Create the following directories to configure the WebAuthn servlet home folder.

    ```sh
    mkdir -p /usr/local/strongkey/webauthntutorial/etc
    ```

3. Create a configuration file for the Relying Party web application to configure a FIDO2 Server.

    ```sh
    echo "webauthntutorial.cfg.property.apiuri=https://$(hostname):8181/api" > /usr/local/strongkey/webauthntutorial/etc/webauthntutorial.properties
    ```

4. Download the Relying Party .war file [basicserver.war](https://github.com/StrongKey/fido2/blob/master/sampleapps/java/basic/basicserver.war).

    ```sh
    wget https://github.com/StrongKey/fido2/blob/master/sampleapps/java/basic/basicserver.war
    ```

5. Add the .war file to Payara.

    ```sh
    payara41/glassfish/bin/asadmin deploy basicserver.war
    ```

6. Test that the servlet is running by executing the following Curl command and confirming that you get the API _Web Application Definition Language (WADL)_ file back in response.

    ```sh
    curl -k https://localhost:8181/basicserver/application.wadl
    ```


## Removal

To uninstall the RP sample web application, follow the uninstall instructions in the [FIDO2 Server, Community Edition Installation Guide](https://github.com/StrongKey/FIDO-Server/blob/master/docs/Installation_Guide_Linux.md#removal). Removing the StrongKey FIDO Server also removes the sample RP web application and sample WebAuthn client.

## Contributing to the Sample Relying Party Web Application 

If you would like to contribute to the sample Relying Party web application project, please read [CONTRIBUTING.md](https://github.com/StrongKey/relying-party-java/blob/master/CONTRIBUTING.md), then sign and return the [Contributor License Agreement (CLA)](https://cla-assistant.io/StrongKey/FIDO-Server).

## Licensing
This project is currently licensed under the [GNU Lesser General Public License v2.1](https://github.com/StrongKey/relying-party-java/blob/master/LICENSE).
