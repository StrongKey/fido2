# Basic Java Sample Application
This project is a basic service provider web application written in Java to work with StrongKey's [FIDO2 Server, Community Edition](https://github.com/StrongKey/fido2). This project also includes sample JavaScript files providing a basic user interface to test FIDO2 registration and authentication.

## Prerequisites

- This service provider web application example must have a means of connecting with a StrongKey FIDO2 Server; you can install a FIDO2 Server either on the same machine as your service provider web application or a different one
- You must have a Java web application server; these instructions assume you are using Payara (GlassFish)
- The instructions assume the default ports for all the applications installed; Payara runs HTTPS on port 8181 by default, so make sure all firewall rules allow that port to be accessible
- **The sample commands below assume you are installing this service provider web application on the same machine where StrongKey FIDO2 Server has been installed;** if you are installing on a separate machine, you may have to adjust the commands accordingly

## Installation Instructions

1. Switch to (or login as) the _strongkey_ user. The default password for the _strongkey_ user is _ShaZam123_.

    ```sh
    su - strongkey
    ```

2. Create the following directories to configure the WebAuthn servlet home folder.

    ```sh
    mkdir -p /usr/local/strongkey/webauthntutorial/etc
    ```

3. Create a configuration file for the service provider web application to configure a FIDO2 Server.

    ```sh
    echo "webauthntutorial.cfg.property.apiuri=https://$(hostname):8181" > /usr/local/strongkey/webauthntutorial/etc/webauthntutorial-configuration.properties
    ```

4. Download the service provider web application .war file [basicserver.war](https://github.com/StrongKey/fido2/raw/master/sampleapps/java/basic/basicserver.war).

    ```sh
    wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/basic/basicserver.war
    ```

5. Add the .war file to Payara.

    ```sh
    payara41/glassfish/bin/asadmin deploy basicserver.war
    ```

6. Test that the servlet is running by executing the following cURL command and confirming that you get the API _Web Application Definition Language (WADL)_ file back in response.

    ```sh
    curl -k https://localhost:8181/basicserver/fido2/application.wadl
    ```


## Removal

To uninstall the service provider sample web application, follow the uninstall instructions in the [FIDO2 Server, Community Edition Installation Guide](https://github.com/StrongKey/fido2/blob/master/docs/Installation_Guide_Linux.md#removal). Removing the StrongKey FIDO Server also removes the sample service provider web application and sample WebAuthn client.

## Contributing to the Sample Service Provider Web Application 

If you would like to contribute to the sample service provider web application project, please read [CONTRIBUTING.md](https://github.com/StrongKey/fido2/blob/master/CONTRIBUTING.md), then sign and submit the [Contributor License Agreement (CLA)](https://cla-assistant.io/StrongKey/FIDO-Server).

## Licensing
This project is currently licensed under the [GNU Lesser General Public License v2.1](https://github.com/StrongKey/relying-party-java/blob/master/LICENSE).
