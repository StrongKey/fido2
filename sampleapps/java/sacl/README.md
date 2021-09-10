# Collection of FIDO-enabled Sample Applications Demonstrating the Android Client Library and SSO

This project is a service provider web application written in JavaScript and Java to work with the [FIDO(R) Certified StrongKey FIDO2 Server (SKFS), Community Edition](https://github.com/StrongKey/fido2).

Web application developers worldwide face multiple challenges in the near future: learning about FIDO2, coding in FIDO2, demonstrating to decision makers what FIDO2 can do for their company, and acquiring budgets and resources to transition to FIDO2 strong authentication. Unless you spend many weeks (or months) understanding how FIDO2 works, addressing all these challenges remains daunting.

StrongKey has released this project to the open-source community to address these challenges. SKFS allows developers to do the following:

- Setup a FIDO2-enabled single-page web application that can run unmodified and demonstrate FIDO2 registration, authentication, and some simple FIDO2 key management on the client side
- Substitute the stock graphics and logo with your own company's graphics and logo without additional programming&mdash;just replace the graphic image files and reload the application; this allows you to demonstrate to peers and management what FIDO2 can do for the company, and how the user experience (UX) might look in its most basic form
- Learn how FIDO2 works; all the code is available here in a web application framework
- Use the FIDO(R) Certified, open-source FIDO2 server with your web application without having to anticipate deployment issues&mdash;you will have already deployed this FIDO2 Server proof of concept

While this web application can show you how to use W3C's WebAuthn (a subset of the FIDO2 specification) JavaScript, it is also intended to demonstrate how to use FIDO2 protocols with SKFS to enable strong authentication. Follow the instructions below to install this sample.

This folder includes the following sample applications:
  * [SFAECO](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/sacl/sfaeco/): E-commerce application to serve the requests sent by the Android app and the SFABOA
  * [SFABOA](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/sacl/sfaboa/): Sample FIDO e-commerce application (back end)
  * [SFAKMA](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/sacl/sfakma/): FIDO-enabled key management application
  * [Android](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/sacl/mobile/android/): Sample Android native app and the Android client library for FIDO
 
## Installation Instructions for Installing All the Applications to Demonstrate the Andoid App and SSO

1. If installing this sample application **on a separate server**, StrongKey's software stack must be installed to make it work. Follow these steps to do so:
    * **Complete Steps 1&ndash;5** of the [Installation Guide](https://docs.strongkey.com/index.php/skfs-home/skfs-installation/skfs-installation-standalone) but come back here after completing *Step 5*
    * **Edit** the *install-skfs.sh* script in a text editor; on the line where you see **INSTALL_FIDO=Y**, change the value of **Y** to **N**
    * **Run** the script *install-skfs.sh*

    ```sh
     sudo ./install-skfs.sh
    ```
    
2.  **Continue** the installation as shown under _Installation Instructions on a Server with a FIDO2 Server on the SAME Server_. Note that this assumes SKFS was previously installed on the server **without** modifying the _install-skfs.sh_ script.
   
## Installation Instructions on a Server with a FIDO2 Server on the SAME Server

1. The first application we will be installing is **SFAECO**. Follow the [SFAECO Installation Guide](sfaeco/docs/Installation_Guide_Linux.md) instructions to download and install the latest version.

2. The second applicaion we will be installinf is **SFABOA**, which has to be installed on the same machine where SFAECO has been installed in the previous step. Follow the [SFABOA Installation Guide](sfaboa/docs/Installation_Guide_Linux.md) instructions to download and install the latest version.
    
3. The third application we will be installing is **SFAKMA**. Follow the [SFAKMA Installation Guide](sfakma/docs/Installation_Guide_Linux.md) instructions to download and install the latest version.

4. We will now install the **Android native app** on an Android device. Follow the [Androind Native App Installation Guide](mobile/android/docs/SACL.pdf) instructions to download and install the latest version.

5. All the required applications have been installed. You can now **enroll a user** through your Android device and perform transactions.

6. SFABOA is a web application that can now be used to view the transaction performed by the Android user, **simulating 3DS**.

    ```
    https://<FQDN-of-sfaboa-server>:8181/boa
    ```
    
7. **Register a user** to the SFABOA application to view the transactions.

8. To experience the SSO functionality, click the username at the top right and then click on **My Profile**. This redirects to a new application, SFAKMA, which will verify the exisintg JWT that was created when the user logged into SFABOA, then display a list of FIDO keys. If the JWT is invalid then it returns to the SFABOA login page. 

## Removal

To uninstall the service provider sample web application, follow the uninstall instructions in the [SKFS Installation Guide](https://docs.strongkey.com/index.php/skfs-home/skfs-installation/skfs-installation-removal). Removing SKFS also removes the sample service provider web application and sample WebAuthn client.
If this SFABOA was installed on top of SKFS, the cleanup script will erase SKFS as well. If this was a standalone install, the cleanup script will only remove the SFABOA application.

## Contributing to the Sample Service Provider Web Application 

If you would like to contribute to the Sample Service Provider Web Application project, please read [CONTRIBUTING.md](https://github.com/StrongKey/fido2/blob/master/CONTRIBUTING.md), then sign and submit the [Contributor License Agreement (CLA)](https://cla-assistant.io/StrongKey/FIDO-Server).
