# POC web application

## Prerequisites
- **The sample commands below assume you are installing this RP web application on the same machine where StrongKey FIDO2 Server has been installed.** If you are installing on a separate machine, you may have to adjust the commands accordingly.

## Installation Instructions

A step by step instructions on how to get a development env running
1. Switch to (or login as) the strongkey user. The default password for the strongkey user is ShaZam123.
```
su - strongkey
```

2. Download the web application distribution for the fido server [poc-ui-dist.tar.gz](./poc-ui-dist.tar.gz).
```
wget https://github.com/StrongKey/fido2/raw/master/sampleapps/java/poc/angular/poc-ui-dist.tar.gz
```


3. Extract the downloaded file

```
tar xvzf poc-ui-dist.tar.gz
```
4. Copy all the files to the payara docroot.

```
cp -r dist/* /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot
```
5. Optional: You can modify the background image and the logo image.

```
cp <your background> /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/assets/app/media/image/bg/background.jpg
cp <your logo> /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/assets/app/media/image/logo/logo.png
```
6. The application is deployed in the docroot and can be accessed.

```
https://localhost:8443/
```


