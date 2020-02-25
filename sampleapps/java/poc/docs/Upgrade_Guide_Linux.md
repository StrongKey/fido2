# Proof of Concept (PoC) Java Application
This project is a service provider web application written in JavaScript and Java to work with StrongKey's [FIDO Certified FIDO2 Server, Community Edition](https://github.com/StrongKey/fido2).

## Upgrade Instructions

1. Login as the _strongkey_ user. The default password for the _strongkey_ user is _ShaZam123_.

    ```
    su - strongkey
    ```

2. Change the name of the configuration file used by the poc application

    ```sh
    mv /usr/local/strongkey/poc/etc/poc.properties /usr/local/strongkey/poc/etc/poc-configuration.properties
    ```
  
3. Using your favorite text editor (Ex: vi)  edit the configuration file and update the "poc.cfg.property.apiuri" preoperty. (You are removing the /api at the end of the URL)

    ```sh
    poc.cfg.property.apiuri=https://$(hostname):8181
    ```
    
4. Download the service provider web application distribution [pocserver-v1.0-dist.tgz](https://github.com/StrongKey/fido2/raw/master/sampleapps/java/poc/server/pocserver-v1.0-dist.tgz).

    ```sh
    wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/poc/server/pocserver-v1.0-dist.tgz
    ```
5. Extract the downloaded file to the current directory:

    ```sh
    tar xvzf pocserver-v1.0-dist.tgz
    ```
6. Undeploy the old version and deploy the new .war file to Payara.

    ```sh
    payara41/glassfish/bin/asadmin undeploy poc
    payara41/glassfish/bin/asadmin deploy --contextroot poc --name poc pocserver.war
    ```
7. Test that the servlet is running by executing the following cURL command and confirming that you get the API _Web Application Definition Language (WADL)_ file back in response.

    ```sh
    curl -k https://localhost:8181/poc/fido2/application.wadl
    ```
    
8. Download the application distribution for the POC Server [poc-ui-dist.tar.gz](https://github.com/StrongKey/fido2/raw/master/sampleapps/java/poc/angular/poc-ui-dist.tar.gz).
    ```
    wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/poc/angular/poc-ui-dist.tar.gz
    ```

9. Extract the downloaded file.

    ```
    tar xvzf poc-ui-dist.tar.gz
    ```
10. Remove all the files for the old distribution from Payara _docroot_.

    ```
    rm -rf /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/*.js /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/index.html /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/styles.*.css /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/assets/ /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/3rdpartylicenses.txt
    ```
11. Copy all the files for the new poc distribution to the Payara _docroot_.

    ```
    cp -r dist/* /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot
    ```
    
12. Optional: Modify the background image and the logo image.

    ```
    cp <your background> /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/assets/app/media/img/bg/background.jpg
    cp <your logo> /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/assets/app/media/img/logo/logo.png
    ```
13. The application is deployed in _docroot_ on the PoC server and can be accessed as follows in a browser:

    ```
    https://<FQDN-of-PoC-server>:8181/
    ```
