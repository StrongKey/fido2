#### StrongKey FIDO2 Server, Community Edition for Linux

## Prerequisites

-  **One of the following Linux distributions.** The installation script is untested on other flavors of Linux but may work with slight modifications.
    - RedHat/CentOS/Oracle 7
    - Ubuntu 18.04
    - Debian 9
    - Amazon Linux 2
    
----------------

-  A **fully qualified public domain name (FQDN)**. It is very important to have a hostname that is at least _top-level domain (TLD)_+1 (i.e., [acme.com](http://acme.com), [example.org](http://example.org), etc); otherwise FIDO2 functionality may not work.

-  The installation script installs Payara running HTTPS on port 8181, so make sure all firewall rules allow that port to be accessed.

- StrongKey's FIDO2 Server must be installed before the sample service provider web application and sample WebAuthn Java client.

----------------

## Installation

**NOTE:** If the install fails for any reason, follow the instructions for [Removal](#removal), below, and restart from the beginning.


1. Open a terminal and **change directory** to the target download folder.

2. Install **wget** if it has not been already.
    ```sh
    sudo yum install wget 
    or
    sudo apt install wget
     ```

3.  **Download** the binary distribution file [fido2server-v0.9-dist.tgz](https://github.com/StrongKey/fido2/raw/master/fido2server-v0.9-dist.tgz).

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

8. To test this installation of the FIDO2 Server, check out the [Basic Java Sample application](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/basic) or a [JAVA proof of concept (PoC) application](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/poc) which also involves user registration using emails. 

__NOTE__: Both the signing and secret keys in the keystore use default values and should be changed after installation is completed. The keystore and the TrustStore are located in _/usr/local/strongkey/skfs/keystores_. Run the following command from _usr/local/strongkey/keymanager_ to see the usage and syntax for the keymanager tool, then change them both:
    
    java -jar keymanager.jar


## Clustering

**NOTE: The Clustering capability only applies to the FIDO2 Server and not to any other sample applications that are available to use on github.**

1. Using the Installation steps in the previous section, **individually install and configure all the FIDO2 Servers to be clustered**.
2. For each server **determine the FQDN and assign it a unique server ID**. A _server id (SID)_ is a numeric value that uniquely identifies a server in the cluster.
	For example, a two node cluster will look as follows:
	
	|  SID  |  FQDN  |
	|  --  |  --  |
	|  1  |  fidoserver1.strongkey.com  |
	|  2  |  fidoserver2.strongkey.com  |
		
3. **As the _root_ user**, perform the following on every server to be clustered:

	a. **Login** to the server as _root_.
	
	b. If no DNS is configured, **add entries to the server _/etc/hosts_** file to identify all machines in the cluster. Using a text  editor, modify _/etc/hosts_ file and add entries for all the servers:
		
		shell> vi /etc/hosts
	For example, for a two-node cluster, add the following to the end of the _hosts_ file:
		
		<ip-fidoserver1>	fidoserver1.strongkey.com
		<ip-fidoserver2>	fidoserver2.strongkey.com
	
	c. **Modify the firewall** configuration to allow ports 7001,7002, and 7003 for replication between the servers. Run the following command once for each server IP address (substitute for <ip-target-fidoserver>) in the cluster **except** the server being modified:
	
		shell> firewall-cmd --permanent --add-rich-rule 'rule family="ipv4" source address='<ip-target-fidoserver>' port port=7001-7003 protocol=tcp accept'
	
	d. After adding the new rules, **restart the firewall**:

		shell> systemctl restart firewalld

	
4.  **As the _strongkey_ user**, perform the following on every server to be clustered:
	
	a. **Login** to the server as _strongkey_.
	
	b. Using a text editor, **edit the appliance configuration properties** and modify the following (add any properties that do not already exist):
	
		shell> vi /usr/local/strongkey/appliance/etc/appliance-configuration.properties
		
		appliance.cfg.property.serverid=<server-id> (set to the corresponding SID)
		appliance.cfg.property.replicate=true (should be set to true)
		
	c. Now **login to MySQL** (Enter the password when prompted, default password : AbracaDabra):

		shell> mysql -u skfsdbuser -p skfs
	d. **Truncate** the existing _SERVERS_ table:

		mysql> truncate SERVERS;
	e. **Insert the new entries** into the _SERVERS_ table. Make sure the SID and FQDN match the values used in _Step 2_.
	    For example, if there are two nodes in the cluster, add the following entries:

		mysql> insert into SERVERS values (1, 'fidoserver1.strongkey.com', 'Active', 'Both', 'Active', null, null);
		mysql> insert into SERVERS values (2, 'fidoserver2.strongkey.com', 'Active', 'Both', 'Active', null, null);
	
	f. **Logout of MySQL**.
        
        mysql> exit
		
 	g. **Import the self-signed certificate** generated by all the servers into the GlassFish TrustStore. Run the _certimport.sh_ script included in the _/bin_ directory to import the certificate.
	    For example, if there are two nodes in the cluster, run the following commands:
	 
	 	shell> /usr/local/strongkey/bin/certimport.sh fidoserver1.strongkey.com -kGLASSFISH
	 	shell> /usr/local/strongkey/bin/certimport.sh fidoserver2.strongkey.com -kGLASSFISH
		
 	h. **Restart GlassFish**:

		shell> sudo service glassfishd restart
	i. **Repeat the above sequence** of steps (a&ndash;f) on all remaining StrongKey FIDO2 Servers.
	

## Removal

To uninstall StrongKey FIDO2 Server, run the following command from the folder where the distribution was extracted:

    sh
    sudo ./cleanup.sh

This removes all StrongKey files plus the installed dependency packages. If you've installed the sample service provider web application and the StrongKey WebAuthn client, they will be removed as well.
