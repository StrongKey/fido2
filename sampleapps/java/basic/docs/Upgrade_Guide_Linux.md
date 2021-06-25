# Basic Java Sample Application
This project is a basic service provider web application written in Java to work with StrongKey's [FIDO2 Server, Community Edition](https://github.com/StrongKey/fido2). This project also includes sample JavaScript files providing a basic user interface to test FIDO2 registration and authentication.

## Upgrade Instructions

1. Switch to (or login as) the _strongkey_ user. The default password for the _strongkey_ user is _ShaZam123_.

    ```sh
    su - strongkey
    ```

2. Change the name of the configuration file.

    ```sh
    mv /usr/local/strongkey/webauthntutorial/etc/webauthntutorial.properties /usr/local/strongkey/webauthntutorial/etc/webauthntutorial-configuration.properties
    ```

3. Using your favorite text editor (Ex: vi)  edit the configuration file and update the "webauthntutorial.cfg.property.apiuri" preoperty. (You are removing the /api at the end of the URL)

    ```sh
    webauthntutorial.cfg.property.apiuri=https://$(hostname):8181
    ```

4. Download the service provider web application .war file [basicdemo.war](https://github.com/StrongKey/fido2/raw/master/sampleapps/java/basic/basicdemo.war).

    ```sh
    wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/basic/basicdemo.war
    ```

5. Undeploy the old version and deploy the new .war file to Payara.

    ```sh
    payara5/glassfish/bin/asadmin undeploy basicserver
    payara5/glassfish/bin/asadmin deploy basicdemo.war
    ```

6. Test that the servlet is running by executing the following cURL command and confirming that you get the API _Web Application Definition Language (WADL)_ file back in response.

    ```sh
    curl -k https://localhost:8181/basicdemo/fido2/application.wadl
    ```
