/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
'use strict';

// ECMAScript 6 class
class FidoTutorial {
    constructor() {
        this.server = '/basicserver/fido2/';
        this.initErrMap();
    }

    initComponents() {
        this.initActions();

        this.debugPanelVisible = false;
        this.fidoData = null;
        this.clearModel();

        this.queryLoggedIn();
    }

    // wire up the widgets
    initActions() {
        $('#regSubmit').click(() => {
            this.submitRegForm();
        });

        $('#authSubmit').click(() => {
            this.submitAuthForm();
        });

        $('#regUsername,#regDisplayName').keypress((e) => {
            if (e && e.keyCode == 13) {
                this.submitRegForm();
            }
        });

        $('#authUsername').keypress((e) => {
            if (e && e.keyCode == 13) {
                this.submitAuthForm();
            }
        });

        $('#logout').click(() => {
            this.post('logout', null)
                .done(resp => {
                    if (resp.Error != 'true') {
                        this.fidoData = null;
                        this.clearModel();
                        this.queryLoggedIn();
                    }
                    else {
                        onError(resp.Message);
                    }
                })
                .fail((jqXHR, textStatus, errorThrown) => {
                    this.onFailError(jqXHR, textStatus, errorThrown);
                });
        });

        $('#showRegPanel').click(() => {
            this.registering = true;
            this.updateView();
        });

        $('#showAuthPanel').click(() => {
            this.registering = false;
            this.updateView();
        });

        $('#fidoCheckbox').click(() => {
            this.debugPanelVisible = !this.debugPanelVisible;
            this.updateView();
        });
    }

    // clear view model and update UI
    clearModel() {
        // using MVVM style model
        this.username = null;
        this.displayName = null;
        this.loggedIn = false;
        this.registering = false;

        this.updateView();
    }

    // refresh widgets from view model
    updateView() {
        // default to authentication panel when not logged in
        // if user wants to register or is logged in, show registration panel
        // use logout to return to auth panel
        let showRegPanel = (this.loggedIn || this.registering);
        this.setVisible('#authPanel', !showRegPanel);
        this.setVisible('#regPanel', showRegPanel);

        // dim cipher the octopus when user isn't logged in
        if (this.loggedIn) {
            $('.login_logo').removeClass('dimmed');
        }
        else {
            $('.login_logo').addClass('dimmed');
        }

        // show username under logo when logged in
        $('#loginUsername').text(this.elide(this.username, 12) || '');
        $('.login_logo').css('text-align', this.loggedIn ? 'center' : 'end');
        $('.login_logo img').css('width', this.loggedIn ? '42px' : '58px');

        // populate reg & auth form fields
        $('#regUsername').val(this.username || '');
        $('#regDisplayName').val(this.displayName || '');
        $('#authUsername').val(this.username || '');

        // reg panel shows only the display name when logged in (this one needs display:none so we use setVisible helper)
        $('#regSubmit').text(this.loggedIn ? 'Register Additional Key' : 'Register New Key');
        this.setVisible('#regUsernamePanel', !this.loggedIn);

        // show/hide the various link actions
        this.setVisible('#logoutLinkPanel', this.loggedIn);
        this.setVisible('#registerLinkPanel', !this.loggedIn && !this.registering);
        this.setVisible('#authLinkPanel', !this.loggedIn && this.registering);

        $('#metadataContent').css('visibility', this.debugPanelVisible ? 'visible' : 'hidden');
        $('#metadataContent').empty().append(this.fidoData || '');
    }

    // update session status in model and update UI
    queryLoggedIn() {
        this.post('isLoggedIn', null)
            .done(resp => {
                this.username = resp.Response;
                this.loggedIn = !!this.username;
                this.updateView();
            })
            .fail((jqXHR, textStatus, errorThrown) => {
                this.onFailError(jqXHR, textStatus, errorThrown);
            });
    }

    // submit registration fields to server using preregister/register REST calls
    submitRegForm() {
        this.fidoData = null;

        // When not logged in, use preregister to check existence of username. The preregister call
        // doesn't require authentication. It returns a challenge, which contains a cryptographic
        // nonce, i.e. a random number used only once. If username doesn't exist, proceed with registration.
        if (!this.loggedIn) {
            this.post('preregister', {
                'username': $('#regUsername').val(),
                'displayName': $('#regDisplayName').val()
            })
                .done(resp => {
                    this.register(resp.Response);
                })
                .fail((jqXHR, textStatus, errorThrown) => {
                    this.onFailError(jqXHR, textStatus, errorThrown);
                });
        }
        // When logged in, use preregisterExisting to add additional keys associated with the username.
        // It requires authentication since we must ensure that the person adding the key is authorized
        // to do so. It returns the same fields as preregister.
        else {
            this.post('preregisterExisting', {
                'displayName': $('#regDisplayName').val()
            })
                .done(resp => {
                    this.register(resp.Response);
                })
                .fail((jqXHR, textStatus, errorThrown) => {
                    this.onFailError(jqXHR, textStatus, errorThrown);
                });
        }
    }

    // submit authentication fields to server using preauthenticate/authenticate REST calls
    submitAuthForm() {
        this.fidoData = null;

        // When not logged in, use preauthenticate to check existence of username. Unsurprisingly, the
        // preauthenticate call doesn't require authentication. It returns a challenge that is supplied
        // to the authenticator and then, assuming it is properly signed, to the authenticate REST endpoint.
        // If the username doesn't exist, the preauthenticate call will fail.
        this.post('preauthenticate', {
            'username': $('#authUsername').val()
        })
            .done((resp) => {
                this.authenticate(resp.Response);
            })
            .fail((jqXHR, textStatus, errorThrown) => {
                this.onFailError(jqXHR, textStatus, errorThrown);
            });
    }

    // interpret fido credentials response and display in metadata panel
    displayFIDOData(credResponse) {
        this.fidoData = '';

        // try/catch this so nothing breaks on a failed decode
        try {
            // display fido data if decode functions are available
            if (typeof TextDecoder !== "undefined") {
                let clientData = base64url.decode(credResponse.response.clientDataJSON);
                let textDecoder = new TextDecoder('utf-8');
                let clientData2 = textDecoder.decode(clientData);
                clientData2 = JSON.parse(clientData2);
                this.fidoData += '<p style="margin-top: 0"><span class="fido-data-header">Client Data</span></p>';
                this.fidoData += '<ul>';
                for (let key of Object.keys(clientData2)) {
                    this.fidoData += '<li><span class="fido-data-key">' + key + ':</span> <span class="fido-data-value">' + clientData2[key] + '</span></li>';
                }
                this.fidoData += '</ul>';
            }

            if (CBOR) {
                if (credResponse.response.attestationObject) {
                    let attest = base64url.decode(credResponse.response.attestationObject);
                    let attest2 = CBOR.decode(attest);
                    this.fidoData += '<p><span class="fido-data-header">Attestation</span></p>';
                    this.fidoData += '<ul>';
                    for (let key of Object.keys(attest2)) {
                        this.fidoData += '<li><span class="fido-data-key">' + key + ':</span> <span class="fido-data-value">';
                        switch (key) {
                            case 'authData':
                                this.fidoData += base64js.fromByteArray(attest2[key]);
                                break;

                            case 'attStmt':
                                if (Buffer) {
                                    this.fidoData += base64js.fromByteArray(new Buffer(JSON.stringify(attest2[key])));
                                }
                                else {
                                    this.fidoData += attest2[key];
                                }
                                break;

                            default:
                                this.fidoData += attest2[key];
                                break;
                        }
                        this.fidoData += '</span></li>';
                    }
                    this.fidoData += '</ul>';
                }
            }
        }
        catch (err) {
            window.console && console.log(err.message);
        }
    }

    // The preregister call returns a challenge to be signed by a FIDO2 key to prove ownership of the key.
    // The challenge contains metadata that tells the browser response preferences, as well as the types
    // of keys preferred/supported by the FIDO2 server. For example, the server can state a preference for
    // user verification, key attestation, or resident key (where the username and secret key are stored
    // in memory on the FIDO2 key).
    register(preregResponse) {
        let that = this;

        // Convert base64url fields to ArrayBuffer format [verify].
        let challengeBuffer = this.preregToBuffer(preregResponse);

        // Browser passes challenge fields to WebAuthn API, which tells relevant FIDO2 authenticators
        // to generate a new set of public key credentials.
        let credentialsContainer = window.navigator;
        credentialsContainer.credentials.create({ publicKey: challengeBuffer.Response })
            .then(credResp => {
                // convert response to base64url
                let credResponse = this.preregResponseToBase64(credResp);

                // update debugging panel
                this.displayFIDOData(credResponse);

                // If authenticator returns signed response, pass to FIDO2 server using register call.
                if (!this.loggedIn) {
                    // Use register when not logged in. The server verifies that a user with this username
                    // has not been created.
                    this.post('register', credResponse)
                        .done(regResponse => that.onRegResult(regResponse))
                        .fail((jqXHR, textStatus, errorThrown) => {
                            this.onFailError(jqXHR, textStatus, errorThrown);
                        });
                }
                else {
                    // Use registerExisting when logged in. The server ensures that the user is still
                    // authenticated when registering the key.
                    this.post('registerExisting', credResponse)
                        .done(regResponse => that.onRegResult(regResponse))
                        .fail((jqXHR, textStatus, errorThrown) => {
                            this.onFailError(jqXHR, textStatus, errorThrown);
                        });
                }
            })
            .catch(error => {
                that.onError(error);
            });
    }

    // The authenticate call returns a challenge to be signed by a FIDO2 key to prove ownership of the key.
    // The challenge contains metadata that tells the browser response preferences such as whether user
    // verification is required.
    authenticate(preauthResponse) {
        let that = this;
        if (preauthResponse.serviceErr != null) {
            this.onError(preauthResponse.serviceErr.Message);
        }
        else {
            // Convert base64url fields to ArrayBuffer format.
            let challengeBuffer = this.preauthToBuffer(preauthResponse);

            // Browser passes challenge fields to WebAuthn API, which asks FIDO2 authenticators
            // to sign the challenge if they own a key pair associated with that user's account.
            let credentialsContainer;
            credentialsContainer = window.navigator;
            credentialsContainer.credentials.get({ publicKey: challengeBuffer.Response })
                .then(credResp => {
                    // convert response to base64url
                    let credResponse = that.preauthResponseToBase64(credResp);

                    // update debugging panel
                    this.displayFIDOData(credResponse);

                    // If authenticator returns signed response, pass to FIDO2 server using authenticate call.
                    this.post('authenticate', credResponse)
                        .done(authResponse => that.onAuthResult(authResponse))
                        .fail((jqXHR, textStatus, errorThrown) => {
                            this.onFailError(jqXHR, textStatus, errorThrown);
                        });
                })
                .catch(error => {
                    this.onError(error);
                });
        }
    }

    // conversions between base64url and ArrayBuffer as specified by the WebAuthn API

    preregToBuffer(input) {
        input = JSON.parse(input);
        input.Response.challenge = base64url.decode(input.Response.challenge);
        input.Response.user.id = base64url.decode(input.Response.user.id);

        if (input.Response.excludeCredentials) {
            for (let i = 0; i < input.Response.excludeCredentials.length; i++) {
                input.Response.excludeCredentials[i].id = input.Response.excludeCredentials[i].id.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
                input.Response.excludeCredentials[i].id = base64url.decode(input.Response.excludeCredentials[i].id);
            }
        }
        return input;
    }

    preauthToBuffer(input) {
        input = JSON.parse(input);
        input.Response.challenge = base64url.decode(input.Response.challenge);

        if (input.Response.allowCredentials) {
            for (let i = 0; i < input.Response.allowCredentials.length; i++) {
                input.Response.allowCredentials[i].id = input.Response.allowCredentials[i].id.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
                input.Response.allowCredentials[i].id = base64url.decode(input.Response.allowCredentials[i].id);
            }
        }
        return input;
    }

    preregResponseToBase64(input) {
        let copyOfDataResponse = {};
        copyOfDataResponse.id = input.id;
        copyOfDataResponse.rawId = base64url.encode(input.rawId);
        copyOfDataResponse.response = {};
        copyOfDataResponse.response.attestationObject = base64url.encode(input.response.attestationObject);
        copyOfDataResponse.response.clientDataJSON = base64url.encode(input.response.clientDataJSON);
        copyOfDataResponse.type = input.type;
        return copyOfDataResponse;
    }

    preauthResponseToBase64(input) {
        let copyOfDataResponse = {};
        copyOfDataResponse.id = input.id;
        copyOfDataResponse.rawId = base64url.encode(input.rawId);
        copyOfDataResponse.response = {};
        copyOfDataResponse.response.authenticatorData = base64url.encode(input.response.authenticatorData);
        copyOfDataResponse.response.signature = base64url.encode(input.response.signature);
        copyOfDataResponse.response.userHandle = base64url.encode(input.response.userHandle);
        copyOfDataResponse.response.clientDataJSON = base64url.encode(input.response.clientDataJSON);
        copyOfDataResponse.type = input.type;
        return copyOfDataResponse;
    }

    onRegResult(regResponse) {
        let responseJSON = JSON.parse(JSON.stringify(regResponse));
        if (responseJSON.Response === 'Successfully processed registration response') {
            this.clearModel();
            this.queryLoggedIn();
        }
    }

    onAuthResult(authResponse) {
        let response = authResponse.Response;
        this.clearModel();
        this.queryLoggedIn();
    }

    onFailError(jqXHR, textStatus, errorThrown) {
        var msg;

        if (jqXHR) {
            msg = this.errMsgMap[jqXHR.status];

            if (!msg && jqXHR.responseJSON && jqXHR.responseJSON.Message) {
                msg = jqXHR.responseJSON.Message;
            }
        }

        if (!msg) {
            if (textStatus) {
                msg = textStatus;
            }
            else if (errorThrown) {
                msg = errorThrown;
            }
            else {
                msg = 'An unknown error was encountered';
            }
        }

        alert(msg);
    }

    onResponseError(errMsg, response) {
        var msg;

        if (errMsg) {
            // if (errMsg.includes('ERR')) {
            //     this.onError(errMsg.split(':')[1]);
            // }
            msg = errMsg;
        }
        else {
            msg = 'An unknown error was encountered'
        }

        alert(msg);
    }

    onError(errMsg) {
        this.onResponseError(errMsg);
    }

    // error messages for common HTTP status codes
    initErrMap() {
        this.errMsgMap = {
            0: 'Network error/server not available',
            401: 'Authentication error',
            404: 'An account with this username does not exist',
            409: 'Username has already been registered'
        };
    }

    // REST post helper
    post(endpoint, data) {
        return $.ajax({
            url: this.server + endpoint,
            type: 'post',
            data: data ? JSON.stringify(data) : null,
            contentType: 'application/json; charset=UTF-8',
            dataType: 'json'
        });
    }

    // trim string and add ellipsis
    elide(s, len) {
        if (!s) return "";

        if (s.length > len) {
            return s.substr(0, len) + "...";
        }
        return s;
    }

    // hides child elements
    setVisible(element, value) {
        $(element).css('visibility', value ? 'visible' : 'hidden');
        // to hide child elements
        $(element).css('display', value ? 'block' : 'none');
    }
}

// hide loading graphic, init application
if (supportedBrowser) {
    let app = new FidoTutorial();

    $(setTimeout(() => {
        $('.loading-spinner').hide();
        $('#main').show();

        app.initComponents();
    }, 1));
}
