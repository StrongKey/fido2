# Project Title

POC web application


### Installing

A step by step instructions on how to get a development env running

1.Download the web application distribution for the fido server [poc-ui-dist.tar.gz](./dist/demo6/poc-git.tar.gz).


2.Extract the downloaded file

```
tar xvzf poc-ui-dist.tar.gz
```
3.Copy all the files to the payara docroot.

```
cp -r dist/* /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot
```
4.Optional: You can modify the background image and the logo image.

```
cp <your background> /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/assets/app/media/image/bg/background.jpg
cp <your logo> /usr/local/strongkey/payara41/glassfish/domains/domain1/docroot/assets/app/media/image/logo/logo.png
```
5.The application is deployed in the docroot and can be accessed.

```
https://localhost:8443/
```


