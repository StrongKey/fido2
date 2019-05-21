# POC Java Sample application
This project is a Relying Party (RP) web application written in Java to work with StrongKey's [FIDO2 Server, Community Edition](https://github.com/StrongKey/fido2).

The goals of this project are to demonstrate what a production level FIDO2 authentication deployment might looks like and to demonstrate StrongKey's APIs for managing FIDO2 keys. It is meant to serve as a reference implementation of a project that leverages StrongKey's FIDO2 Server to enable FIDO2 authentication. **If you are an application developer looking to FIDO2-enable an application, this code uses examples of the FIDO2 API calls.**

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

## Installation Instructions

1. If installing the sample application **on the same server** as the StrongKey FIDO Server, skip to step 2. Otherwise, StrongKey's software stack must be installed. The easiest way to do this is to follow the [FIDO server installation instructions](../../../docs/Installation_Guide_Linux.md) steps 1-5. Next, edit the install-skfs.sh script in a text editor. On the line "INSTALL_FIDO=Y" change the value of "Y" to "N". Run the script install-skfs.sh.

2. Create the following directories to configure the WebAuthn servlet home folder.

    ```sh
    sudo mkdir -p /usr/local/strongkey/webauthntutorial/etc
    ```

3. Create a configuration file for the Relying Party web application.

    ```sh
    sudo vi /usr/local/strongkey/webauthntutorial/etc/webauthntutorial.properties
    ```
4. Fill in the appropriate values (listed in []) to configure the sample application with a StrongKey FIDO server and an email server.

   ```
   webauthntutorial.cfg.property.apiuri=https://**[hostname of FIDO Server]**:8181/api
   webauthntutorial.cfg.property.mailhost.type=**[SendMail or SSL or StartTLS]**
   webauthntutorial.cfg.property.mailhost=**[localhost or hostname of mailhost]**
   webauthntutorial.cfg.property.mail.smtp.port=**[25 (SendMail) or mail server's port]**
   webauthntutorial.cfg.property.smtp.from=**[local-part of email address]**
   webauthntutorial.cfg.property.smtp.fromName=**[Human readable name associated with email]**
   webauthntutorial.cfg.property.smtp.auth.user=**[Username used to login to mail server]**
   webauthntutorial.cfg.property.smtp.auth.password=**[Password used to login to mail server]**
   webauthntutorial.cfg.property.email.subject=Verify your email address
   webauthntutorial.cfg.property.email.type=HTML
   ```
   Save and exit

5. Download the Relying Party web application distribution [pocserver-v0.9-dist.tgz](./server/pocserver-v0.9-dist.tgz).

    ```sh
    wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/poc/server/pocserver-v0.9-dist.tgz
    ```

6. Extract the downloaded file to the current directory:

    ```sh
    tar xvzf pocserver-v0.9-dist.tgz
    ```

7. Execute the _install-pocserver.sh_ script as follows:

    ```sh
    sudo ./install-pocserver.sh
    ```

8. Test that the servlet is running by executing the following Curl command and confirming that you get the API _Web Application Definition Language (WADL)_ file back in response.

    ```sh
    curl -k https://localhost:8181/pocserver/fido2/application.wadl
    ```


## Removal

To uninstall the RP sample web application, follow the uninstall instructions in the [FIDO2 Server, Community Edition Installation Guide](https://github.com/StrongKey/fido2/blob/master/docs/Installation_Guide_Linux.md#removal). Removing the StrongKey FIDO Server also removes the sample RP web application and sample WebAuthn client.

## Contributing to the Sample Relying Party Web Application 

If you would like to contribute to the sample Relying Party web application project, please read [CONTRIBUTING.md](https://github.com/StrongKey/fido2/blob/master/CONTRIBUTING.md), then sign and submit the [Contributor License Agreement (CLA)](https://cla-assistant.io/StrongKey/FIDO-Server).

## Licensing
This project is currently licensed under the [GNU Lesser General Public License v2.1](../../../LICENSE).
