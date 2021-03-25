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

  `]
})

export class FIDOComponent implements OnInit, AfterViewInit {

    username: string;
    isLoggedInResponse: string;
    errorMessage = "";
    hasError: boolean;
    isRegister: boolean;
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

        this._restService.isLoggedIn().
            then(resp => {
                let responseJSON = JSON.parse(JSON.stringify(resp));
                let body = responseJSON.body;
                this.isLoggedInResponse = body.Response;
            });
    }

    ngAfterViewInit() {
        this.ref.detectChanges();
    }

    onError(error: string) {
        console.log("onError fido component == " + error);
        this.hasError = true;
        this.errorMessage = error;

        // this._sharedService.setError(this.errorMessage);
        Swal.fire({
            position: 'top',
            icon: 'error',
            title: 'Oops...',
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
                // Read more about handling dismissals
                result.dismiss === Swal.DismissReason.cancel
            ) {
                console.log("else retry");
                // this._router.navigate(["/login"]);
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
        console.log("isLoggedIn == " + this.isLoggedInResponse);
        if (this.isLoggedInResponse === "") {
            this._restService.register(response).
                then(regResponse => that.onRegResult(regResponse))
                .catch(error => {
                    that.onError(error)
                });
        }
        else {
            this._restService.registerExisting(response).
                then(regResponse => that.onRegResult(regResponse))
                .catch(error => {
                    that.onError(error)
                });
        }
    }



    onRegResult(regResponse: any) {
        let responseJSON = JSON.parse(JSON.stringify(regResponse));
        let body = responseJSON.body;
        if (body.Response === "Successfully processed registration response") {
            if (this.isLoggedInResponse === "") {
                //this.zone.run(() => this._router.navigateByUrl("/dashboard"));
                  this.zone.run(() => this._router.navigateByUrl("/profile"));

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
        let body = JSON.parse(responseJSON.Response);
        let user: User = <User>JSON.parse(body.user);

        if (responseJSON.Error == "false") {
            this._sharedService.setUser(user);
            this._sharedService.setRegisterUserURLHash('');
            // only register user for applications if this is a demo and if the user belongs to the original company's CID
            this.zone.run(() => this._router.navigateByUrl("/dashboard"));

        }
    }


    fidoPreauth() {
        let username = this._sharedService.getPreauthUsername();
        this._restService.preauthenticate(username)
            .then(resp => this.fidoAuthenticate(resp.Response))
            .catch(error => {
                this._sharedService.setError(error);
                this.zone.run(() => this._router.navigateByUrl("/login"));
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
                    that._restService.authenticate(response)
                        .then(authResponse => that.onAuthResult(authResponse));
                }).catch((error) => {
                    this.onError(error);
                });
        }
    }


    onAuthResult(authResponse: any) {
        console.log("Auth response from server: ");
        console.log("authResponse : " + JSON.stringify(authResponse));
        if (authResponse.body.Error === "False") {
             localStorage.setItem("jwt_payload",authResponse.body.JWT);

           // this.zone.run(() => this._router.navigateByUrl("/dashboard"));
           this.zone.run(() => this._router.navigateByUrl("/profile"));
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
        if (this.isRegister) {
            let registerURLHash = this._sharedService.getRegisterUserURLHash();
            if (registerURLHash) {
                let user = this._sharedService.getUser();

                this._restService.preregister(user, registerURLHash)
                    .then(resp => {
                        if (resp.Error === 'False') {
                            console.log("in preregister");
                            this.fidoRegister(JSON.parse(resp.Response));
                        }
                    }).catch(error => {
                        this._sharedService.setError(error);
                        this._router.navigateByUrl("/login");
                    });
            }
            else {
                console.log("adding new fido key for existing user");
                this._restService.preregisterExisting("newkey")
                    .then(resp => {
                        let response = JSON.parse(JSON.stringify(resp));
                        if (response.Error === 'False') {
                            console.log("need to work on the preregisterExisting retryy")
                        }

                    });
            }
            // let preauthObject = this._sharedService.getPreauthObject();
            //
            // if (preauthObject) this.fidoRegister(JSON.parse(JSON.stringify(preauthObject)));
            // else
            // this.fidoPrereg();

        } else {
            this.fidoPreauth();
        }
        this.ref.detectChanges();
    }

    preregToBuffer(input) {
        console.log("input = " + JSON.stringify(input));
        // input = JSON.parse(input);
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
        if (this.isLoggedInResponse === "") {
            this._router.navigate(["/login"]);
        }
        else {
            this._router.navigate(["/profile"]);
        }

    }

}
