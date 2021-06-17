# Basic Java Sample Application
This project is a basic service provider web application written in Java to work with [StrongKey FIDO Server (SKFS), Community Edition](https://github.com/StrongKey/fido2). This project also includes sample JavaScript files providing a basic user interface to test FIDO2 registration and authentication.

## Upgrade Instructions

1. __Login as (or switch to)__ the _strongkey_ user. The default password for the _strongkey_ user is _ShaZam123_.

    ```sh
    su - strongkey
    ```

2. __Rename__ the configuration file.

    ```sh
    mv /usr/local/strongkey/webauthntutorial/etc/webauthntutorial.properties /usr/local/strongkey/webauthntutorial/etc/webauthntutorial-configuration.properties
    ```

3. Using your favorite text editor (Ex: vi) __edit the configuration file__ and update the "webauthntutorial.cfg.property.apiuri" property (remove the "/api" at the end of the URL).

    ```sh
    webauthntutorial.cfg.property.apiuri=https://$(hostname):8181
    ```

4. __Download__ the service provider web application _.war_ file [basicserver.war](https://github.com/StrongKey/fido2/raw/master/sampleapps/java/basic/basicserver.war).

    ```sh
    wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/basic/basicserver.war
    ```

5. __Undeploy the old__ version and __deploy the new__ _.war_ file to Payara.

    ```sh
    payara5/glassfish/bin/asadmin undeploy basicserver
    payara5/glassfish/bin/asadmin deploy basicserver.war
    ```

6. Test that the servlet is running. __Execute the following cURL command__ and confirm that the API _Web Application Definition Language (WADL)_ file comes back in response.

    ```sh
    curl -k https://localhost:8181/basicserver/fido2/application.wadl
    ```
