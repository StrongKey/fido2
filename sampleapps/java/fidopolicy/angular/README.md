# POC web application

## Prerequisites
- **The sample commands below assume you are installing this RP web application on the same machine where [StrongKey FIDO Server (SKFS), Community Edition](https://github.com/StrongKey/fido2) has been installed.** If you are installing on a separate machine, you may have to adjust the commands accordingly.

## Installation Instructions

A step by step instructions on how to get a development env running
1. __Login as (or switch to)__ the _strongkey_ user. The default password for the _strongkey_ user is _ShaZam123_.
```
su - strongkey
```

2. __Download__ the web application distribution for the FIDO server [policy-ui-dist.tar.gz](./policy-ui-dist.tar.gz).
```
wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/poc/angular/policy-ui-dist.tar.gz
```


3. __Extract__ the downloaded file.

```
tar xvzf policy-ui-dist.tar.gz
```

4. __Copy__ all the files to the Payara docroot.

```
cp -r dist/* /usr/local/strongkey/payara5/glassfish/domains/domain1/docroot
```

5. __Optional: Modify the background image and the logo image.__

```
cp <your background> /usr/local/strongkey/payara5/glassfish/domains/domain1/docroot/assets/app/media/img/bg/background.jpg
cp <your logo> /usr/local/strongkey/payara5/glassfish/domains/domain1/docroot/assets/app/media/img/logo/logo.png
```

6. The application is deployed in the docroot and can be accessed. __Browse__ to the following URL.

```
https://localhost:8443/
```


