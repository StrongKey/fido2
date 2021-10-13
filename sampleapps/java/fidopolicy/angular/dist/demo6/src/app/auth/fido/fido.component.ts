import { Component, OnInit, ViewEncapsulation, ChangeDetectorRef, NgZone, AfterViewInit } from "@angular/core";
import { Router, ActivatedRoute } from "@angular/router";
import { Helpers } from "../../helpers";
import { RestService } from '../../_services/rest.service';
import { SharedService } from '../../_services/shared.service';
import { ScriptLoaderService } from '../../_services/script-loader.service';
import Swal from 'sweetalert2';
import { User } from '../_models/user';
declare var base64url: any;

@Component({
    selector: '.m-grid.m-grid--hor.m-grid--root.m-page',
    templateUrl: './fido.component.html',
    encapsulation: ViewEncapsulation.None,
    styles: [`
      .m_login__footer{
        margin-top : 10% !important;
      }

      .m-login__container {
        width: 500px; height: 600px;
        margin-top: 50px;
        margin-left: auto;
        margin-right: auto;
        background: rgb(255, 255, 255);
        padding: 30px;
        -webkit-box-shadow: 0 1px 2px rgba(0,0,0,0.15);
        -moz-box-shadow: 0 1px 2px rgba(0,0,0,0.15);
        box-shadow: 0 1px 2px rgba(0,0,0,0.15);
        -webkit-border-radius: 5px;
        -moz-border-radius: 5px;
        -ms-border-radius: 5px;
        -o-border-radius: 5px;
        border-radius: 5px;
      }
      .login-card {
        box-shadow: 0 8px 16px 0 rgba(0,0,0,0.2);
        transition: 0.3s;
      }

  `]
})

export class FIDOComponent implements OnInit, AfterViewInit {

    username: string;
    isLoggedInResponse: string;
    errorMessage = "";
    hasError: boolean;
    isRegister: boolean;
    status: string;
    swalWithBootstrapButtons = Swal.mixin({
        customClass: {
          confirmButton: 'btn btn-success',
          cancelButton: 'btn btn-danger',
        },
        buttonsStyling: false,
    });


    constructor(private _router: Router,
        private _activatedRoute: ActivatedRoute,
        private ref: ChangeDetectorRef,
        private zone: NgZone,
        private _restService: RestService,
        private _sharedService: SharedService,
        private _script: ScriptLoaderService, ) {
    }

    ngOnInit(): void {
        console.log("calling fido component");
        this._script.loadScripts('body', [
            'assets/vendors/base/vendors.bundle.js',
            'assets/demo/demo6/base/scripts.bundle.js'], true).then(() => {
                Helpers.setLoading(false);
                this.handleFidoCancel();
            });
        this._activatedRoute.params.subscribe(params => {
            let action = params["action"];
            this.status = params["status"];
            // Determine if we are registering or authenticating
            if (action === "register") {
                console.log("register");
                this.isRegister = true;
                let preregObject = this._sharedService.getPreregObject();
                if (preregObject) this.fidoRegister(JSON.parse(preregObject));
            } else if (action === "authenticate") {
                this.isRegister = false;
                this.fidoPreauth();
            }
        });
        
    }

    ngAfterViewInit() {
        this.ref.detectChanges();
    }

    onError(error: string) {
        console.log("onError fido component == " + error);
        this.hasError = true;
        this.errorMessage = error;
        Swal.fire({
            position: 'top',
            icon: 'error',
            // title: 'Error...',
            html: '<br/>' + this.errorMessage + '<br/>',
            showCancelButton: true,
            confirmButtonText: 'Yes, try again!',
            cancelButtonText: 'No, cancel!',
            reverseButtons: true
        }).then((result) => {
            console.log("retry result = " + JSON.stringify(result));
            if (result.value) {
                console.log("if retry");
                this.onRetry();
            } else if (
                result.dismiss === Swal.DismissReason.cancel
            ) {
                console.log("else retry");
                this.fidoCancel();
            }
        });
    }


    fidoPrereg() {
    }

    fidoRegister(preregResponse: any) {
        console.log("fido preegister");
        console.log("preregResponse - " + JSON.stringify(preregResponse));
        console.log("typeof preregResponse = " + typeof (preregResponse));
        let that = this;

        if (preregResponse.serviceErr != null) {
            this.onError(preregResponse.serviceErr);
        } else {
            let responseJSON = preregResponse.Response;
            console.log("responseJSON = " + JSON.stringify(responseJSON))
            let challengeBuffer = this.preregToBuffer(responseJSON);
            let credentialsContainer: any;
            credentialsContainer = window.navigator;
            console.log("Registering...");
            credentialsContainer.credentials.create({ publicKey: challengeBuffer })
                .then((resp) => {
                    let response = this.preregResponseToBase64(resp);
                    console.log("response create = " + JSON.stringify(response));
                    that.registerFidoResponse(response);
                }).catch(error => { that.onError(error) });

        }
    }

    registerNewToken(challengeBuffer: any, username: string) {
        console.log("registerNewToken");
        let that = this;
        let credentialsContainer: any;
        credentialsContainer = window.navigator;
        credentialsContainer.credentials.create({ publicKey: challengeBuffer.Challenge })
            .then((resp) => {
                console.log("lost token");
                let response = this.preregResponseToBase64(resp);
                console.log("response");
                console.log("username  == " + that.username);
                // that.registerFidoResponseForLostToken(lostTokenCID, username, response, hash);
            }).catch(error => { that.onError(error) });


    }


    registerFidoResponse(response) {
        let that = this;
        let user = this._sharedService.getUser();
        if (user.jwt === undefined) {
            this._restService.register(response, user).
                then(regResponse => that.onRegResult(regResponse, false))
                .catch(error => {
                    that.onError(error)
                });
        } else {
            this._restService.registerExisting(response, user).
                then(regResponse => that.onRegResult(regResponse, true))
                .catch(error => {
                    that.onError(error)
            });
        }
    }
    


    onRegResult(regResponse: any, userExisting: boolean) {
        let responseJSON = JSON.parse(JSON.stringify(regResponse));
        let body = responseJSON.body;
        if (body.Response === "Successfully processed registration response") {
            if (!userExisting) {
                Swal.fire({
                    position: 'top',
                    icon: 'success',
                    title: 'All done!',
                    html: '<br/>' + "Successfully Registered New User" + '<br/>',
                    confirmButtonText: 'OK'
                }).then((result) => { 
                    this.zone.run(() => this._router.navigateByUrl("/"));
                });
            }
            else {
                this.zone.run(() => this._router.navigateByUrl("/profile"));
            }
        }
        else {
            this._sharedService.setError("Registration didnt process.")
        }
    }


    registerUserResponse(regUserResponse: any) {
        let responseJSON = JSON.parse(JSON.stringify(regUserResponse));
        let body = responseJSON.Response;
        let user: User = <User>JSON.parse(body.user);

        if (responseJSON.body.toLowerCase() == "false") {
            this._sharedService.setUser(user);
            // only register user for applications if this is a demo and if the user belongs to the original company's CID
            this.zone.run(() => this._router.navigateByUrl("/"));
        }
    }


    fidoPreauth() {
        let user = this._sharedService.getUser();
        this._restService.preauthenticate(user)
            .then(resp => this.fidoAuthenticate(resp.Response))
            .catch(error => {
                this._sharedService.setError(error);
                this.zone.run(() => this._router.navigateByUrl(""));
            })
    }

    fidoAuthenticate(preauthResponse: any) {
        let that = this;
        console.log("Preauth response from server: ");
        if (preauthResponse.serviceErr != null) {
            this.onError(preauthResponse.serviceErr);
        } else {
            let response = JSON.parse(preauthResponse).Response;
            console.log(JSON.stringify(response));
            let challengeBuffer = this.preauthToBuffer(response);
            let credentialsContainer: any;
            credentialsContainer = window.navigator;
            credentialsContainer.credentials.get({ publicKey: challengeBuffer })
                .then((resp) => {
                    let response = that.preauthResponseToBase64(resp);
                    let user = this._sharedService.getUser();
                    that._restService.authenticate(response, user)
                        .then(authResponse => that.onAuthResult(authResponse));
                }).catch((error) => {
                    this.onError(error);
                });
        }
    }


    onAuthResult(authResponse: any) {
        console.log("Auth response from server: ");
        console.log("authResponse : " + JSON.stringify(authResponse));
        let responseJSON = JSON.parse(JSON.stringify(authResponse));
        console.log(responseJSON);
        let body = responseJSON.body;
        console.log(body);
        let user = this._sharedService.getUser();

        console.log("Pre:" + user);
        console.log("JWT: "+ body.jwt);
        user.jwt = body.jwt;
        console.log("Post:" + user);
        console.log("Error:" + body.Error );
        console.log("Error:" + responseJSON.Error );
        if (body.Error.toLowerCase() == "false") {
            this._sharedService.setUser(user);
            // only register user for applications if this is a demo and if the user belongs to the original company's CID
            this.zone.run(() => this._router.navigateByUrl("/dashboard"));

        } else {
            console.log("else - onAuthResultonAuthResult");
            let errorMsg = JSON.parse(authResponse.body.Message);
            let authresponse = JSON.parse(errorMsg.authresponse);
            let error = authresponse.Error;
            if (error.includes("ERR")) {
                this.onError(error.split(':')[1]);
            }
            else {
                this.onError(error);
            }
        }
    }

    handleFidoCancel() {
        document.getElementById('m_fido_cancel').addEventListener('click', (e) => {
            e.preventDefault();
            // take to loginpage
            this.fidoCancel();
        });
    }


    onRetry() {
        console.log("onRetry");
        // reset error status
        this.hasError = false;
        this.errorMessage = "";
        // try registration/authentication again
        // if the current action is registration
        if (this.isRegister) {
            if (this.status == "new") {
                let user = this._sharedService.getUser();
                this._restService.preregister(user)
                    .then(resp => {
                        if (resp.Error === 'False') {
                            console.log("in preregister");
                            this.fidoRegister(JSON.parse(resp.Response));
                        }
                    }).catch(error => {
                        this._sharedService.setError(error);
                        this._router.navigateByUrl("");
                    });
            }
            else {
                console.log("adding new fido key for existing user");
                let user = this._sharedService.getUser();
                this._restService.preregisterExisting("newkey", user)
                    .then(resp => {
                        let response = JSON.parse(JSON.stringify(resp));
                        if (response.Error === 'False') {
                            console.log("need to work on the preregisterExisting retryy")
                        }

                    });
            }

        } else {
            this.fidoPreauth();
        }
        this.ref.detectChanges();
    }

    preregToBuffer(input) {
        console.log("input = " + JSON.stringify(input));
        input.challenge = base64url.decode(input.challenge);
        input.user.id = base64url.decode(input.user.id);
        if (input.excludeCredentials) {
            for (let i = 0; i < input.excludeCredentials.length; i++) {
                input.excludeCredentials[i].id = input.excludeCredentials[i].id.replace(/\+/g, "-").replace(/\//g, "_").replace(/=/g, "");
                input.excludeCredentials[i].id = base64url.decode(input.excludeCredentials[i].id);
            }
        }
        console.log("return input = " + JSON.stringify(input));
        return input;
    }

    preregResponseToBase64(input) {
        let copyOfDataResponse: any = {};
        copyOfDataResponse.id = input.id;
        copyOfDataResponse.rawId = base64url.encode(input.rawId);
        copyOfDataResponse.response = {};
        copyOfDataResponse.response.attestationObject = base64url.encode(input.response.attestationObject);
        copyOfDataResponse.response.clientDataJSON = base64url.encode(input.response.clientDataJSON);
        copyOfDataResponse.type = input.type;
        return copyOfDataResponse;
    }

    preauthToBuffer(input) {
        input.challenge = base64url.decode(input.challenge);
        if (input.allowCredentials) {
            for (let i = 0; i < input.allowCredentials.length; i++) {
                //Because these were stored incorrectly, must convert from Base64 to Base64URL safe first
                input.allowCredentials[i].id = input.allowCredentials[i].id.replace(/\+/g, "-").replace(/\//g, "_").replace(/=/g, "");
                input.allowCredentials[i].id = base64url.decode(input.allowCredentials[i].id);
            }
        }
        return input;
    }

    preauthResponseToBase64(input) {
        let copyOfDataResponse: any = {};
        copyOfDataResponse.id = input.id;
        copyOfDataResponse.rawId = base64url.encode(input.rawId);
        copyOfDataResponse.response = {};
        copyOfDataResponse.response.authenticatorData = base64url.encode(input.response.authenticatorData);
        copyOfDataResponse.response.signature = base64url.encode(input.response.signature);
        copyOfDataResponse.response.userHandle = base64url.encode(input.response.userHandle);
        copyOfDataResponse.response.clientDataJSON = base64url.encode(input.response.clientDataJSON);
        copyOfDataResponse.type = input.type;
        return copyOfDataResponse;
    };

    fidoCancel() {
        let user = this._sharedService.getUser();
        
        if(user.jwt === undefined || user.username === undefined){
            this._router.navigate([""]);
        } else {
        this._restService.isLoggedIn(user).
            then(resp => {
                let responseJSON = JSON.parse(JSON.stringify(resp));
                let body = responseJSON.body;
                let isLoggedInResponse = body.Response;
                console.log("isLoggedIn == " + this.isLoggedInResponse);
                if (this.isLoggedInResponse === undefined) {
                    this._router.navigate([""]);
                }
                else {
                    this._router.navigate(["/profile"]);
                }
            }); 
        }
    }   
}


