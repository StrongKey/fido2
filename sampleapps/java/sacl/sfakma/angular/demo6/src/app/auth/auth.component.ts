import { Component, ComponentFactoryResolver, OnInit, ViewChild, ViewContainerRef, ViewEncapsulation, } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ScriptLoaderService } from '../_services/script-loader.service';
import { Helpers } from '../helpers';
import { RestService } from '../_services/rest.service';
import { SharedService } from '../_services/shared.service';
import { ConstantsService } from '../_services/constants.service';
import { User } from './_models/user';
import Swal from 'sweetalert2';
declare let $: any;
declare let mUtil: any;

@Component({
    selector: '.m-grid.m-grid--hor.m-grid--root.m-page',
    templateUrl: './templates/login.component.html',
    encapsulation: ViewEncapsulation.None,
    styles: [`
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

export class AuthComponent implements OnInit {

    loading = false;
    returnUrl: string;
    cid: number;
    username: string;
    email: string;
    remember: boolean = false;

    @ViewChild('alertSignin', { read: ViewContainerRef, static: true }) alertSignin: ViewContainerRef;
    @ViewChild('alertSignup', { read: ViewContainerRef, static: true }) alertSignup: ViewContainerRef;


    constructor(
        private _router: Router,

        private _script: ScriptLoaderService,
        private _route: ActivatedRoute,
        private restService: RestService,
        private _sharedService: SharedService,
        private cfr: ComponentFactoryResolver) {
        console.log("auth compoennt");
        if (!this._sharedService.getIsWebauthnSupported()) {
            Swal.fire({
                position: 'top',
                icon: 'error',
                title: '<strong>WebAuthn Support</strong>',
                html:
                'Your browser does not appear to support WebAuthn.' + 'For a list of the browsers that currently support WebAuthn, please' +
                '<a href="https://caniuse.com/#search=webauthn" target="_blank">[check here]</a> ' +
                'If you have questions, please contact us via email at support@strongkey.com.',
                confirmButtonText: 'OK'
            })
        }
        // if user is already logged in, show them the portal page // TODO:
    }

    ngOnInit() {
        this._script.loadScripts('body', [
            'assets/vendors/base/vendors.bundle.js',
            'assets/demo/demo6/base/scripts.bundle.js'], true).then(() => {
                Helpers.setLoading(false);
                this.handleFormSwitch();
                this.handleSignInFormSubmit();
                this.handleSignUpFormSubmit();
            });
    }

    signin() {
        console.log("signin");
        this.loading = true;
        this._router.navigateByUrl('/fido/authenticate');
        this._sharedService.setPreauthUsername(this.username);
    }

    signup() {
        this.loading = true;
        let that = this;
        this.restService.registerEmail(this.email)
            .then(resp => {
                console.log("registerEmail response = " + JSON.stringify(resp));
                this.loading = false;
                that._sharedService.setMessage("Check your email to complete the registration process.");
            }).catch(error => {
                that.loading = false;
                that._sharedService.setError(error)
            });
    }

    handleSignInFormSubmit() {
        $('#m_login_signin_submit').click((e) => {
            console.log("sigin button clicked");
            let form = $(e.target).closest('form');
            form.validate({
                rules: {
                    cidDOM: {
                        required: true,
                    },
                    usernameDOM: {
                        required: true,
                    },
                },
            });
            console.log("form valid == " + form.valid());
            if (!form.valid()) {
                console.log("form not valid");
                e.preventDefault();
                return;
            }
        });
    }

    displaySignUpForm() {
        let login = document.getElementById('m_login');
        mUtil.removeClass(login, 'm-login--signin');

        mUtil.addClass(login, 'm-login--signup');
        mUtil.animateClass(login.getElementsByClassName('m-login__signup')[0], 'flipInX animated');
    }

    displaySignInForm() {
        let login = document.getElementById('m_login');
        mUtil.removeClass(login, 'm-login--signup');
        try {
            $('form').data('validator').resetForm();
        } catch (e) {
        }

        mUtil.addClass(login, 'm-login--signin');
        mUtil.animateClass(login.getElementsByClassName('m-login__signin')[0], 'flipInX animated');
    }


    handleFormSwitch() {
        document.getElementById('m_login_signup').addEventListener('click', (e) => {
            e.preventDefault();
            this.email = '';
            this.displaySignUpForm();
        });

        document.getElementById('m_login_signup_cancel').addEventListener('click', (e) => {
            e.preventDefault();
            this.email = '';
            this.displaySignInForm();
        });
    }

    handleSignUpFormSubmit() {
        document.getElementById('m_login_signup_submit').addEventListener('click', (e) => {
            let btn = $(e.target);
            let form = $(e.target).closest('form');
            form.validate({
                rules: {
                    emailDOM: {
                        required: true,
                        email: true,
                    },
                },
            });
            if (!form.valid()) {
                e.preventDefault();
                return;
            }
        });
    }

}
