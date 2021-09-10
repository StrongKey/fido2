###############################################################
## /**
## * Copyright StrongAuth, Inc. All Rights Reserved.
## *
## * Use of this source code is governed by the GNU Lesser General Public License v2.1
## * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
## */
################################################################

FROM centos:7

# Default payara ports to expose
# 8181: https
# 7001-7003: StrongKey FIDO2 Server Replication
EXPOSE 8181 7001 7002 7003

# Set up environment variables
ENV STRONGKEY_HOME=/usr/local/strongkey\
    SKFS_HOME=/usr/local/strongkey/skfs\
    GLASSFISH_HOME=/usr/local/strongkey/payara5/glassfish\
    GLASSFISH_CONFIG=${GLASSFISH_HOME}/domains/domain1/config\
    MARIACONJAR=mariadb-java-client-2.2.6.jar\
    XMXSIZE=512m

# Create strongkey user
RUN groupadd -g 1000 strongkey &&\
    useradd -u 1000 -m -s /bin/bash -d ${STRONGKEY_HOME} strongkey -g strongkey

# Add to PATH and set up useful aliases for strongkey user
RUN echo "\
    export GLASSFISH_HOME=${GLASSFISH_HOME} ; \ 
    export STRONGKEY_HOME=${STRONGKEY_HOME} ; \ 
              export PATH=${GLASSFISH_HOME}/bin:${STRONGKEY_HOME}/bin:/usr/lib64/qt-3.3/bin:/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin:/root/bin:/root/bin ; \
\
alias str='cd ${STRONGKEY_HOME}' ; \ 
alias dist='cd ${STRONGKEY_HOME}/dist' ; \ 
alias aslg='cd ${GLASSFISH_HOME}/domains/domain1/logs' ; \ 
alias ascfg='cd ${GLASSFISH_HOME}/domains/domain1/config' ; \ 
alias tsl='tail --follow=name ${GLASSFISH_HOME}/domains/domain1/logs/server.log' ; \ 
alias mys='mysql -u skfsdbuser -p\`dbpass 2> /dev/null\` skfs' ; \ 
alias java='java -Djavax.net.ssl.trustStore=${STRONGKEY_HOME}/certs/cacerts'"\
>> /etc/skfsrc &&\
    if [ -f /etc/bashrc ]; then echo ". /etc/skfsrc" >> /etc/bashrc; else echo ". /etc/skfsrc" >> /etc/bash.bashrc; fi

# Install and download necessary libraries and fidoserver files
RUN yum -y install wget unzip libaio java-1.8.0-openjdk ncurses-compat-libs rng-tools curl sudo >/dev/null 2>&1 &&\
    yum clean all &&\
    rm -rf /var/cache/yum &&\
    mkdir fidoserver &&\
    cd fidoserver &&\
    wget https://repo1.maven.org/maven2/fish/payara/distributions/payara/5.2020.7/payara-5.2020.7.zip -q &&\
    wget https://downloads.mariadb.com/Connectors/java/connector-java-2.2.6/mariadb-java-client-2.2.6.jar -q &&\
    wget https://download-ib01.fedoraproject.org/pub/epel/7/x86_64/Packages/j/jemalloc-3.6.0-1.el7.x86_64.rpm -q &&\
    wget https://github.com/StrongKey/fido2/raw/master/fido2server-v4.4.2-dist.tgz -q &&\
    tar xzf fido2server-v4.4.2-dist.tgz

# Create necessary directories
RUN mkdir -p ${STRONGKEY_HOME}/certs ${STRONGKEY_HOME}/Desktop ${STRONGKEY_HOME}/dbdumps ${STRONGKEY_HOME}/lib ${STRONGKEY_HOME}/bin ${STRONGKEY_HOME}/appliance/etc ${STRONGKEY_HOME}/crypto/etc ${SKFS_HOME}/etc ${SKFS_HOME}/keystores ${STRONGKEY_HOME}/skce/etc

# Perform certificate/keystore tasks
RUN cd fidoserver &&\
    cp certimport.sh ${STRONGKEY_HOME}/bin &&\
    exec bash

# Install and set up Glassfish
RUN cd fidoserver &&\
    unzip payara-5.2020.7.zip -d ${STRONGKEY_HOME} >/dev/null &&\
    chown -R strongkey. /usr/local/strongkey &&\
    ${GLASSFISH_HOME}/bin/asadmin start-domain &&\
    cp ${MARIACONJAR} ${GLASSFISH_HOME}/lib &&\
    sed -ri 's|^(com.sun.enterprise.server.logging.GFFileHandler.rotationOnDateChange=).*|\1true|'  ${GLASSFISH_HOME}/domains/domain1/config/logging.properties &&\
    sed -ri 's|^(com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=).*|\1200000000|' ${GLASSFISH_HOME}/domains/domain1/config/logging.properties &&\
    ${GLASSFISH_HOME}/bin/asadmin set server.network-config.network-listeners.network-listener.http-listener-1.enabled=false &&\
    ${GLASSFISH_HOME}/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.http.request-timeout-seconds=7200 &&\
    ${GLASSFISH_HOME}/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.ssl3-tls-ciphers=+TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,+TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,+TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,+TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,+TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,+TLS_DHE_RSA_WITH_AES_256_CBC_SHA &&\
    ${GLASSFISH_HOME}/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.ssl2-enabled=false &&\
    ${GLASSFISH_HOME}/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.ssl3-enabled=false &&\
    ${GLASSFISH_HOME}/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.tls-enabled=false &&\
    ${GLASSFISH_HOME}/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.tls11-enabled=false &&\
    ${GLASSFISH_HOME}/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.http.trace-enabled=false &&\
    ${GLASSFISH_HOME}/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.http.xpowered-by=false &&\
    ${GLASSFISH_HOME}/bin/asadmin delete-jvm-options $(${GLASSFISH_HOME}/bin/asadmin list-jvm-options | sed -n '/\(-XX:NewRatio\|-XX:MaxPermSize\|-XX:PermSize\|-client\|-Xmx\|-Xms\)/p' | sed 's|:|\\\\:|' | tr '\n' ':') &&\
    ${GLASSFISH_HOME}/bin/asadmin create-jvm-options -Djtss.tcs.ini.file=${STRONGKEY_HOME}/lib/jtss_tcs.ini:-Djtss.tsp.ini.file=${STRONGKEY_HOME}/lib/jtss_tsp.ini:-Xmx${XMXSIZE}:-Xms${XMXSIZE}:-Djdk.tls.ephemeralDHKeySize=2048:-Dproduct.name="":-XX\\:-DisableExplicitGC &&\
    cp fidoserver.ear /usr/local/strongkey &&\
    cp signingkeystore.bcfks /usr/local/strongkey &&\
    cp signingtruststore.bcfks /usr/local/strongkey &&\
    cp jwtsigningkeystore.bcfks /usr/local/strongkey &&\
    cp jwtsigningtruststore.bcfks /usr/local/strongkey &&\
    cp -r keymanager/ /usr/local/strongkey &&\
    cp -r skfsclient/ /usr/local/strongkey &&\
    cp keygen-jwt.sh /usr/local/strongkey &&\
    chown -R strongkey. /usr/local/strongkey &&\
    cd ../ &&\
    rm -rf fidoserver

# Copy entrypoint script to image to run when container is initialized
COPY ./entrypoint.sh ${STRONGKEY_HOME}
RUN chown -R strongkey. /usr/local/strongkey/entrypoint.sh

# All operations from here on should be run as strongkey user
USER strongkey
WORKDIR ${STRONGKEY_HOME}
ENTRYPOINT ["/usr/local/strongkey/entrypoint.sh"]
