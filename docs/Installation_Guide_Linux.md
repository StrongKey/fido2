#### StrongKey FIDO2 Server, Community Edition

## Prerequisites

-  **The scripted installation process has been tested on the following Linux distributions only.** The installation script is untested on other flavors of Linux but may work with slight modifications.
    - RedHat/CentOS/Oracle 7
    - Ubuntu 18.04
    - Debian 9
    - Amazon Linux 2

-  A **fully qualified public domain name (FQDN)**. It is very important to have a hostname that is at least _top-level domain (TLD)_+1 (i.e., [acme.com](http://acme.com), [example.org](http://example.org), etc); otherwise FIDO2 functionality may not work.

-  The installation script installs Payara running HTTPS on port 8181, so make sure all firewall rules allow that port to be accessed.

- StrongKey's FIDO2 Server must be installed before the sample Relying Party web application and sample WebAuthn Java client.

----------------

## Installation

**NOTE:** If the install fails for any reason, follow the instructions for [Removal](#removal), below, and restart from the beginning.


1.  Open a terminal and **change directory** to the target download folder.

2. Install **wget** if it has not been already.
    ```sh
    sudo yum install wget 
    or
    sudo apt install wget
     ```

3.  **Download** the binary distribution file [fido2server-v0.9-dist.tgz](../fido2server-v0.9-dist.tgz).

    ```sh
    wget https://github.com/StrongKey/fido2/raw/master/fido2server-v0.9-dist.tgz
    ```

4.  **Extract the downloaded file to the current directory**:

    ```sh
    tar xvzf fido2server-v0.9-dist.tgz
    ```
5. Be sure that you have your machine's **FQDN set as its hostname**. This is necessary to properly configure the self-signed certificate for the API. Check with the following command:

    ```sh
    hostname
    ```

    If you see only the machine name and not the public FQDN, run the following command:

    ```sh
    sudo hostnamectl set-hostname <YOUR SERVER'S PUBLIC FQDN>
    ```

    If you do not have DNS configured for this machine, please run the following command to add an entry to the _/etc/hosts_ file.
    **DO NOT run this if your machine does not have a configured FQDN and is still running as _localhost_.**

    ```sh
    echo `hostname -I | awk '{print $1}'` $(hostname) | sudo tee -a /etc/hosts
    ```

6.  **Execute** the _install-skfs.sh_ script as follows:

    ```sh
    sudo ./install-skfs.sh
    ```

    The installation script will create a _strongkey_ user account with the home directory of _/usr/local/strongkey_. All software required for the StrongKey FIDO2 Server will be deployed to the _/usr/local/strongkey_ directory and be run by the _strongkey_ user. The default password for the _strongkey_ user is _ShaZam123_.

7. Using the following command, **confirm your FIDO2 Server is running**. You should get the API _Web Application Definition Language (WADL)_ file back in response.

    ```sh
    curl -k https://localhost:8181/api/application.wadl
    ```

8. To test this installation of the FIDO2 Server, check out the [Basic Java Sample application](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/basic).

__NOTE__: Both the signing and secret keys in the keystore use default values and should be changed after installation is completed. Run the following command from _usr/local/strongkey/keymanager_ to see the usage and syntax for the keymanager tool, then change them both:
    
    ```java -jar keymanager.jar```


## Removal

To uninstall StrongKey FIDO2 Server, run the following command from the folder where the distribution was extracted:

    ```sh
    sudo ./cleanup.sh
    ```

This removes all StrongKey files plus the installed dependency packages. If you've installed the sample Relying Party web application and the StrongKey WebAuthn client, they will be removed as well.
