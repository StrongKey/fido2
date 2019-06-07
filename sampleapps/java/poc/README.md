# Proof Of Concept (POC) Java application
This project is a Relying Party (RP) web application written in JavaScript and Java to work with StrongKey's [FIDO Certified FIDO2 Server, Community Edition](https://github.com/StrongKey/fido2).

Web-application developers, worldwide, are going to face multiple challenges in the near future: having to learn about FIDO2, trying to program in FIDO2, demonstrate to management what FIDO2 can do for their company and acquire budgets and resources to transition to FIDO2 strong-authentication.  Unless you've spent many weeks to many months understanding how FIDO2 works, addressing all these challenges remains daunting.

StrongKey has released this project to the open-source community to address all these challenges.

- First, it allows you to setup a FIDO2-enabled single-page web-application that can run unmodified and demonstrate FIDO2 registration, authentication and some simple FIDO2 key-management on the client side.
- Second, it allows you to substitute the stock graphics and logo in this web-applications with your own company's graphics and logo without having to do any programming - you just have to replace the graphic image files and reload the application.  This allows you to demonstrate to your peers and management what FIDO2 can do for the company, and how the user-experience (UX) might look in its most basic form.  
- Third, it allows you to learn how FIDO2 works because you have all the code available to you here in a modern web-application framework.
- Fourth, it allows you to use the FIDO Certified, open-source FIDO2 server with your web-application without having to figure out what the deployment issues are going to look like - you will have already deployed this FIDO2 server in your environment to get this proof-of-concept web-application working.

While this web-application can show you how to use W3C's WebAuthn (a subset of the FIDO2 specification) JavaScript, it is also intended to demonstrate how to use FIDO2 protocols with StrongKey's FIDO2 Server to enable strong-authentication.  Follow the instructions below to install this sample.

## Prerequisites

- This Relying Party web application example must have a means of connecting with a StrongKey FIDO2 Server. You can install a FIDO2 Server either on the same machine as your RP web application or a different one.
- You must have a Java web application server. These instructions assume you are using Payara (GlassFish).
- The instructions assume the default ports for all the applications installed; Payara runs HTTPS on port 8181 by default, so make sure all firewall rules allow that port to be accessible.

## Installation Instructions

1. If installing the sample application **on the same server** as the StrongKey FIDO Server, skip to Step 2. Otherwise, StrongKey's software stack must be installed. Follow these steps for a separate server install:
    * Complete Steps 1-5 of the [FIDO server installation instructions](../../../docs/Installation_Guide_Linux.md) 
    * Edit the *install-skfs.sh* script in a text editor; on the line "INSTALL_FIDO=Y" change the value of "Y" to "N"
    * Run the script *install-skfs.sh*
    ```sh
     sudo ./install-skfs.sh
    ```
   

2. Create the following directories to configure the WebAuthn servlet home folder.

    ```sh
    sudo mkdir -p /usr/local/strongkey/poc/etc
    ```

3. Create a configuration file for the Relying Party web application.

    ```sh
    sudo vi /usr/local/strongkey/poc/etc/poc.properties
    ```
4. Fill in the appropriate values (listed in []) to configure the sample application with a StrongKey FIDO server and an email server. (You can also use GMAIL as the mail server with your own gmail account to send emails out. Just make sure that you enable access through your google account's security settings.)

   ```
   poc.cfg.property.apiuri=https://**[hostname of FIDO Server]**:8181/api
   poc.cfg.property.mailhost.type=**[SendMail or SSL or StartTLS]**
   poc.cfg.property.mailhost=**[localhost or hostname of mailhost]**
   poc.cfg.property.mail.smtp.port=**[25 (SendMail) or mail server's port]**
   poc.cfg.property.smtp.from=**[local-part of email address]**
   poc.cfg.property.smtp.fromName=**[Human readable name associated with email]**
   poc.cfg.property.smtp.auth.user=**[Username used to login to mail server]**
   poc.cfg.property.smtp.auth.password=**[Password used to login to mail server]**
   poc.cfg.property.email.subject=Verify your email address
   poc.cfg.property.email.type=HTML
   ```
   Save and exit

5. Download the Relying Party web application distribution [pocserver-v0.9-dist.tgz](https://github.com/StrongKey/fido2/raw/master/sampleapps/java/poc/server/pocserver-v0.9-dist.tgz).

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
    curl -k https://localhost:8181/poc/fido2/application.wadl
    ```
At this point, we have the POC server installed and will continue to install the frontend angular application.

9. Switch to (or login as) the strongkey user. The default password for the strongkey user is ShaZam123.
```
su - strongkey
```

10. Download the web application distribution for the fido server [poc-ui-dist.tar.gz](https://github.com/StrongKey/fido2/raw/master/sampleapps/java/poc/angular/poc-ui-dist.tar.gz).
```
wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/poc/angular/poc-ui-dist.tar.gz
```

11. Extract the downloaded file

```
tar xvzf poc-ui-dist.tar.gz
```
12. Copy all the files to the payara docroot.

```
cp -r dist/* /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot
```
13. Optional: You can modify the background image and the logo image.

```
cp <your background> /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/assets/app/media/img/bg/background.jpg
cp <your logo> /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/assets/app/media/img/logo/logo.png
```
14. The application is deployed in the docroot and can be accessed.

```
https://<FQDN>:8181/
```

## Removal

To uninstall the RP sample web application, follow the uninstall instructions in the [FIDO2 Server, Community Edition Installation Guide](https://github.com/StrongKey/fido2/blob/master/docs/Installation_Guide_Linux.md#removal). Removing the StrongKey FIDO Server also removes the sample RP web application and sample WebAuthn client.
If this POC was installed on top of the FIDO server, the clean up script will erase the FIDO server as well. If this was a standalone install, the cleanup script will only remove the POC application.

## Contributing to the Sample Relying Party Web Application 

If you would like to contribute to the sample Relying Party web application project, please read [CONTRIBUTING.md](https://github.com/StrongKey/fido2/blob/master/CONTRIBUTING.md), then sign and submit the [Contributor License Agreement (CLA)](https://cla-assistant.io/StrongKey/FIDO-Server).

## A note on "Relying Party"

The cybersecurity term, "Relying Party," originating from the jurisprudential term, was intended to represent legal entities that have a standing within a court of law where disputes related to digital signatures and non-repudiation could be argued; *a web application has no legal standing in court, but its owner does*. For additional information on WebAuthn Relying Parties, visit the technical specification:

- [Definition of WebAuthn Relying Party](https://www.w3.org/TR/webauthn/#webauthn-relying-party)
- [Complete WebAuthn specification](https://www.w3.org/TR/webauthn)
- [A useful diagram of WebAuthn functional flow](https://www.w3.org/TR/webauthn/#api)

For more information on the originating jargon and related terms, visit the Internet Engineering Task Force (IETF) Request for Comments (RFC):

- The definition of Relying Party is in the [second paragraph of 1.1. Background](https://tools.ietf.org/html/rfc3647#section-1.1)

## Licensing
This project is currently licensed under the [GNU Lesser General Public License v2.1](../../../LICENSE).
