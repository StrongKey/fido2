import { Component, OnInit, ViewEncapsulation, ViewChild, ViewContainerRef, } from "@angular/core";
import { Router, ActivatedRoute } from "@angular/router";
import { ScriptLoaderService } from '../../_services/script-loader.service';
import { ConstantsService } from "../../_services/constants.service";
import { Helpers } from "../../helpers";
import { User } from "../_models/user";
import { CookieService } from 'ngx-cookie-service';
import { RestService } from '../../_services/rest.service';
import { SharedService } from '../../_services/shared.service';

@Component({
    selector: '.m-grid.m-grid--hor.m-grid--root.m-page',
    templateUrl: './register.component.html',
    encapsulation: ViewEncapsulation.None,
    styles: [`
    .col-md-9 {
      padding :  0px!important;
    }
    .col-md-3 {
      padding :  0px 15px 0px 0px !important;
    }

    .m-login__container {
      width: 500px; height: 800px; margin-top: 50px; margin-left: auto; margin-right: auto;
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

export class RegisterComponent implements OnInit {

    model: any = {};
    user: User = new User();
    loading = false;
    company: string;
    hash: string;

    @ViewChild('alertSignup',
        { read: ViewContainerRef }) alertSignup: ViewContainerRef;

    constructor(private _router: Router,
        private _script: ScriptLoaderService,
        private _activatedRoute: ActivatedRoute,
        private _restService: RestService,
        private _sharedService: SharedService) {
        console.log("register component")
    }

    ngOnInit(): void {
        this._script.loadScripts('body', [
            'assets/vendors/base/vendors.bundle.js',
            'assets/demo/demo6/base/scripts.bundle.js'], true).then(() => {
                Helpers.setLoading(false);
                this.handleRegisterFormSubmit();
            });
        // fetch info based off of hash
        this._activatedRoute.params.subscribe(params => {
            this.hash = params["hash"];
            this._sharedService.setRegisterUserURLHash(this.hash);
        });
    }

    register() {
        this._sharedService.setUser(this.user);
        this._restService.preregister(this.user, this.hash)
            .then(resp => {
                if (resp.Error === 'False') {
                    this.redirectToFIDO(resp.Response);
                }
            }).catch(error => {
                this._sharedService.setError(error);
                this._router.navigateByUrl("/login");
            });
    }

    handleRegisterFormSubmit() {
        $('#m_login_register_submit').click((e) => {
            let form = $(e.target).closest('form');
            form.validate({
                rules: {
                    firstnameDOM: {
                        required: true,
                    },
                    lastnameDOM: {
                        required: true,
                    },
                    usernameDOM: {
                        required: true,
                    },
                    displayNameDOM: {
                        required: true,
                    }
                },
            });
            if (!form.valid()) {
                console.log("form not valid");
                e.preventDefault();
                return;
            }
        });
    }

    redirectToFIDO(response: string) {
        console.log("redirectToFIDO");
        this._sharedService.setPreregObject(response);
        this._router.navigateByUrl("fido/register");
    }

}
