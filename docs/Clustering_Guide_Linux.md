# StrongKey FIDO2 Server, Community Edition for Linux

## Cluster Installation

StrongKey's FIDO2 Server can be clustered with multiple nodes to deliver high availability (HA) across a local area network (LAN) and/or disaster recovery (DR) on a wide area network (WAN). No additonal software is required to enable these features because StrongKey has enabled this capability as a **standard** feature in its FIDO2 Server. Furthermore, with multiple nodes processing FIDO2 transactions at the same time, the StrongKey FIDO2 cluster can deliver higher throughput to multiple web applications that use this server. This document guides you through the setup of a FIDO2 cluster with two nodes, as depicted in the image below.

**The clustering capability in StrongKey's FIDO2 Server only applies to the FIDO2 capability**. Web applications that use the StrongKey FIDO2 Server must make their own arrangements to deliver HA and/or DR independent of the StrongKey FIDO2 Server. The sample application used here to demonstrate FIDO2 clustering will, itself, not be highly available, but demonstrates that the web application can use either or both FIDO2 Servers in this HA configuration.

While it is possible to add more than two nodes to the cluster, IT architects will recognize that there is a trade-off with N-way replication designs&mdash;the more nodes in such a configuration, the higher the resource requirements on each node to manage fail-safe replication, which can reduce the overall throughput after a certain point. Each site will have to do its own testing to determine where the throughput curve flattens out. However, if you have truly large-scale deployments in mind, please contact us to see how we can help.

## Sample Cluster Configuration

![StrongKey FIDO2 Server Clustering](https://github.com/StrongKey/fido2/raw/master/docs/images/fido2cluster.png)

## Prerequisites

1. **Two (2)** virtual machines (VMs) for the FIDO2 Servers, running the current version of CentOS Linux 7.x, with fully qualified domain names (FQDN) and internet protocol (IP) addresses
2. **One (1)** virtual machine for the load-balancer, running HAProxy version 1.5.18 software on the current version of CentOS Linux 7.x with an FQDN and an IP address
3. **One (1)** virtual machine for the StrongKey sample Proof-of-Concept web-application from this Github repository, also running on the current version of CentOS Linux 7.x with an FQDN and an IP address

**NOTE:** This document assumes you are setting up this cluster with all nodes connected to a single ethernet switch. If your intent is to do a more realistic test, you should plan on using VMs with multiple network interfaces connected to different switches to isolate traffic to the appropriate segments as you might except in a more real-world environment.


## How To Setup the Cluster

1. Using the installation steps [here](../docs/Installation_Guide_Linux.md), install and configure the two FIDO2 Server VMs **as if they were individual FIDO2 Servers, but do NOT install any web applications to test out the FIDO2 Server at this point**; we will do this later.
2. For each server **determine the FQDN and assign it a unique Server ID**. A _Server id (SID)_ is a numeric value that uniquely identifies a node within the cluster. Conventionally, StrongKey cluster SIDs begin with the numeral **1** and continue incrementally for each node in the cluster. In the current setup, the following values are used:
	
	|  SID  |  FQDN  |
	|  --  |  --  |
	|  1  |  fidoserver1.strongkey.com  |
	|  2  |  fidoserver2.strongkey.com  |
		
3. As the **_root_** user, perform the following sub-tasks on every FIDO2 Server in the cluster:

	a. Login as _root_.
	
	b. If DNS **is configured**, make sure that it is configured for **forward and reverse** lookups&mdash;meaning that it should be possible to resolve the IP address using the FQDN, as well as resolve the FQDN using the IP address doing a reverse lookup. Without the reverse resolution, services in the Payara application server configuration will not work correctly.
	
	If Domain Name Service (DNS) **is not configured**, add the following entries to the **_/etc/hosts_** file to identify the cluster nodes. Use a text editor such as _vi_ to modify the _/etc/hosts_ file. For the two-node cluster, add the following to the end of the _hosts_ file, substituting the _strongkey.com_ domain name for your own environment:
		
		<ip-fidoserver1>	fidoserver1.strongkey.com fidoserver1
		<ip-fidoserver2>	fidoserver2.strongkey.com fidoserver2
	
	c. **Modify the firewall** configuration to open ports 7001, 7002, and 7003 to accept connections between _just the FIDO2 Servers_ to enable multi-way replication. Run the following command once for each cluster node's IP address (substituting for \<ip-target-fidoserver\>). 
	
	**Do _not_ execute this command for the IP address of the cluster node on which you are executing the command itself**. It is not necessary to open the node's ports on the firewall for itself since the replication module in the FIDO2 Server does not need to replicate to itself.
	
		shell> firewall-cmd --permanent --add-rich-rule 'rule family="ipv4" source address='<ip-target-fidoserver>' port port=7001-7003 protocol=tcp accept'
	
	d. After adding the new rule, **restart the firewall**:

		shell> systemctl restart firewalld

	e. Logout from the _root_ account.
4.  As the **_strongkey_ user**, perform the following on every FIDO2 Server node to be clustered:
	
	a. Login to the server as _strongkey_ (the default password is _ShaZam123_).
	
	b. Using a text editor, edit the configuration properties of the FIDO2 Server node; if the specified file is empty add these properties:
	
		shell> vi /usr/local/strongkey/appliance/etc/appliance-configuration.properties
		
		appliance.cfg.property.serverid=<server-id> (set the value to the corresponding SID of the current node)
		appliance.cfg.property.replicate=true (should be set to true)
		
	c. Using the **_mysql_** client, login to the MariaDB database that was installed as part of the FIDO2 Server installation. The default password for the _skfsdbuser_ is _AbracaDabra_.

		shell> mysql -u skfsdbuser -p skfs
	
	d. **Truncate** the existing _SERVERS_ table&mdash;this deletes all contents of the SERVERS table:

		mysql> truncate SERVERS;
	
	e. **Insert the following entries** into the _SERVERS_ table, ensuring the SID and FQDN match the values used in _Step 2_.
	    For example, if there are two nodes in the cluster, add the following entries:

		mysql> insert into SERVERS values (1, 'fidoserver1.strongkey.com', 'Active', 'Both', 'Active', null, null);
		mysql> insert into SERVERS values (2, 'fidoserver2.strongkey.com', 'Active', 'Both', 'Active', null, null);
	
	f. **Logout of the _mysql_ client**:
        
        mysql> exit
		
 	g. **Import the self-signed certificates** generated as part of the FIDO2 Server installation into the Payara Application Server's truststore&mdash;this is necessary to ensure that replication between the FIDO2 Server nodes occurs over a trusted Transport Layer Security (TLS) connection. Execute the _certimport.sh_ script included in the _/usr/local/strongkey/bin_ directory to import the certificate.
	 
	 	shell> /usr/local/strongkey/bin/certimport.sh fidoserver1.strongkey.com -kGLASSFISH
	 	shell> /usr/local/strongkey/bin/certimport.sh fidoserver2.strongkey.com -kGLASSFISH
		
 	h. **Restart the Payara Application Server**. (Even though we refer to it as the Payara Application Server, the startup script is named _glassfishd_ for legacy reasons given Payara's origins from the open-source GlassFish Application Server):

		shell> sudo service glassfishd restart
		
	i. **Repeat *Steps 3 and 4*** on the remaining StrongKey FIDO2 Server nodes of this cluster.
	

## Install HAProxy Load Balancer

High availability (HA) is enabled for applications by inserting a _load balancer_ between components of the infrastructure, such as between the web application and the two FIDO2 Servers of this configuration. The load balancer determines which target server is available to receive application connections, and distributes application requests to the appropriate target server.

StrongKey's FIDO2 Server has been tested with the open-source HAProxy load balancer, part of the standard CentOS Linux distribution. It is conceivable that the StrongKey FIDO2 Server will work with other load balancers; please contact us to discuss your needs.

To install and configure HAProxy for use with the FIDO2 Server cluster, follow the steps below:

a. **Install the standard CentOS 7.x Linux** distribution on one of the four VMs provisioned for this setup.
 
b. **Login** to the server as _root_.
 
c. **install HAProxy** using the _Yellowdog Updater, Modified (yum)_ tool:
      
    shell> yum install haproxy
    
d. **Create a self-signed certificate** to be used by HAProxy, replacing the value in the **-subj** parameter with the value relevant to your site. The most important element within this parameter is the **CN** component&mdash;the value **must** match the FQDN of the VM used for this load balancer; so if you choose to name your VM _fidoserver.mydomain.com_ then the **-subj** parameter may simply be "/CN=fidoserver.mydomain.com":
 
    shell> openssl req -x509 -nodes -days 365 -newkey rsa:2048 -keyout /etc/pki/tls/private/fidoserver.key -out /etc/pki/tls/certs/fidoserver.crt -subj "/CN=saka02.strongkey.com"

    
e. **Concatenate the generated key and certificate files** into a single file, preserving the names of the files as shown below:
 
    shell> cat /etc/pki/tls/certs/fidoserver.crt /etc/pki/tls/private/fidoserver.key > /etc/pki/tls/certs/fidoserver.pem

f. Using a text editor, **edit the HAProxy configuration file** to make the following changes:
    
    shell> vi /etc/haproxy/haproxy.cfg
    
   Replace the content with the following and replace the **\<ip-fidoserver1\>** and **\<ip-fidoserver2\>** parameters with the IP addresses for the FIDO2 Servers:
    
    global
        log 127.0.0.1   local0
        log 127.0.0.1   local1 debug
        maxconn   45000 # Total Max Connections.
        daemon
        nbproc      1 # Number of processing cores.
    defaults
        timeout server 86400000
        timeout connect 86400000
        timeout client 86400000
        timeout queue   1000s

    listen  https_web
        bind *:443 ssl crt /etc/pki/tls/certs/fidoserver.pem
        option tcplog
        mode http
        balance roundrobin
        option forwardfor
        server server1 <ip-fidoserver1>:8181 check ssl verify none
        server server2 <ip-fidoserver2>:8181 check ssl verify none

g. Create a firewall rule opening port 443 to enable the web application to communicate with the load balancer:

	shell> firewall-cmd --permanent --add-rich-rule 'rule family="ipv4" port port=443 protocol=tcp accept'
	
h. **Restart HAProxy**:
 
    shell> service haproxy restart
    
i. Verify HAProxy is functioning as expected by accessing the URL in the browser. If it is functioing correctly, it will redirect you to one of the configured FIDO2 Servers.

    https://<fidoserver.mydomain.com_>
    

### Troubleshooting

If you cannot access the above URL in the browser, ensure that the **selinux** config has been set to _permissive_ instead of _enforcing_.

The following command will show you the current status of selinux:
   
    shell> sestatus
    
If it is set to _enforcing_, change it to _permissive_ by running the following command:

    shell> setenforce 0


## Testing the FIDO2 Server Cluster with a Sample Web Application

To test the cluster with a sample web application, provision the fourth VM to install the sample application and follow the steps [here](https://github.com/StrongKey/fido2/tree/master/sampleapps/java/poc) to install the **StrongKey Proof of Concept (PoC) Java Application**. When installing the PoC application, make sure that you **follow the steps to NOT install it with a FIDO2 Server on the VM**; because you already have a FIDO2 Server cluster setup following this document, there is no need for an additional FIDO2 Server.

The StrongKey PoC Java application is a self-contained web application that demonstrates the use of StrongKey's FIDO2 Server for registering users with FIDO2 and U2F Authenticators and, once registered, authenticating them with those Authenticators. The PoC web application also showcases a key management panel where registered users may add manage Authenticator keys to their account.

a. **Login** to the PoC VM as **_strongkey_** upon completing its installation.

b. Using a text editor such as _vi_, **modify the application's configuration properties** to point the application to the HAProxy load balancer setup in the earlier section of this document:

    shell> vi /usr/local/strongkey/poc/etc/poc.properties
    
 **Change the value of the property _poc.cfg.property.apiuri_** and point it to the new load balancer, replacing the **\<load-balancer-FQDN\>** parameter with the FQDN of the HAProxy VM from your environment:
    
    poc.cfg.property.apiuri=https://<load-balancer-FQDN>/api
    
c. **Run the _certimport.sh_ script** to import the load balancer's self-signed certificate into the Payara application server running on the PoC VM:

    shell> /usr/local/strongkey/bin/certimport.sh <load-balancer-FQDN> -p443 -kGLASSFISH
    
d. **Restart the Payara application server**:

    shell> sudo service glassfishd restart
    
e. Open a browser to the appropriate URL to **access the PoC application** on the PoC VM, replacing the **\<PoC-VM-FQDN\>** with the FQDN of the VM on which the PoC application is installed:

    https://<PoC-VM-FQDN>:8181

## Simulating Node Failures in the FIDO2 Server Cluster

Following are several methods to simulate failures of a FIDO2 Server node within the cluster for verification purposes:

 1. Remove the ethernet cable from one of the FIDO2 Server nodes.
 2. Shut down the Payara Applicatiion Server on one of the FIDO2 Server nodes.
 3. Close port 8181 by disabling the firewall rule that accepts connections on the FIDO2 Server.
 4. Modify the configuration of HAProxy on the load balancer to remove one of the FIDO2 Servers.

__NOTE:__ Because of the complexity of the FIDO2 protocols as well as its implementation in StrongKey's FIDO2 Server, some _in-flight_ FIDO2 registrations and/or authentications may see failures due to the simulated outage (as might occur in a real-world environment). Application architects should consider how they might choose to address these failures within their web applications&mdash;the PoC was designed to demonstrate simple FIDO2 transactions and not a specific business application and, hence, does not handle these failures as gracefully as might be desired.

StrongKey definitely appreciates feedback on how it might improve the FIDO2 Server to better serve this community's needs. Please feel free to provide feedback through the forum on Github. Thank you.

## Removal

To uninstall StrongKey FIDO2 Server, run the following command (on every server in the cluster) from the folder where the distribution was extracted:

    
    shell> sudo ./cleanup.sh

This removes all StrongKey files plus the installed dependency packages. If the sample service provider web application and the StrongKey WebAuthn client are installed, they will be removed as well.
