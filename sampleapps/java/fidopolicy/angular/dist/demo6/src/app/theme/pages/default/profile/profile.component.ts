import { Component, OnInit, ViewEncapsulation, NgZone, ViewChild, ElementRef } from '@angular/core';
import { Helpers } from '../../../../helpers';
import { Router, ActivatedRoute } from "@angular/router";
import { SecurityKey } from '../../../../auth/_models/securitykey.model';
import { User } from '../../../../auth/_models/user';
import { RestService } from '../../../../_services/rest.service';
import { SharedService } from '../../../../_services/shared.service';
import { ConstantsService } from "../../../../_services/constants.service";
import Swal from 'sweetalert2';


@Component({
  selector: "app-profile",
  templateUrl: "./profile.component.html",
  encapsulation: ViewEncapsulation.None,
  styles: [`
    .nav.nav-pills, .nav.nav-tabs {
      margin-top: 20px;
    }
    .form-control[readonly] {
      background-color: #e9ecef !important;
      opacity: 1 !important;
    }
    .table-card {
      box-shadow: 0 4px 8px 0 rgba(0,0,0,0.2);
      transition: 0.3s;
      width: 40%;
    }

    .table-card:hover {
      box-shadow: 0 8px 16px 0 rgba(0,0,0,0.2);
    }

    `]
})
export class ProfileComponent implements OnInit {

  // array to store fidokeys
  fidoKeys: SecurityKey[] = [];
  platformKeys: SecurityKey[] = [];
  nonPlatformKeys: SecurityKey[] = [];
  displayedColumns: string[] = ['credentialId', 'displayName', 'createDate', 'modifyDate'];
  user: User = new User();
  cid: number;
  isAllSelected: boolean = false;
  newDisplayName: string;
  ifPlatformKey: boolean = false;
  ifNonPlatformKey: boolean = false;
  ifZeroPlatformKeys: boolean = false;


  constructor(private _restService: RestService,
    private _sharedService: SharedService,
    private zone: NgZone,
    private _router: Router,
  ) {
  }

  ngOnInit() {
    // let user = this._sharedService.getUser();
    // this._restService.isLoggedIn(user).
    // then(resp => {
    //     let responseJSON = JSON.parse(JSON.stringify(resp));
    //     let body = responseJSON.body;
    //     if (body.Response === undefined) {
    //         this._router.navigate([""]);
    //     }
    // });
    this.getUserInfo();

  }


  deSelectKeys() {
    for (let key of this.fidoKeys) {
      key.checked = false;
    }
  }

  addKey() {
    if (this.newDisplayName) {
      let user = this._sharedService.getUser();
      this._restService.preregisterExisting(this.newDisplayName, user)
        .then(resp => {
          let response = JSON.parse(JSON.stringify(resp));
          let body = response.body;
          this.redirectToFIDO(body.Response);
        });
    }
    else {
      this._sharedService.setError("Enter a display name.");
    }

  }

  redirectToFIDO(response: string) {
    console.log("redirectToFIDO");
    this._sharedService.setPreregObject(response);
    this._router.navigateByUrl("fido/register/existing");
  }

  // function to delete fido keys
  deleteKeys() {
    let randomIDs = [];
    for (let key of this.fidoKeys) {
      if (key.checked) {
        randomIDs.push(key.keyid);
      }
    }
    if (randomIDs.length > 0) {
      if (this.fidoKeys.length > 1 && (randomIDs.length < this.fidoKeys.length)) {
        let user = this._sharedService.getUser();
        this._restService.deleteFIDOKeys(randomIDs, user)
          .then(resp => {
            let responseJSON = JSON.parse(JSON.stringify(resp));
            console.log("responseJSON = " + JSON.stringify(responseJSON));
            if (responseJSON.Response === "Success") {
              this._sharedService.setMessage("Successfully deleted the fido key.");
              this.getUserInfo();
              this.deSelectKeys();
            }
            else {
              this._sharedService.setError("Failed to delete key.");
            }
          });

      }
      else {
        if (randomIDs.length === this.fidoKeys.length && this.fidoKeys.length > 1) {
          this._sharedService.setError("Cannot delete all keys. To delete all keys, you need to delete this account(use the Advanced tab).");
        }
        else {
          this._sharedService.setError("You cannot delete the final key registered on your account. Please register another key before deleting this key");
        }
        this.deSelectKeys();
      }
    }
    else {
      this._sharedService.setError("No keys are selected.");
    }
  }


  getUserInfo() {
    this.fidoKeys = [];
    this.ifPlatformKey = false;
    this.ifNonPlatformKey = false;
    let user = this._sharedService.getUser();
    this._restService.getUsersInfo(user)
      .then(resp => {
        let responseJSON = JSON.parse(JSON.stringify(resp));
        let body = responseJSON.body;
        let resultString = body.Response;
        let result = JSON.parse(resultString);
        let fidoKeys = result.Response.keys;
        let platformKeyCount = 0;

        for (let i = 0; i < fidoKeys.length; i++) {
          let fidoKey = <SecurityKey>fidoKeys[i];
          if (fidoKey.attestationFormat == 'tpm' || fidoKey.attestationFormat == 'apple' || fidoKey.attestationFormat.startsWith('android')) {
            this.ifPlatformKey = true;
            platformKeyCount++;
          }
          else {
            this.ifNonPlatformKey = true;
          }
          this.fidoKeys.push(fidoKey);
        }
        if(platformKeyCount == 0)
        {
          this.ifZeroPlatformKeys = true;
        }

      });
  }

  deleteUser() {
    Swal.queue([{
      title: 'Are you sure you would like to delete the account?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete it!',
      cancelButtonText: 'No, cancel!',
      reverseButtons: true,
      text:
        'We are deleting the account and respective FIDO keys.',
      showLoaderOnConfirm: true,
      preConfirm: () => {
        let user = this._sharedService.getUser();
        return this._restService.deleteUser(user)
          .then(resp => {
            let responseJSON = JSON.parse(JSON.stringify(resp));
            console.log("Resp =  " + JSON.stringify(responseJSON));
            let body = responseJSON.body;

            if (body.Response == "Success") {
              this._sharedService.setMessage("Successfully deleted user");
              this.logout();
            }
          })
      }
    }])
  }

  logout() {
    // initialize variabes
    this._restService.logout()
      .then(resp => {
        let responseJSON = JSON.parse(JSON.stringify(resp));
      })

    this._router.navigate([""]);

  }
}
