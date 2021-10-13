import { Component, ComponentFactoryResolver, OnInit, ViewEncapsulation, ViewChild, ViewContainerRef, } from "@angular/core";
import { Router, ActivatedRoute } from "@angular/router";
import { ScriptLoaderService } from '../../_services/script-loader.service';
import { ConstantsService } from "../../_services/constants.service";
import { Helpers } from "../../helpers";
import { User } from "../_models/user";
import { CookieService } from 'ngx-cookie-service';
import { RestService } from '../../_services/rest.service';
import { SharedService } from '../../_services/shared.service';
import Swal from 'sweetalert2';


@Component({
  selector: '.m-grid.m-grid--hor.m-grid--root.m-page',
  templateUrl: './registerLogin.component.html',
  encapsulation: ViewEncapsulation.None,
  styles: [`
    .col-md-9 {
      padding :  0px!important;
    }
    .col-md-3 {
      padding :  0px 15px 0px 0px !important;
    }

    .m-login__container {
      width: 500px; height:400px; margin-top: 50px; margin-left: auto; margin-right: auto;
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

    .m-login__form{
      margin-top: 5%;
    }

    .m-title{
      margin-left:5%;
      margin-top: 5%;
    }

    .content {
      padding: 16px;
    }

    .content > mat-card {
      width: 200px;
    }
    pre {
     overflow-x: auto;
     white-space: pre-wrap;
     white-space: -moz-pre-wrap;
     white-space: -pre-wrap;
     white-space: -o-pre-wrap;
     word-wrap: break-word;
        }

    hr{
      display:block;
      height:1px;
      border:0;
      border-top:1px solid #ccc;
      margin: 0 0;
      padding:0;
    }

  `]
})

export class RegisterLoginComponent implements OnInit {

  model: any = {};
  user: User = new User();
  loading = false;
  company: string;
  hash: string;
  returnUrl: string;
  cid: number;
  username: string;
  email: string;
  remember: boolean = false;
  policySelected: boolean = false;
  selectedPolicyList: string[] = [];
  selectedPolicyDescList: string[] = [];
  selectedPolicyJsonList: string[] = [];
  selectedPolicyJson: string
  outerPolicy: boolean = true;
  policyRequired: string;
  policyList: string[] = [];
  policyDescList: string[] = [];
  restrictedPolicyList: string[] = [];
  restrictedPolicyDescList: string[] = [];
  restrictedPolicyJsonList: string[] = [];
  strictPolicyList: string[] = [];
  strictPolicyDescList: string[] = [];
  strictPolicyJsonList: string[] = [];
  moderatePolicyList: string[] = [];
  moderatePolicyDescList: string[] = [];
  moderatePolicyJsonList: string[] = [];
  minimalPolicyList: string[] = [];
  minimalPolicyDescList: string[] = [];
  minimalPolicyJsonList: string[] = [];
  strictAndroidSafetyNetRef: string;
  strictRef: string;
  moderateRef: string;
  minimalRef: string;
  restrictedAndroidKeyRef: string;
  restrictedTpmRef: string;
  restrictedAppleRef: string;
  restrictedFipsRef: string;


  @ViewChild('alertSignin', { read: ViewContainerRef, static: true }) alertSignin: ViewContainerRef;
  @ViewChild('alertSignup', { read: ViewContainerRef, static: true }) alertSignup: ViewContainerRef;

  constructor(private _router: Router,
    private _script: ScriptLoaderService,
    private _activatedRoute: ActivatedRoute,
    private _restService: RestService,
    private _sharedService: SharedService,
    private cfr: ComponentFactoryResolver) {
    console.log("register component");
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
    this.user.policy = "minimal";
    // ConstantsService.tab.innerHTML = "<a href='https://github.com/strongkey/fido2/tree/master/sampleapps/java/sacl'> SACL </a>";


    //To hold different policy labels - restricted, strict, moderate and minimal
    this.policyList.push(ConstantsService.restrictedLabel);
    this.policyList.push(ConstantsService.strictLabel);
    this.policyList.push(ConstantsService.moderateLabel);
    this.policyList.push(ConstantsService.minimalLabel);

    //To hold subtypes in restricted policy - Basic, Android, TPM, Apple, FIPS
    this.restrictedPolicyList.push(ConstantsService.restrictedAndroidKey);
    this.restrictedPolicyList.push(ConstantsService.restrictedApple);
    this.restrictedPolicyList.push(ConstantsService.restrictedTpm);
    this.restrictedPolicyList.push(ConstantsService.restrictedFips);

    //To hold subtypes in strict policy - Basic, Android, TPM, Apple, FIPS
    this.strictPolicyList.push(ConstantsService.strictBasic);
    this.strictPolicyList.push(ConstantsService.strictAndroidSafetyNet);
    //To hold subtypes in moderate policy - Basic, Android, TPM, Apple, FIPS
    this.moderatePolicyList.push(ConstantsService.moderateBasic);
    //To hold subtypes in minimal policy - Basic, Android, TPM, Apple, FIPS
    this.minimalPolicyList.push(ConstantsService.minimalBasic);


    //main page policy descriptions
    this.policyDescList.push(ConstantsService.policyDesc.restricted);
    this.policyDescList.push(ConstantsService.policyDesc.strict);
    this.policyDescList.push(ConstantsService.policyDesc.moderate);
    this.policyDescList.push(ConstantsService.policyDesc.minimal);

    //different types of restrcited policy descriptions
    this.restrictedPolicyDescList.push(ConstantsService.policyDesc.restrictedAndroidKey);
    this.restrictedPolicyDescList.push(ConstantsService.policyDesc.restrictedApple);
    this.restrictedPolicyDescList.push(ConstantsService.policyDesc.restrictedTpm);
    this.restrictedPolicyDescList.push(ConstantsService.policyDesc.restrictedFips);

    //different types of strict policy descriptions
    this.strictPolicyDescList.push(ConstantsService.policyDesc.strictBasic);
    this.strictPolicyDescList.push(ConstantsService.policyDesc.strictAndroidSafetyNet);
    //different types of moderate policy descriptions
    this.moderatePolicyDescList.push(ConstantsService.policyDesc.moderateBasic);
    //different types of minimal policy descriptions
    this.minimalPolicyDescList.push(ConstantsService.policyDesc.minimalBasic);


    //To hold JSON descriptions for all types of restricted policies
    this.restrictedPolicyJsonList.push(ConstantsService.restrictedAndroidKeyJson);
    this.restrictedPolicyJsonList.push(ConstantsService.restrictedAppleJson);
    this.restrictedPolicyJsonList.push(ConstantsService.restrictedTpmJson);
    this.restrictedPolicyJsonList.push(ConstantsService.restrictedFipsJson);

    //To hold JSON descriptions for all types of strict policies
    this.strictPolicyJsonList.push(ConstantsService.strictJson);
    this.strictPolicyJsonList.push(ConstantsService.strictAndroidSafetyNetJson);
    //To hold JSON descriptions for all types of moderate policies
    this.moderatePolicyJsonList.push(ConstantsService.moderateJson);
    //To hold JSON descriptions for all types of minimal policies
    this.minimalPolicyJsonList.push(ConstantsService.minimalJson);

    //variables holding policy names for dropdown
    this.strictAndroidSafetyNetRef = ConstantsService.strictAndroidSafetyNet;
    this.strictRef = ConstantsService.strictBasic;
    this.moderateRef = ConstantsService.moderateBasic;
    this.minimalRef = ConstantsService.minimalBasic;
    this.restrictedAndroidKeyRef = ConstantsService.restrictedAndroidKey;
    this.restrictedTpmRef = ConstantsService.restrictedTpm;
    this.restrictedAppleRef = ConstantsService.restrictedApple;
    this.restrictedFipsRef = ConstantsService.restrictedFips;

    //variables used to display subtypes of a policy when clicked view more
    this.selectedPolicyList = this.policyList;
    this.selectedPolicyDescList = this.policyDescList;
  }

  ngOnInit(): void {
    this._script.loadScripts('body', [
      'assets/vendors/base/vendors.bundle.js',
      'assets/demo/demo6/base/scripts.bundle.js'], true).then(() => {
        Helpers.setLoading(false);
        this.handleRegisterFormSubmit();
        this.handleSignInFormSubmit();

      });
  }

  viewCard(policyName: string) {
    if (policyName == ConstantsService.restrictedLabel) {
      this.selectedPolicyList = this.restrictedPolicyList;
      this.selectedPolicyDescList = this.restrictedPolicyDescList;
      this.selectedPolicyJsonList = this.restrictedPolicyJsonList;
    }
    else if (policyName == ConstantsService.strictLabel) {
      this.selectedPolicyList = this.strictPolicyList;
      this.selectedPolicyDescList = this.strictPolicyDescList;
      this.selectedPolicyJsonList = this.strictPolicyJsonList;

    }
    else if (policyName == ConstantsService.moderateLabel) {
      this.selectedPolicyList = this.moderatePolicyList;
      this.selectedPolicyDescList = this.moderatePolicyDescList;
      this.selectedPolicyJsonList = this.moderatePolicyJsonList;

    }
    else if (policyName == ConstantsService.minimalLabel) {
      this.selectedPolicyList = this.minimalPolicyList;
      this.selectedPolicyDescList = this.minimalPolicyDescList;
      this.selectedPolicyJsonList = this.minimalPolicyJsonList;

    }
    this.outerPolicy = false;

  }

// to view JSON description of a policy
  viewJson(index: number) {
    let ind = index;
    this.selectedPolicyJson = this.selectedPolicyJsonList[ind];
  }

// to handle back button
  returnToPolicies() {
    this.outerPolicy = true;
  }


  //check If UV Platform popup
  policyValue(event: any) {
    this.policySelected = true;
    if (this.user.policy == ConstantsService.strictAndroidSafetyNetPolicy ||
      this.user.policy == ConstantsService.strictPolicy ||
      this.user.policy == ConstantsService.restrictedAndroidKeyPolicy ||
      this.user.policy == ConstantsService.restrictedTpmPolicy ||
      this.user.policy == ConstantsService.restrictedApplePolicy
    ) {
      this.checkIfUVPlatform();
    }
  }

  checkIfUVPlatform() {
    let credentialsContainer: any;
    credentialsContainer = window;
    if (credentialsContainer.PublicKeyCredential) {
      credentialsContainer.PublicKeyCredential.isUserVerifyingPlatformAuthenticatorAvailable()
        .then(function(uvpaAvailable) {
          // If there is not a user-verifying platform authenticator
          if (!uvpaAvailable) {
            Swal.fire({
              position: 'top',
              icon: 'warning',
              title: '<strong>PLATFORM AUTHENTICATOR UNAVAILABLE ON YOUR DEVICE</strong>',
              html:
                'Choose a Minimal/Moderate Policy or Use a Security Key With User Verification. This is a sample text.This is a sample text.This is a sample textThis is a sample text',
              confirmButtonText: 'OK'
            })
          }
        });
    }
  }

  onError(error: string) {
    console.log("onError fido component == " + error);
    Swal.fire({
        position: 'top',
        icon: 'error',
        // title: 'Error...',
        html: '<br/>' + error + '<br/>',
        showCancelButton: false,
        confirmButtonText: 'OK',
        reverseButtons: true
    }).then((result) => { 
      this._router.navigate([""]);
    });
}

  register() {
    this._sharedService.setUser(this.user);
    this._restService.preregister(this.user)
      .then(resp => {
        if (resp.Error === 'False') {
          this.redirectToFIDO(resp.Response);
        }
      }).catch(error => {
        this._sharedService.setError(error);
        this.onError(error);
        this._router.navigateByUrl("");
      });
  }

  handleRegisterFormSubmit() {
    console.log(this.user.policy);
    $('#m_login_register_submit').click((e) => {
      let form = $(e.target).closest('form');

      form.validate({
        rules: {
          usernameDOM: {
            required: true,
          },
          policyDOM: {
            required: true,
          },

        },
      });
      this.policySelected = true;
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
    this._router.navigateByUrl("fido/register/new");
  }
  signin() {
    console.log("signin");
    this.loading = true;
    this._sharedService.setUser(this.user);
    this._router.navigateByUrl('/fido/authenticate/dummy');
    this._sharedService.setPreauthUsername(this.user.username);
  }
  handleSignInFormSubmit() {
    $('#m_login_signin_submit').click((e) => {
      console.log("sigin button clicked");
      let form = $(e.target).closest('form');
      form.validate({
        rules: {
          policyDOM: {
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

}
