# FIDO-enabled Key Management Application
This project is a service provider web application written in JavaScript and Java to work with the [FIDO(R) Certified StrongKey FIDO Server (SKFS), Community Edition](https://github.com/StrongKey/fido2).

Web application developers worldwide face multiple challenges in the near future: learning about FIDO2, coding in FIDO2, demonstrating to decision makers what FIDO2 can do for their company, and acquiring budgets and resources to transition to FIDO2 strong authentication. Unless you spend many weeks (or months) understanding how FIDO2 works, addressing all these challenges remains daunting.

StrongKey has released this project to the open source community to address these challenges. SKFS allows developers to do the following:

- Setup a FIDO2-enabled single-page web application that can run unmodified and demonstrate FIDO2 registration, authentication, and some simple FIDO2 key management on the client side
- Substitute the stock graphics and logo with your own company's graphics and logo without additional programming&mdash;just replace the graphic image files and reload the application; this allows you to demonstrate to peers and management what FIDO2 can do for the company, and how the user experience (UX) might look in its most basic form
- Learn how FIDO2 works; all the code is available here in a web application framework
- Use the FIDO(R) Certified open-source FIDO2 server with your web application without having to anticipate deployment issues&mdash;you will have already deployed this FIDO2 Server proof of concept

While this web application can show you how to use W3C's WebAuthn (a subset of the FIDO2 specification) JavaScript, it is also intended to demonstrate how to use FIDO2 protocols with SKFS to enable strong authentication.

## Prerequisites

- This service provider web application example must have a means of connecting with a StrongKey FIDO2 Server
- Install a FIDO2 Server either on the same machine as your service provider web application or a different one
- You must have a Java web application server; these instructions assume you are using Payara (GlassFish)
- These instructions assume the default ports for all the applications installed; Payara runs HTTPS on port 8181 by default, so make sure all firewall rules allow that port to be accessible

## Installation Instructions on a Server with a FIDO2 Server on a SEPARATE Server

1. If installing this sample application **on a separate server**, StrongKey's software stack must be installed to make it work. Follow these steps to do so:
    * **Complete Steps 1&ndash;5** of the [FIDO Server Installation Instructions](https://docs.strongkey.com/index.php/skfs-home/skfs-installation/skfs-installation-standalone) but come back here after completing *Step 5*
    * **Edit** the *install-skfs.sh* script in a text editor; on the line where you see **INSTALL_FIDO=Y** change the value of **Y** to **N**
    * **Run** the script *install-skfs.sh*

        ```sh
         sudo ./install-skfs.sh
        ```
    
2.  **Continue the installation** as shown under _Installation Instructions on a Server with a FIDO2 Server on the SAME Server_. Note that this assumes SKFS was previously installed on the server **without** modifying the _install-skfs.sh_ script.
   
## Installation Instructions on a Server with a FIDO2 Server on the SAME Server

1. **Create** the following directories to configure the WebAuthn servlet home folder:

    ```sh
    sudo mkdir -p /usr/local/strongkey/sfakma/etc
    ```

2. **Create** a configuration file for the service provider web application.

    ```sh
    sudo vi /usr/local/strongkey/sfakma/etc/sfakma-configuration.properties
    ```
3. **Enter the appropriate values** (listed in []) to configure the sample application with an SKFS instance and an email server (you can also use GMail as the mail server with your own GMail account to send emails; just make sure you enable access through the Google account's security settings).
   **If the mail server has a self-signed certificate, make sure to import it in the GlassFish TrustStore before continuing.**

   ```
   sfakma.cfg.property.apiuri=https://**[hostname of FIDO Server]**:8181
   sfakma.cfg.property.mailhost.type=**[SendMail or SSL or StartTLS]**
   sfakma.cfg.property.mailhost=**[localhost or hostname of mailhost]**
   sfakma.cfg.property.mail.smtp.port=**[25 (SendMail) or mail server's port]**
   sfakma.cfg.property.smtp.from=**[local-part of email address]**
   sfakma.cfg.property.smtp.fromName=**[Human readable name associated with email]**
   sfakma.cfg.property.smtp.auth.user=**[Username used to login to mail server]**
   sfakma.cfg.property.smtp.auth.password=**[Password used to login to mail server]**
   sfakma.cfg.property.email.subject=Verify your email address
   sfakma.cfg.property.email.type=HTML
   ```
   **Save and exit.**

4. **Download** the service provider web application distribution [sfakmaserver-v1.0-dist.tgz](https://github.com/StrongKey/fido2/raw/master/sampleapps/java/sacl/sfakma/sfakmaserver/sfakmaserver-v1.0-dist.tgz).

    ```sh
    wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/sacl/sfakma/sfakmaserver/sfakmaserver-v1.0-dist.tgz
    ```

5. **Extract** the downloaded file to the current directory:

    ```sh
    tar xvzf sfakmaserver-v1.0-dist.tgz
    ```

6. **Execute** the _install-sfakmaserver.sh_ script as follows:

    ```sh
    sudo ./install-sfakmaserver.sh
    ```

7. Test that the servlet is running by **executing the following cURL command** and confirming that you get the API _Web Application Definition Language (WADL)_ file back in response.

    ```sh
    curl -k https://localhost:8181/sfakma/fido2/application.wadl
    ```

    The SFAKMA server is installed. Continue to install the front-end Angular application.

8. **Switch users** to (or login as) the _strongkey_ user. The default password for the _strongkey_ user is _ShaZam123_.
    ```
    su - strongkey
    ```

9. **Download** the web application distribution for SKFS [sfakma-ui-dist.tar.gz](https://github.com/StrongKey/fido2/raw/master/sampleapps/java/sacl/sfakma/angular/sfakma-ui-dist.tar.gz).
    ```
    wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/sacl/sfakma/angular/sfakma-ui-dist.tar.gz
    ```

10. **Extract** the downloaded file.

    ```
    tar xvzf sfakma-ui-dist.tar.gz
    ```

11. **Copy** all the files to the Payara _docroot_.

    ```
    mkdir /usr/local/strongkey/payara5/glassfish/domains/domain1/docroot/kma
    cp -r dist/* /usr/local/strongkey/payara5/glassfish/domains/domain1/docroot/kma
    ```
    
12. **Optional: Modify** the background image and the logo image.

    ```
    cp <your background> /usr/local/strongkey/payara5/glassfish/domains/domain1/docroot/assets/app/media/img/bg/background.jpg
    cp <your logo> /usr/local/strongkey/payara5/glassfish/domains/domain1/docroot/assets/app/media/img/logo/logo.png
    ```
13. The application is deployed in _docroot_ on the SFAKMA server. **Access** it as follows in a browser:

    ```
    https://<FQDN-of-sfakma-server>:8181/kma
    ```

## Removal

To uninstall the service provider sample web application, follow the uninstall instructions in the [FIDO2 Server, Community Edition Installation Guide](https://docs.strongkey.com/index.php/skfs-home/skfs-installation/skfs-installation-removal). Removing the SKFS also removes the sample service provider web application and sample WebAuthn client.
If this SFAKMA was installed on top of SKFS, the cleanup script will erase SKFS as well. If this was a standalone install, the cleanup script will only remove the SFAKMA application.

## Contributing to the Sample Service Provider Web Application 

If you would like to contribute to the sample service provider web application project, please read [CONTRIBUTING.md](https://github.com/StrongKey/fido2/blob/master/CONTRIBUTING.md), then sign and submit the [Contributor License Agreement (CLA)](https://cla-assistant.io/StrongKey/FIDO-Server).

## More Information on FIDO2

For detailed information on the FIDO2 project, visit the technical specification:

- [Complete WebAuthn specification](https://www.w3.org/TR/webauthn)
- [A useful diagram of WebAuthn functional flow](https://www.w3.org/TR/webauthn/#api)

For more information on the originating jargon and related terms, visit the Internet Engineering Task Force (IETF) Request for Comments (RFC):

- The definition of "service provider" is in the [second paragraph of 1.1. Background](https://tools.ietf.org/html/rfc3647#section-1.1), therein referred to as, "relying parties."

## Licensing
This project is currently licensed under the [GNU Lesser General Public License v2.1](../../../LICENSE).
