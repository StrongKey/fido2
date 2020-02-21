#### StrongKey FIDO2 Server, Community Edition for Linux

## Upgrade

1. Open a terminal and **change directory** to the target download folder.

2.  **Download** the binary distribution file [fido2server-v4.3.0-dist.tgz](https://github.com/StrongKey/fido2/raw/master/fido2server-v4.3.0-dist.tgz).

    ```
    shell> wget https://github.com/StrongKey/fido2/raw/master/fido2server-v4.3.0-dist.tgz
    ```

3.  **Extract the downloaded file to the current directory**:

    ```
    shell> tar xvzf fido2server-v4.3.0-dist.tgz
    ```
    
4.  **Execute** the _upgrade-skfs.sh_ script as follows:

    ```
    shell> sudo ./upgrade-skfs.sh
    ```

5. Using the following command, **confirm the FIDO2 Server is running**. The API _Web Application Definition Language (WADL)_ file comes back in response.

    ```
    shell> curl -k https://localhost:8181/skfs/rest/application.wadl
    ```