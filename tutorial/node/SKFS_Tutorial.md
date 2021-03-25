# StrongKey FIDO2 Tutorial

## Requirements
-   Node.js 10.x.x+
-   SQLite 3.7.17

**NOTE:** If you are planning to test the client and the server component of this web application on the same computer—designated COMBINED in this document—then make sure you have a version of the browser that supports FIDO2.

If you plan to use multiple computers to test this web application, the computer on which the server part of this web application is running is designated as APPSERVER while the computer running the browser(s) is designated APPCLIENT.

The computer running the FIDO2 server is designated FIDO2SERVER.

### Installing Required Software Components
**CentOS 8**

    sudo yum install -y gcc-c++ make
    sudo yum install nodejs
    sudo yum install sqlite

**CentOS 7**
Install any version 10.x.x or higher of _Node.js_ from the following link:
[https://nodejs.org/en/download/](https://nodejs.org/en/download/)

    sudo yum install npm
    sudo yum install sqlite

**Ubuntu**

    sudo apt-get update
    sudo apt install nodejs
    sudo apt install npm
    sudo apt install sqlite

**Windows 10**

1.  To install _Node.js_, **browse** to [https://nodejs.org/en/download/](https://nodejs.org/en/download/).
2.  **Download** the latest Windows Installer.
3.  **Run the installer** and follow the prompts.  
4.  To install _SQLite_, **browse** to [https://www.sqlite.org/download.html](https://www.sqlite.org/download.html).
5.  Under _Precompiled Binaries for Windows_, **download** the .zip that starts with “[sqlite-tools-win32-x86](https://www.sqlite.org/2020/sqlite-tools-win32-x86-3310100.zip)...”
     **NOTE:** Windows users will need to have installed a compression/unpacking application such as [7-zip](https://www.7-zip.org/download.html), [WinZip](https://www.winzip.com/landing/download-winzip-v1.html?gclid=EAIaIQobChMIrcqqxM-B6QIVCxgMCh29kQ57EAAYASAAEgLg-PD_BwE), etc.
6.  **Extract** the .zip file to the desired path.
7.  **Edit the Windows PATH variable** to include the extracted folder that contains the sqlite3 executable.

**Mac OS**

1.  To install _Node.js_, **browse** to [https://nodejs.org/en/download/](https://nodejs.org/en/download/).
2.  **Download** the latest MacOS Installer (.pkg).
3. **Run** the installer (and **fail**).
4.  Click **Settings→Security and Privacy→General**.
5.  Under _Allow Apps_ find the *node...pkg* file and select **Open Anyway**.
6.  Select **Open**.
7.  To install _SQLite_, **browse** to [https://www.sqlite.org/download.html](https://www.sqlite.org/download.html).
8.  **Download** *sqlite-autoconf-[version number].tar.gz*.
9.  **Open a terminal** to the location of the *sqlite-autoconf* file.
10.  **Run the following commands**, replacing _[version number]_ with your SQLite version:

         tar zxvf sqlite-autoconf-[version number].tar.gz
         cd sqlite-autoconf-[version number]
         ./configure --prefix=/usr/local
         make
         make install

### Installing and Deploying the PREFIDO2 Web Application
1.  **If using a single computer for testing the client and server portions of this tutorial on COMBINED, modify** */etc/hosts* or *C:\Windows\System32\drivers\etc\hosts* file (depending on whether you are using Linux/OS-X or Windows) to include *fido2tutorial.strongkey.com* as an alias for _localhost_ (the entry with 127.0.0.1).

	If you plan to test the tutorial web application with a browser from a different client computer (APPCLIENT) while running the server component of the tutorial web application on APPSERVER, then identify the IP address of your APPSERVER and add the *fido2tutorial.strongkey.com* alias to APPSERVER’s IP address within the _hosts_ file _on the APPCLIENT_:

	- **CentOS/Ubuntu/Mac**

          sudo vi /etc/hosts

	- **Windows**

	    1.  **Run Notepad as** _**administrator**_.
	    2.  In _Notepad_ click **File→Open...** and **edit** *c:\Windows\System32\Drivers\etc\hosts*.
	    3.  **Add** _fido2tutorial.strongkey.com_ after localhost (uncomment the localhost line if necessary).
	    4.  **Save** the file.
	    5.  **Close Notepad**.

2.  **Ping** _fido2tutorial.strongkey.com_.

	**NOTE: If you have a firewall on the APPSERVER/COMBINED, add a rule to open port 3001 so network connections can reach the web application**.

    - **CentOS** (*If you are using Ubuntu use* apt *instead of* yum.)

           sudo yum install firewalld
           sudo firewall-cmd --zone=public --add-port=3001/tcp --permanent
           sudo firewall-cmd --complete-reload

3.  **Deploy** the project.

    a.  **Download** the _prefido2_ source code. **If you are using Ubuntu use _apt_ instead of _yum_**.

        sudo yum install wget
        wget https://github.com/StrongKey/fido2/tree/master/tutorial/node/prefido2.tgz

       **If using Windows, download to the following file:**

        https://github.com/StrongKey/fido2/tree/master/tutorial/node/prefido2.tgz

    b.  **Unzip** the _StrongKey FIDO2 Tutorial_:

        tar zxvf prefido2.tgz

    c. **Change directory** into prefido2:

        cd prefido2/

    d. **Install** the required node modules.

        npm install

    e. **Install pm2**. This is the process manager we will use to run the application:

        sudo npm install pm2@latest -g

    f. **For Windows only:** install _node-gyp_ and _sqlite3_ manually.

        npm install -g node-gyp
        npm install sqlite3

    g. **Start** the project.

        pm2 start main.js

    h. **Take a snapshot** of your currently running _Node_ applications; this allows _pm2_ to restart your application automatically upon restart of _pm2_. **Ignore this step on Windows/Mac OS**.

        sudo pm2 startup systemd

    i. **Take a snapshot** of your currently running Node applications which allows _pm2_ to restart your application automatically upon restart of _pm2._

        pm2 save

    j. **Open a browser** to [https://fido2tutorial.strongkey.co](https://fido2tutorial.strongkey.com:3001/)[m](https://fido2tutorial.strongkey.com:3001/)[:3001](https://fido2tutorial.strongkey.com:3001/). When prompted, **add an exception** for a self-signed certificate. Messages will vary by browser.

	   **NOTE:**  The default certificate is a 1-year certificate.

4.  The home page displays. Register a new user by clicking **Sign Up**. **Enter the required registration information** and click **Sign Up**.

5.  **Login** using the credentials you just registered. The sample application _Quote Boat_ displays.

6.  Do whatever floats your boat, then **Logout**.

### FIDO2-enabling the PREFIDO2 Web Application

1.  **Copy** the PREFIDO2 web application folder (_prefido2_) and **rename** the copy to _postfido2_.

		cp -r prefido2 postfido2

2.  **Delete old users** from the database.

		sqlite3 postfido2/db/aftdb.db
		delete from users;
		.exit

3.  **Open** *postfido2/templates/register.html* in your preferred text editor.

    -  **Copy** this snippet of code (between the AAAAA... lines, but NOT including the AAAAA…. lines) and **paste** it between the AAAAA lines in _register.html_, replacing the existing content in the HTML file.

		Here we are removing the _action_ and _method_ attributes of the form to remove the old registration post request. This will be replaced by a call to a function in *functions.js*.

		   <!-- AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA -->
		   <form  id="regform" >
		   <!-- AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA -->

	-  **Copy** this snippet of code (between the BBBBB... lines, but NOT including the BBBBB…. lines) and **paste** between the BBBBB lines in _register.html_, replacing the existing content in the HTML file.

		Here we are removing the _passcontainer_ and _password_ input and replacing it with _id displayname_ input, which is used to identify the name of the FIDO2 Token used in registration.

    	   <!-- BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB -->
		   <input class="input-out" type="text" id="displayname" name="displayname" placeholder="Display Name"><br>
		   <!-- BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB -->

	-  **Copy** this snippet of code (between the CCCCC... lines) and **paste** between the CCCCC lines in _register.html_, replacing the existing content in the HTML file.


		Here we remove the type, form, and value from the button and add _onclick="submitForm('registration')"_. This will call the function that will be added to _functions.js_.

		   <!-- CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC -->
		   <button id="regbutton" onclick="submitForm('registration')">Sign Up</button>
		   <!-- CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC -->

	-  **Copy** this snippet of code (between the DDDDD... lines) and **paste** between the DDDDD lines in _register.html_.

           <!-- DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD -->
		   <script type="text/javascript" src="/js/common.js"></script>
		   <!-- DDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD -->

4.  **Open** _postfido2/templates/login.html_ in your preferred text editor.


	-  **Copy** this snippet of code (between the EEEEE... lines) and **paste** between the EEEEE lines in _login.html_, replacing the existing content in the HTML file.


		Here we remove the action and method aspects of the form to remove the old login post request. This will be replaced by a call to a function in _functions.js_.

		   <!-- EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE -->
		   <form id="loginform">
		   <!-- EEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE -->

	-  **Delete** the snippet of code (between the FFFFF... lines). Here we delete the _passcontainer_ and _password_ input. They are no longer needed thanks to FIDO2!

           <!-- FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF -->

           <!-- FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF -->

	-  **Copy** this snippet of code (between the GGGGG... lines) and **paste** between the GGGGG lines in _login.html_, replacing the existing content in the HTML file.


		Here we remove the type, form, and value attributes from the button and add onclick="submitForm('authentication')". This will call the function that will be added to to _functions.js_.

		   <!-- GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG -->
		   <button onclick="submitForm('authentication')">Sign In</button>
		   <!-- GGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG -->

	-  **Copy** this snippet of code (between the HHHHH... lines) and **paste** between the HHHHH lines in _register.html_.


		   <!-- HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH -->
		   <script type="text/javascript" src="/js/common.js"></script>
		   <!-- HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH -->

5.  **Open** _postfido2/templates/js/functions.js_ in your preferred text editor.

	-  **Copy** this snippet of code (between the IIII... lines) and **paste** between the IIII lines in _functions.js_.


		Here we add functions to the APPCLIENT that are used to request challenges from the APPSERVER, pass the challenges to the FIDO2 Token, and submit challenge results to the APPSERVER.

                  //IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
                   function submitForm(intent){
                     if(intent=="registration"){
                     $.post('/getChallenge', {
                         'intent' : intent,
                         'username': $('#regusername').val(),
                         'displayname': $('#displayname').val(),
                         'firstname': $('#firstname').val(),
                         'lastname': $('#lastname').val()
                     }).done(resp => {
                      if(resp.Response == "sqlite-error"){
                        console.log(resp.Response);
                        location.reload();
                      }else if(resp.Response == "skfs-error"){
                        console.log(resp.Response);
                      } else {
                        document.getElementById("failed").style.display = "none";
                        document.getElementById("failedbreak").style.display = "none";
                        callFIDO2Token(intent,resp.Response);
                      }

                         }).fail((jqXHR, textStatus, errorThrown) => {
                      alert(jqXHR, textStatus, errorThrown);
                    });

                   } else if(intent=="authentication")
                    $.post('/getChallenge', {
                      'intent' : intent,
                        'username': $('#username').val()
                    })
                        .done((resp) => {
                      if(!resp.Response.toString().toLowerCase().includes("error")){
                        callFIDO2Token(intent,resp.Response);
                      } else {
                        alert("Username not registered");
                      }

                        })
                        .fail((jqXHR, textStatus, errorThrown) => {
                      alert(jqXHR, textStatus, errorThrown);
                        });

                   }

                   function callFIDO2Token(intent,challenge) {
                     let challengeBuffer = challengeToBuffer(challenge);
                     let credentialsContainer = window.navigator;
                     if(intent=="registration"){
                         credentialsContainer.credentials.create({ publicKey: challengeBuffer.Response })
                      .then(credResp => {
                      let credResponse = responseToBase64(credResp);
                      credResponse.intent = intent;
                        $.post('/submitChallengeResponse',  credResponse)
                            .done(regResponse => onResult(intent,regResponse))
                            .fail((jqXHR, textStatus, errorThrown) => {
                                console.log(jqXHR, textStatus, errorThrown);
                            });
                          })
                      .catch(error => {
                          alert(error);
                      });
                       } else if (intent=="authentication"){
                         credentialsContainer.credentials.get({ publicKey: challengeBuffer.Response })
                      .then(credResp => {
                          let credResponse = responseToBase64(credResp);
                          credResponse.intent = intent;
                          $.post('/submitChallengeResponse', credResponse)
                        .done(authResponse => onResult(intent,authResponse))
                        .fail((jqXHR, textStatus, errorThrown) => {
                            alert(jqXHR, textStatus, errorThrown);
                        });
                      })
                      .catch(error => {
                          alert(error);
                      });
                     }
                   }
                     function onResult(intent,response){
                       if(intent=="registration"){
                         if(!response.Response.toString().toLowerCase().includes("error")){
                    window.location.replace(window.location.protocol + "//" + window.location.host + "/login");
                         } else {
                    alert(response.Response);
                         }
                     } else if(intent=="authentication"){

                       if(response.Response.toString() == "{\"Response\":\"\"}"){
                         window.location.replace(window.location.protocol + "//" + window.location.host + "/dashboard");
                       } else {
                         alert(response.Response);
                       }
                     }
                     }
                   //IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII


6.  **Create the file** _postfido2/templates/js/common.js_.

	-  **Copy** this snippet of code (between the JJJJ... lines) and **paste** between the JJJJ lines in _postfido2/templates/js/common.js_. This code is used for encoding and decoding data to and from the FIDO2 Token.

			//JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ
			function challengeToBuffer(input) {
			    input = JSON.parse(input);
			    input.Response.challenge = decode(input.Response.challenge);
			    if(typeof input.Response.user !== 'undefined'){
				  input.Response.user.id = decode(input.Response.user.id);
			    }


		    if (input.Response.excludeCredentials) {
			for (let i = 0; i < input.Response.excludeCredentials.length; i++) {
			    input.Response.excludeCredentials[i].id = input.Response.excludeCredentials[i].id.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
			    input.Response.excludeCredentials[i].id = decode(input.Response.excludeCredentials[i].id);
			}
		    }
		    if (input.Response.allowCredentials) {
			for (let i = 0; i < input.Response.allowCredentials.length; i++) {
			    input.Response.allowCredentials[i].id = input.Response.allowCredentials[i].id.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
			    input.Response.allowCredentials[i].id = decode(input.Response.allowCredentials[i].id);
			}
		    }
		    return input;

			}

			function responseToBase64(input) {

		    let copyOfDataResponse = {};
		    copyOfDataResponse.id = input.id;
		    copyOfDataResponse.rawId = encode(input.rawId);
		    if(typeof input.response.attestationObject !== 'undefined'){
			  copyOfDataResponse.attestationObject = encode(input.response.attestationObject);
		    }
		    if(typeof input.response.authenticatorData !== 'undefined'){
			  copyOfDataResponse.authenticatorData = encode(input.response.authenticatorData);
			  copyOfDataResponse.signature = encode(input.response.signature);
			  copyOfDataResponse.userHandle = encode(input.response.userHandle);
			}
		    copyOfDataResponse.clientDataJSON = encode(input.response.clientDataJSON);
		    copyOfDataResponse.type = input.type;
		    return copyOfDataResponse;
		    }

			let chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_';
				let lookup = new Uint8Array(256);
				for (let i = 0; i < chars.length; i++) {
					lookup[chars.charCodeAt(i)] = i;
			}
			let encode = function (arraybuffer) {
				let bytes = new Uint8Array(arraybuffer),
					i, len = bytes.length, base64url = '';
				for (i = 0; i < len; i += 3) {
					base64url += chars[bytes[i] >> 2];
					base64url += chars[((bytes[i] & 3) << 4) | (bytes[i + 1] >> 4)];
					base64url += chars[((bytes[i + 1] & 15) << 2) | (bytes[i + 2] >> 6)];
					base64url += chars[bytes[i + 2] & 63];
				}
				if ((len % 3) === 2) {
					base64url = base64url.substring(0, base64url.length - 1);
				} else if (len % 3 === 1) {
					base64url = base64url.substring(0, base64url.length - 2);
				}
				return base64url;
			};
			let decode = function (base64string) {
				let bufferLength = base64string.length * 0.75,
					len = base64string.length, i, p = 0,
					encoded1, encoded2, encoded3, encoded4;
				let bytes = new Uint8Array(bufferLength);
				for (i = 0; i < len; i += 4) {
					encoded1 = lookup[base64string.charCodeAt(i)];
					encoded2 = lookup[base64string.charCodeAt(i + 1)];
					encoded3 = lookup[base64string.charCodeAt(i + 2)];
					encoded4 = lookup[base64string.charCodeAt(i + 3)];
					bytes[p++] = (encoded1 << 2) | (encoded2 >> 4);
					bytes[p++] = ((encoded2 & 15) << 4) | (encoded3 >> 2);
					bytes[p++] = ((encoded3 & 3) << 6) | (encoded4 & 63);
				}
				return bytes.buffer
			};
			//JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ


7. **Create the file** _postfido2/constants.js_.

	-  **Copy** this snippet of code (between the KKKK... lines) and **paste** between the KKKK lines in postfido2/constants.js. **The current values of SKFS_HOSTNAME, SVCUSERNAME, and SVCPASSWORD default to pointing to a FIDO2SERVER hosted by StrongKey.** If you are using your own SKFS, **replace** the example values for _skfs_ with the _hostname_ of the FIDO2SERVER and the _svcusername_ and _svcpassword_ with the _username_  and  _password_  used to access the FIDO2SERVER.

			//KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK
			const DID = 1;
			const PROTOCOL = "FIDO2_0";
			const AUTHTYPE = "PASSWORD";
			const SVCUSERNAME = "svcfidouser";
			const SVCPASSWORD = "Abcd1234!";

			exports.SKFS_HOSTNAME = "demo4.strongkey.com";
			exports.SKFS_PORT="8181";
			exports.SVCINFO = {
				 did: DID,
				 protocol: PROTOCOL,
				 authtype: AUTHTYPE,
				 svcusername: SVCUSERNAME,
				 svcpassword: SVCPASSWORD
			       };



			exports.SKFS_PREAUTHENTICATE_PATH = '/skfs/rest/preauthenticate'
			exports.SKFS_AUTHENTICATE_PATH = '/skfs/rest/authenticate'
			exports.SKFS_PREREGISTRATION_PATH = '/skfs/rest/preregister'
			exports.SKFS_REGISTRATION_PATH = '/skfs/rest/register'
			exports.SKFS_GET_KEYS_INFO_PATH = '/skfs/rest/getkeysinfo'
			exports.SKFS_DEREGISTER_PATH = '/skfs/rest/deregister'

			exports.METADATA_VERSION = "1.0"
			exports.METADATA_LOCATION = "Cupertino, CA"
			//KKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKKK


8.  **Open** _postfido2/routes.js_ in your preferred text editor.

	-  **Copy** this snippet of code (between the LLLL... lines) and **paste** between the LLLL lines in _routes.js_. Here we add include the HTTPS module and the constants file that will be used to call the FIDO2SERVER.


			//LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL
			const https = require('https');
			const CONSTANTS = require('./constants');

			//LLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLLL

	-  **Copy** this snippet of code (between the MMMM... lines) and **paste** between the MMMM lines in _routes.js_, replacing the existing content in the JavaScript file. Replace the _/loginSubmit_ post listener _/registerSubmit_ with _/getChallenge_ and _/submitChallengeResponse_ listeners. The _/getChallenge_ listener is used to request challenges from the FIDO2SERVER to send to the APPCLIENT for registration and authentication. The _/submitChallengeResponse_ listener is used to send the responses received from the FIDO2 Token, sent by the APPCLIENT, to the FIDO2SERVER.


            //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM
             router.post("/getChallenge", (req,res) =>{
               var intent = req.body.intent;
                 var username = req.session.username = req.body.username;
             if(intent=="authentication"){
               if(username == ""){
                 res.redirect("/login");
                 return;
               }
               var db = getDB();
               db.get(`select * from users where username = ? `,[username],
                  (err, row) => {
                  if (err) {
                    log("ERROR: "+ err.message);
                  }
                  if (row) {
              req.session.possibleuserid = row.id;
               process.env["NODE_TLS_REJECT_UNAUTHORIZED"] = 0;
                 const data =JSON.stringify({
                   svcinfo: CONSTANTS.SVCINFO,
                   payload: {
                username: username,
                options: {}
                   }
                 });
                 const options = {
                   hostname: CONSTANTS.SKFS_HOSTNAME,
                   port: CONSTANTS.SKFS_PORT,
                   path: CONSTANTS.SKFS_PREAUTHENTICATE_PATH,
                   method: 'POST',
                   headers: {
              'Content-Type': 'application/json'
                   }
                 };
                 const fido2Req = https.request(options, fido2Res => {
                   log(`statusCode: ${fido2Res.statusCode}`);

                   fido2Res.on('data', d => {
              log("challengeBuffer=");
              log(d);
              res.json({Response:d.toString()});
                   })
                 });
                 fido2Req.on('error', error => {
                   log(error);
                   res.json({Response:"skfs-error"});
                 });
                 fido2Req.write(data);
                 fido2Req.end();
                   } else {
              res.json({Response:"sqlite-error"});
                   }
                 });
             } else if(intent=="registration"){
               var firstname = req.session.firstname = req.body.firstname;
               var lastname = req.session.lastname = req.body.lastname;
               var displayname = req.session.displayname= req.body.displayname;
               if(username == "" | displayname=="" | firstname == "" | lastname == ""){
                 res.redirect("/register");
                 return;
               }
               var db = getDB();
               db.get(`select * from users where username = ? `,[username],
                  (err, row) => {
                  if (err) {
                    log("ERROR: "+ err.message);
                  }
                  if (!row) {

              process.env["NODE_TLS_REJECT_UNAUTHORIZED"] = 0;
             const data = JSON.stringify({
                svcinfo: CONSTANTS.SVCINFO,
                payload: {
                   username: username,
                   displayname: displayname,
                   options: {"attestation":"direct"},
                   extensions: "{}"
                 }
             });
             const options = {
               hostname: CONSTANTS.SKFS_HOSTNAME,
               port: CONSTANTS.SKFS_PORT,
               path: CONSTANTS.SKFS_PREREGISTRATION_PATH,
               method: 'POST',
               headers: {
             'Content-Type': 'application/json'
               }
             };
             const fido2Req = https.request(options, fido2Res => {
               log(`statusCode: ${fido2Res.statusCode}`);

               fido2Res.on('data', d => {
             log("challengeBuffer=");
             log(d);
             res.json({Response:d.toString()});
               })
             });
             fido2Req.on('error', error => {
               log(error);
               res.json({Response:"skfs-error"});
             });
             fido2Req.write(data);
             fido2Req.end();
               } else {
             failedRegistration=true;
             res.json({Response:"sqlite-error"});
               }
             });
             }
             });
            router.post("/submitChallengeResponse", (req,res) =>{
                 var intent = req.body.intent;
                   var username = req.session.username;
                   var credResponse = req.body;
                   var reqOrigin = req.get('host');

               let data = "";
               let path = "";
             if(intent=="authentication"){
               var metadataJSON = {
               version: CONSTANTS.METADATA_VERSION,
               last_used_location: CONSTANTS.METADATA_LOCATION,
               username: username,
               origin: "https://"+reqOrigin
               };
               var responseJSON =   {
               id: credResponse.id,
               rawId: credResponse.rawId,
               response: {
                   authenticatorData: credResponse.authenticatorData,
                   signature: credResponse.signature,
                   userHandle: credResponse.userHandle,
                   clientDataJSON: credResponse.clientDataJSON
               },
               type: "public-key"};
             data = JSON.stringify({
               svcinfo: CONSTANTS.SVCINFO,
               payload: {
                 strongkeyMetadata: metadataJSON,
                 publicKeyCredential: responseJSON,
               }
             });
              path = CONSTANTS.SKFS_AUTHENTICATE_PATH;
             } else if(intent=="registration"){
               var firstname = req.session.firstname;
               var lastname = req.session.lastname;
               var db = getDB();
               var metadataJSON = {
               version: CONSTANTS.METADATA_VERSION,
               create_location: CONSTANTS.METADATA_LOCATION,
               username: username,
               origin: "https://"+reqOrigin
               };

             var responseJSON =   {
                 id: credResponse.id,
                 rawId: credResponse.rawId,
                 response: {
              attestationObject: credResponse.attestationObject,
              clientDataJSON: credResponse.clientDataJSON
                 },
                 type: "public-key"};

             data = JSON.stringify({
               svcinfo: CONSTANTS.SVCINFO,
                 payload: {
                  strongkeyMetadata: metadataJSON,
                  publicKeyCredential: responseJSON,
                }
              });
              path = CONSTANTS.SKFS_REGISTRATION_PATH;
             }
             const options = {
               hostname: CONSTANTS.SKFS_HOSTNAME,
               port: CONSTANTS.SKFS_PORT,
               path: path,
               method: 'POST',
               headers: {
             'Content-Type': 'application/json'
               }
             };

             const fido2Req = https.request(options, fido2Res => {
               log(`statusCode: ${fido2Res.statusCode}`);

               fido2Res.on('data', d => {
             if(d.toString().toLowerCase().includes("error")){
               res.json({Response:d.toString()});
               return;
             }
                 if(intent == "registration"){
                   db.run('insert into users(username,first_name,last_name) values(?,?,?)',[username,firstname,lastname], function(err) {
                  if (err) {log("ERROR: "+ err.message);}
                      log("user added: \nfirst name: "+firstname+"\nlast name: "+lastname+"\nusername: "+username);
                      req.session.justReg=true;
                      log(d);
                      res.json({Response:d.toString()});
                    });
                   } else if(intent == "authentication"){
              req.session.userid = req.session.possibleuserid;
              log(d);
              res.json({Response:d.toString()});
                   }
               })

             });

             fido2Req.on('error', error => {
               log(error);
               res.json({Response:"error"});
             });
             process.env["NODE_TLS_REJECT_UNAUTHORIZED"] = 0;
             fido2Req.write(data);
             fido2Req.end();
               });
             //MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM


	-  **Copy** this snippet of code (between the NNNN... lines) and **paste** between the NNNN lines in _routes.js_, replacing the existing content in the JavaScript file.


		Modify the _deleteUser_ listener by replacing the code below with two post requests to FIDO2SERVER. The first post request is to _/skfs/rest/getkeysinfo_, which is used to retrieve the user's FIDO2 Token's _keyid_. The second request is to _/skfs/rest/deregister_, which deletes the FIDO2 Token registration information. This deletes the user’s FIDO2 Token from the FIDO2SERVER’s database at the same time the user’s info is deleted from the APPSERVER database.


                  //NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN
                  var username = req.session.username;
                  req.session.userid=null;
                  log("logout user " +id +" "+username);

                   db.run(`delete from users where id = ?`, id, function(err) {
                       if (err) {
                  return console.error(err.message);
                       }
                       process.env["NODE_TLS_REJECT_UNAUTHORIZED"] = 0;
                  const data = JSON.stringify({
                    svcinfo: CONSTANTS.SVCINFO,
                    payload: {
                 username: username
                    }
                  });
                  const options = {
                    hostname: CONSTANTS.SKFS_HOSTNAME,
                    port: CONSTANTS.SKFS_PORT,
                    path: CONSTANTS.SKFS_GET_KEYS_INFO_PATH,
                    method: 'POST',
                    headers: {
                      'Content-Type': 'application/json'
                    }
                  };
                  const fido2Req = https.request(options, fido2Res => {
                    log(`statusCode: ${fido2Res.statusCode}`);

                    fido2Res.on('data', d => {
                      log("keyInfo=");
                      log(d);
                      const dataDel = JSON.stringify({
                 svcinfo: CONSTANTS.SVCINFO,
                 payload: {
                     "keyid": JSON.parse(d).Response.keys[0].randomid
                 }
                });
                      const optionsDel = {
                 hostname: CONSTANTS.SKFS_HOSTNAME,
                 port: CONSTANTS.SKFS_PORT,
                 path: CONSTANTS.SKFS_DEREGISTER_PATH,
                 method: 'POST',
                 headers: {
                   'Content-Type': 'application/json'
                 }
                      };
                      const fido2ReqDel = https.request(optionsDel, fido2ResDel => {
                 log(`statusCode: ${fido2ResDel.statusCode}`);

                 fido2ResDel.on('data', dDel => {
                   log(dDel);
                   log("deleted user " +id);
                   req.session.justUserDeleted = true;
                   res.redirect("/login");
                 })
                      });
                      fido2ReqDel.on('error', errorDel => {
                 log(errorDel);
                 res.json({Response:"skfs-error"});
                      });
                      fido2ReqDel.write(dataDel);
                      fido2ReqDel.end();
                    })
                  });
                  fido2Req.on('error', error => {
                    log(error);
                    res.json({Response:"skfs-error"});
                  });
                  fido2Req.write(data);
                  fido2Req.end();
                   });
                //NNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNN

9.  **Deploy** the POSTFIDO2 web application. **Open a terminal** and navigate to the _/postfido2_ directory. **Run** the following commands:

    -  CentOS/Ubuntu.

	        pm2 delete main

	        pm2 start main.js

	        sudo pm2 startup systemd

	        pm2 save

	        tail -f log

    -  Windows/Mac.

	        pm2 delete main

	        pm2 start main.js

	        pm2 save
    - Run this command if the website fails to start:

                node main.js

  10.  **Browse** to [https://fido2tutorial.strongkey.com:3001](https://fido2tutorial.strongkey.com:3001/).
