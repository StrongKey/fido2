
# FIDO2 Server, Community Edition, Dockerized
## Overview:
This branch contains a Dockerized version of StrongKey's Certified FIDO2 Server, Community Edition. This implementation allows for the creation of a FIDO2 server within a container, which allows for the ability to be deployed in any environment. This README will focus on the Docker portion of the StrongKey FIDO2 Server; please refer to the main [README](https://github.com/StrongKey/fido2) for anything related to the FIDO side of the server.

## Prerequisites: Host machine/VM
Docker must be installed and enabled to build and use the StrongKey FIDO2 Server image.
It is recommended that you have a machine or VM with the following minimum requirements:

* 10 GB storage
* 4 GB memory

These values are also the recommended minimums for each container. CPUs and memory for each container may be manually allocated at runtime (see [example usage](https://github.com/StrongKey/fido2/blob/docker/docker/example-usage.txt)). If these flags are not set, "[by default, a container has no resource constraints and can use as much of a given resource as the host’s kernel scheduler allows](https://docs.docker.com/config/containers/resource_constraints/)."

The host machine's firewall should open port 8181 (or the port bound to the Docker container's port 8181) as well as ports 7001-7003 if clustering containers.

## Prerequisites: External DB and LDAP/AD
In this specific version of the Dockerized StrongKey FIDO2 Server, both the  MySQL database and LDAP are not included, so an external MySQL 5 database and LDAP/AD are required for setup. The containers can be configured to use the external database and LDAP/AD in the *base64-input.sh* script.

The database configuration should be sourced from the FIDO2 Server's *create.txt* in the server's [*fidoserverSQL* directory](https://github.com/StrongKey/fido2/tree/master/server/fidoserverInstall/fidoserverSQL). Additionally, any database insert commands found in the install-skfs.sh from a regular StrongKey FIDO2 Server should be performed on this database.

Theexternal LDAP/AD must be configured in the same way as a normal FIDO2 Server as described in the distribution's *.ldif* files.

## Getting Started
1. **Build the image**.
	```sh
	[sudo] docker build -t fidoserver .
	```
2. **Configure the image settings**. Here you may configure the external LDAP/AD database, and clustering.
	```sh
	vi base64-input.sh
	```
3. Get base64 input for the container. **Save the output** of the *base64-input.sh* script somewhere for the next step.
	```sh
	./base64-input.sh
	```
4. **Run the image in a container** with the output from the *base64-input.sh* script as its only argument. The hostname flag (-h) and bound port 8181 are necessary. To debug any issues with glassfish, it may be a good idea to remove the -d (detach) flag to be able to view the logs
	```sh
	[sudo] docker run -dit -h fido01.strongkey.com -p 8181:8181 fidoserver <base64-input>
	```
To enter into a bash terminal within the container, perform the following additional steps:

5. **Find the container ID**.
	```sh
	[sudo] docker container ls
	```
6. To enter into a bash terminal in the container, **execute the following**:
	```sh
	[sudo] docker exec -it <CONTAINER-ID> /bin/bash
	```

## Clustering
When clustering,  for replication to work properly, open ports 7001-7003 on the container when running it. For example, 
```sh
[sudo] docker run -dit -h fido01.strongkey.com -p 8181:8181 -p 7001-7003:7001-7003 fidoserver <base64-input>
```
Further instructions on clustering can be found in the *base64-input.sh* when configuring the image settings on *Step 2* of the *Getting Started* section.



