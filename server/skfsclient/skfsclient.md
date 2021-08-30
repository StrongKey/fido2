


# ﻿1 — Test the FIDO2 V3 API
This document provides information about the sample client application called skfsclient, which is used to test StrongKey’s FIDO2 Server running on v3 API. The sample client is a *command-line interface (CLI)*-based client written in the Java programming language, tested on the *Java Development Kit (JDK)* 8. 

Use *skfsclient* to test registering a key, authenticating a key, associating keys to a user, updating a key, and de-registering (deleting) a key.

**NOTE: *skfsclient* relies on *Java Runtime Edition (JRE)*, so make sure it is installed.**

## 1.1 — Usage
1. **Open a terminal** window.
2. **Change directory** to */usr/local/strongkey/skfsclient*, where skfsclient is present. The skfsclient file is named *skfsclient.jar*.
	
		> cd /usr/local/strongkey/skfsclient

3. **Type the command below** to see the usage of the tool:

		> java -jar /usr/local/strongkey/skfsclient skfsclient.jar

Here is a list of operations as well as a brief description of each argument.
	  
	|  Commands  |  Syntax  |
	|  --  |  --  |
	|  Registration &#40;R&#41;  |   <code>java -jar skfsclient.jar R &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ] &lt;username&gt; &lt;origin&gt;</code>
	| Authentication (A)  |  <code>java -jar skfsclient.jar A &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ] &lt;username&gt; &lt;origin&gt; &lt;authcounter&gt;</code>
	|  Getkeysinfo (G)  |  <code>java -jar skfsclient.jar G &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ] &lt;username&gt;</code>
	|  Update (U)  |  <code>java -jar skfsclient.jar U &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ] &lt;random-id&gt; &lt;displayname&gt; &lt;status&gt;</code>
	|  Deregister (D)  |  <code>java -jar skfsclient.jar D &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ] &lt;random-id&gt;</code>
	|  Ping (P)  |  <code>java -jar skfsclient.jar P &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ]</code>
	|  Create Policy (CP)  |  <code>java -jar skfsclient.jar CP &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ] &lt;status&gt; &lt;notes&gt; &lt;policy&gt;</code>
	|  Patch Policy (PP)  |  <code>java -jar skfsclient.jar PP &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ] &lt;sid&gt; &lt;pid&gt; &lt;status&gt; &lt;notes&gt; &lt;policy&gt;</code>
	|  Delete Policy (DP)  |  <code>java -jar skfsclient.jar DP &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ] &lt;sid&gt; &lt;pid&gt;</code>
	|  Get Policy (GP)  |  <code>java -jar skfsclient.jar GP &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ] &lt;metadataonly&gt; &lt;sid&gt; &lt;pid&gt;</code>
	|  Get Configuration (GC)  |  <code>java -jar skfsclient.jar GC &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ]</code>
	|  Update Configuration (UC)  |  <code>java -jar skfsclient.jar UC &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ] &lt;configkey&gt; &lt;configvalue&gt; [ &lt;notes&gt; ]</code>
	|  Delete Configuration (DC)  |  <code>java -jar skfsclient.jar DC &lt;hostport&gt; &lt;did&gt; &lt;wsprotocol&gt; &lt;authtype&gt; [ &lt;accesskey&gt; &lt;secretkey&gt; &#124; &lt;svcusername&gt; &lt;svcpassword&gt; ] &lt;configkey&gt;</code>
	|  hostport  |  Host and port to access the FIDO SOAP and REST formats: <br> ~ http://&lt;FQDN&gt;:&lt;non-ssl-portnumber&gt; or <br>~ https://&lt;FQDN&gt;:&lt;ssl-portnumber&gt; <br>~ Example: https://fidodemo.strongauth.com:8181  |
	|  wsprotocol  |  Web socket protocol. Example: REST / SOAP  |
	|  authtype  |  Authorization type. Example: HMAC / PASSWORD  |
	|  accesskey  |  Access key for use in identifying a secret key.  |
	|  secretkey  |  Secret key for HMACing a request.  |
	|  svcusername  |  Username used for PASSWORD-based authorization.  |
	|  svcpassword  |  Password used for PASSWORD-based authorization.  |
	|  username  |  Username for registration, authentication, or getting keys info.  |
	|  origin  |  Origin to be used by the FIDO client simulator.  |
	|  authcounter  |  Authorization counter to be used by the FIDO client simulator.  |
	|  random-id  |  String associated to a specific FIDO key registered to a specific user. This is needed to perform actions on the key, like de-activate, activate and deregister. Random ID can be obtained by calling 'G' option.  |
	|  status  |  Active/Inactive. Status to set the key or policy to.  |
	|  cert-profile-name  |  A human-readable name for the policy.  |
	|  version  |  Version of the policy (currently only a value of 1 is accepted).  |
	|  notes  |  Optional notes to store with the policy or configuration.  |
	|  policy  |  A JSON object defining the FIDO2 policy.  |
	|  sid  |  Server ID: Policy identifier returned by creating a policy.  |
	|  pid  |  Policy ID: Policy identifier returned by creating a policy.  |
	|  metadataonly  |  Boolean. If true, returns only the metadata of the policy. If false, returns the metadata + the policy JSON.  |
	|  configkey  |  Configuration identifier of server setting.  |
	|  configvalue  |  Value connected to configuration identifier.  |

	The current defaults for HMAC and PASSWORD authentication are as follows:

	**HMAC**
		accesskey 	= 162a5684336fa6e7
		secretkey 	= 7edd81de1baab6ebcc76ebe3e38f41f4
	**PASSWORD**
		svcusername 	= svcfidouser
		svcpassword 	= Abcd1234!
	
## 1.2 — Perform Example Tests (REST and HMAC)
1. **Register a new FIDO key** to a user named *johndoe* using REST and HMAC authorization.
		
        > java -jar skfsclient.jar R https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4 johndoe https://[FQDN]:8181

2. **Authenticate the same user** with the FIDO key registered in the step above using REST and HMAC authorization. Provide an authentication counter starting at 1, since this is the first authentication.
      
        > java -jar skfsclient.jar A https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4 johndoe https://[FQDN]:8181 1

3. **Retrieve a list of the FIDO keys** registered by a specific user using REST and HMAC authorization.

        > java -jar skfsclient.jar G https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4 johndoe

      **NOTE: Each registered key can be deactivated (rendering the FIDO key temporarily unusable with the affected account), reactivated, renamed, and de-registered (permanently deleted, though it may be registered again). Each of these operations on a specific user-registered FIDO key can be acted upon using the *random-id* value that is sent back when the key’s information is retrieved from the server response in *Step 3*, above.**

4. **Update key information** for a specific key using REST and HMAC authorization. In this call you will be able to set the display name and deactivate or reactivate a specific FIDO key.

        > java -jar skfsclient.jar U https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4 1-1-testuser-171 new-display-name Active

5. **De-register (delete) a specific key** using REST and HMAC authorization.

        > java -jar skfsclient.jar D https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4 1-1-testuser-171

6. **Ping the FIDO2 Server** using REST and HMAC authorization.

        > java -jar skfsclient.jar P https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4

## 1.3 — Perform Example Tests (REST and PASSWORD)
1. **Register a new FIDO key** to a user named *johndoe* using REST and PASSWORD authorization.

        > java -jar skfsclient.jar R https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234! johndoe https://[FQDN]:8181

8. **Authenticate the same user** with the FIDO key registered in the step above using REST and PASSWORD authorization. Provide an authentication counter starting at 1, since this is the first authentication.

        > java -jar skfsclient.jar A https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234! johndoe https://[FQDN]:8181 1

9. **Retrieve the list of FIDO keys** registered by a specific user using REST and PASSWORD authorization.

        > java -jar skfsclient.jar G https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234! johndoe

10. **Update key information** for a specific key using REST and PASSWORD authorization. In this call you will be able to set the display name and deactivate or reactivate a specific FIDO key.

        > java -jar skfsclient.jar U https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234! 1-1-testuser-171 new-display-name Active

11. **De-register (delete) a specific key** using REST and PASSWORD authorization.

        > java -jar skfsclient.jar D https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234! 1-1-testuser-171
12. **Ping the FIDO2 Server** using REST and PASSWORD authorization.

        > java -jar skfsclient.jar P https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234!

## 1.4 — Perform Example Tests (SOAP and HMAC)
1. **Register a new FIDO key** to a user named *johndoe* using SOAP and HMAC authorization.

        > java -jar skfsclient.jar R https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4 johndoe https://[FQDN]:8181

14. **Authenticate the same user** with the FIDO key registered in the step above using SOAP and HMAC authorization. Provide an authentication counter starting at 1, since this is the first authentication.

        > java -jar skfsclient.jar A https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4 johndoe https://[FQDN]:8181 1

15. **Retrieve the list of FIDO keys** registered by a specific user using SOAP and HMAC authorization.

        > java -jar skfsclient.jar G https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4 johndoe

16. **Update key information for a specific key** using SOAP and HMAC authorization. In this call you will be able to set the display name and deactivate or reactivate a specific FIDO key.

        > java -jar skfsclient.jar U https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4 1-1-testuser-171 new-display-name Active

17. **De-register (delete) a specific key** using SOAP and HMAC authorization.

        > java -jar skfsclient.jar D https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4 1-1-testuser-171

18. **Ping the FIDO2 Server** using SOAP and HMAC authorization.

        > java -jar skfsclient.jar P https://[FQDN]:8181 1 REST HMAC 162a5684336fa6e7 7edd81de1baab6ebcc76ebe3e38f41f4

## 1.5 — Perform Example Tests (SOAP and PASSWORD)
1. **Register a new FIDO key** to a user named *johndoe* using SOAP and PASSWORD authorization.

        > java -jar skfsclient.jar R https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234! johndoe https://[FQDN]:8181

2. **Authenticate the same user** with the FIDO key registered in the step above using SOAP and PASSWORD authorization. Provide an authentication counter starting at 1, since this is the first authentication.

        > java -jar skfsclient.jar A https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234! johndoe https://[FQDN]:8181 1

3. **Retrieve the list of FIDO keys** registered by a specific user using SOAP and PASSWORD authorization.

        > java -jar skfsclient.jar G https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234! johndoe

4. **Update key information for a specific key** using SOAP and PASSWORD authorization. In this call you will be able to set the display name and deactivate or reactivate a specific FIDO key.

        > java -jar skfsclient.jar U https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234! 1-1-testuser-171 new-display-name Active

5. **De-register (delete) a specific key** using SOAP and PASSWORD authorization.

        > java -jar skfsclient.jar D https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234! 1-1-testuser-171

6. **Ping the FIDO2 Server** using SOAP and PASSWORD authorization.

        > java -jar skfsclient.jar P https://[FQDN]:8181 1 REST PASSWORD svcfidouser Abcd1234!

## 1.6 — Perform Policy Operations (REST and PASSWORD)
1. **Create a new FIDO2 Server policy.**

		> java -jar skfsclient.jar CP https://[FQDN]:8181 1 REST PASSWORD fidoadminuser Abcd1234! Active <policy-json>

2. **Update the current FIDO2 Server policy.**

		> java -jar skfsclient.jar PP https://[FQDN]:8181 1 REST PASSWORD fidoadminuser Abcd1234! 1 1 <policy-json>

3. **Delete the current FIDO2 Server policy.**

		> java -jar skfsclient.jar DP https://[FQDN]:8181 1 REST PASSWORD fidoadminuser Abcd1234! 1 1

4. **Get the current FIDO2 Server policy.**

		> java -jar skfsclient.jar GP https://[FQDN]:8181 1 REST PASSWORD fidoadminuser Abcd1234! false 1 1


## 1.7 — Perform Configuration Operations (REST and PASSWORD)
1. **Get the default FIDO2 Server configurations.**

		> java -jar skfsclient.jar GC https://[FQDN]:8181 1 REST PASSWORD fidoadminuser Abcd1234!

2. **Update the FIDO2 Server configuration.**

		> java -jar skfsclient.jar UC https://[FQDN]:8181 1 REST PASSWORD fidoadminuser Abcd1234! ldape.cfg.property.service.ce.ldap.ldapservicegroup cn=ServicesTest

3. **Delete the FIDO2 Server configuration.**

		> java -jar skfsclient.jar DC https://[FQDN]:8181 1 REST PASSWORD fidoadminuser Abcd1234! ldape.cfg.property.service.ce.ldap.ldapservicegroup


## 1.8 — Example Results
Following are examples of successful outputs; this example uses REST and HMAC.

### 1.8.1 — Registration
![StrongKey FIDO2 Registration](https://github.com/StrongKey/fido2/raw/master/docs/images/02_REST+HMAC_Register_Results.png)

### 1.8.2 — Authentication
![StrongKey FIDO2 Authentication](https://github.com/StrongKey/fido2/raw/master/docs/images/03_REST+HMAC_Authenticate_Results.png)

### 1.8.3 — List FIDO2 Keys
![StrongKey FIDO2 List](https://github.com/StrongKey/fido2/raw/master/docs/images/04_REST+HMAC_List_Results.png)

### 1.8.4 — Update Keys Information
![StrongKey FIDO2 Update](https://github.com/StrongKey/fido2/raw/master/docs/images/05_REST+HMAC_Update_Results.png)

### 1.8.5 — De-register/Delete a Key
![StrongKey FIDO2 Delete](https://github.com/StrongKey/fido2/raw/master/docs/images/06_REST+HMAC_Delete_Results.png)

### 1.8.6 — Ping StrongKey FIDO2 Server
![StrongKey FIDO2 Ping](https://github.com/StrongKey/fido2/raw/master/docs/images/07_REST+HMAC_Ping_Results.png)

