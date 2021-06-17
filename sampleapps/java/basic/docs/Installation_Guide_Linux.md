# Basic Java Sample Application
This project is a basic service provider web application written in Java to work with [StrongKey FIDO Server (SKFS)](https://github.com/StrongKey/fido2), Community Edition. This project also includes sample JavaScript files providing a basic user interface to test FIDO2 registration and authentication.

## Prerequisites

- This service provider web application example must have a means of connecting with SKFS; you can install SKFS either on the same machine as your service provider web application or a different one
- You must have a Java web application server; these instructions assume you are using Payara (GlassFish)
- The instructions assume the default ports for all the applications installed; Payara runs HTTPS on port 8181 by default, so make sure all firewall rules allow that port to be accessible
- **The sample commands below assume you are installing this service provider web application on the same machine where StrongKey FIDO2 Server has been installed;** if you are installing on a separate machine, you may have to adjust the commands accordingly

## Installation Instructions

1. __Login as (or switch to)__ the _strongkey_ user. The default password for the _strongkey_ user is _ShaZam123_.

    ```sh
    su - strongkey
    ```

2. __Create the following directories__ to configure the WebAuthn servlet home folder.

    ```sh
    mkdir -p /usr/local/strongkey/webauthntutorial/etc
    ```

3. __Create a configuration file__ for the service provider web application to configure a FIDO2 Server.

    ```sh
    echo "webauthntutorial.cfg.property.apiuri=https://$(hostname):8181" > /usr/local/strongkey/webauthntutorial/etc/webauthntutorial-configuration.properties
    ```

4. __Download__ the service provider web application _.war_ file [basicserver.war](https://github.com/StrongKey/fido2/raw/master/sampleapps/java/basic/basicserver.war).

    ```sh
    wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/basic/basicserver.war
    ```

5. __Add__ the _.war_ file to Payara.

    ```sh
    payara5/glassfish/bin/asadmin deploy basicserver.war
    ```

6. Test that the servlet is running: __Execute the following cURL command__ and confirm that you get the API _Web Application Definition Language (WADL)_ file back in response.

    ```sh
    curl -k https://localhost:8181/basicserver/fido2/application.wadl
    ```


## Removal

To uninstall the service provider sample web application, follow the uninstall instructions in the [StrongKey FIDO Server (SKFS), Community Edition Installation Guide](https://github.com/StrongKey/fido2/blob/master/docs/Installation_Guide_Linux.md#removal). Removing SKFS also removes the sample service provider web application and sample WebAuthn client.

## Contributing to the Sample Service Provider Web Application 

If you would like to contribute to the sample service provider web application project, please read [CONTRIBUTING.md](https://github.com/StrongKey/fido2/blob/master/CONTRIBUTING.md), then sign and submit the [Contributor License Agreement (CLA)](https://cla-assistant.io/StrongKey/FIDO-Server).

## Licensing
This project is currently licensed under the [GNU Lesser General Public License v2.1](https://github.com/StrongKey/relying-party-java/blob/master/LICENSE).
