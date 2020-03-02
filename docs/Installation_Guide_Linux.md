#### StrongKey FIDO2 Server, Community Edition for Linux

## Prerequisites

-  **One of the following Linux distributions.** The installation script is untested on other flavors of Linux but may work with slight modifications.
    - RedHat/CentOS/Oracle 7
    - Ubuntu 18.04
    - Debian 9
    - Amazon Linux 2
 
-  **A VM with a minimum of 10GB space and 4GB memory assigned to it.** Some default VMs do not allocate sufficient space and memory, so please verify before getting started.

-  A **fully qualified public domain name (FQDN)**. It is very important to have a hostname that is at least _top-level domain (TLD)_+1 (i.e., [acme.com](http://acme.com), [example.org](http://example.org), etc); otherwise FIDO2 functionality may not work.

-  The installation script instlls Payara running HTTPS on port 8181, so make sure all firewall rules allow that port to be accessed.

- StrongKey's FIDO2 Server must be installed before the sample service provider web application and sample WebAuthn Java client.

----------------

## Installation

**NOTE:** If the install fails for any reason, follow the instructions for [Removal](#removal), below, and restart from the beginning.


1. Open a terminal and **change directory** to the target download folder.

2. Install **wget** if it has not been already.
    ```
    shell> sudo yum install wget
    ```
    or
    ```
    shell> sudo apt install wget
     ```

3.  **Download** the binary distribution file [fido2server-v4.3.0-dist.tgz](https://github.com/StrongKey/fido2/raw/master/fido2server-v4.3.0-dist.tgz).

    ```
    shell> wget https://github.com/StrongKey/fido2/raw/master/fido2server-v4.3.0-dist.tgz
    ```

4.  **Extract the downloaded file to the current directory**:

    ```
    shell> tar xvzf fido2server-v4.3.0-dist.tgz
    ```
5. Be sure the machine's **FQDN is set as its hostname**. This is necessary to properly configure the self-signed certificate for the API. Verify using the following command:

    ```
    shell> hostname
    ```

    If only the machine name is returned, and not the public FQDN, run the following command:

    ```
    shell> sudo hostnamectl set-hostname <SERVER PUBLIC FQDN>
    ```

    If no DNS is configured for this machine, please run the following command to add an entry to the _/etc/hosts_ file.
    **DO NOT run this if the machine does not have a configured FQDN and is still running as _localhost_.**

    ```
    shell> echo `hostname -I | awk '{print $1}'` $(hostname) | sudo tee -a /etc/hosts
    ```

6.  **Execute** the _install-skfs.sh_ script as follows:

    **NOTE : If you are installing on Ubuntu VM, please make sure you are using bash as your default. If the default is set to sh, please execute** `sudo dpkg-reconfigure dash` **to change the detault to bash before continuing.**
    ```
    shell> sudo ./install-skfs.sh
    ```

    The installation script will create a _strongkey_ user account with the home directory of _/usr/local/strongkey_. All software required for the StrongKey FIDO2 Server will be deployed to the _/usr/local/strongkey_ directory and be run by the _strongkey_ user. The default password for the _strongkey_ user is _ShaZam123_.

7. Using the following command, **confirm the FIDO2 Server is running**. The API _Web Application Definition Language (WADL)_ file comes back in response.

    ```
    shell> curl -k https://localhost:8181/skfs/rest/application.wadl
    ```

8. To test this installation of the FIDO2 Server, check out the [Basic Java Sample application](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/basic) or a [JAVA proof of concept (PoC) application](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/poc) which also involves user registration using emails. 

__NOTE__: Both the signing and secret keys in the keystore use default values and should be changed after installation is completed. The keystore and the TrustStore are located in _/usr/local/strongkey/skfs/keystores_. Run the following command from _usr/local/strongkey/keymanager_ to see the usage and syntax for the keymanager tool, then change them both: (The default password for the files is _Abcd1234!_)
    
    ```
    shell> java -jar keymanager.jar
    ```

## Removal

To uninstall StrongKey FIDO2 Server, run the following command from the folder where the distribution was extracted:

    
    shell> sudo ./cleanup.sh

This removes all StrongKey files plus the installed dependency packages. If the sample service provider web application and the StrongKey WebAuthn client are installed, they will be removed as well.
