import { Component, OnInit, ViewEncapsulation, NgZone, ViewChild } from '@angular/core';
import { Helpers } from '../../../../helpers';
import { Router, ActivatedRoute } from "@angular/router";
import { SecurityKey } from '../../../../auth/_models/securitykey.model';
import { User } from '../../../../auth/_models/user';
import { RestService } from '../../../../_services/rest.service';
import { SharedService } from '../../../../_services/shared.service';
import { ConstantsService } from "../../../../_services/constants.service";
import swal from 'sweetalert2';


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
    `]
})
export class ProfileComponent implements OnInit {

  // array to store fidokeys
  fidoKeys: SecurityKey[] = [];
  user: User = new User();
  cid: number;
  username: string;
  isAllSelected: boolean = false;
  // @ViewChild('deleteSwal') private deleteSwal: SwalComponent;
  constructor(private restService: RestService,
    private _sharedService: SharedService,
    private zone: NgZone,
    private _router: Router,
  ) {

  }
  ngOnInit() {
    this.getUserInfo();
  }


  deSelectAll() {
    for (let key of this.fidoKeys) {
      key.checked = false;
    }
  }

  addKey() {
    this.restService.preregisterExisting("newkey")
      .then(resp => {
        let response = JSON.parse(JSON.stringify(resp));
        let body = response.body;
        this.redirectToFIDO(body.Response);
      });
  }

  redirectToFIDO(response: string) {
    console.log("redirectToFIDO");
    console.log("redirectToFIDO - response : " + JSON.stringify(response));
    this._sharedService.setPreregObject(response);
    this._router.navigateByUrl("fido/register");
  }

  // function to delete fido keys
  deleteKeys() {
    let randomIDs = [];
    for (let key of this.fidoKeys) {
      if (key.checked) {
        randomIDs.push(key.randomid);
      }
    }
    if (randomIDs.length > 0) {
      if (this.fidoKeys.length > 1 &&  (randomIDs.length < this.fidoKeys.length)) {
        this.restService.deleteFIDOKeys(randomIDs)
          .then(resp => {
            let responseJSON = JSON.parse(JSON.stringify(resp));
            console.log("responseJSON = " + JSON.stringify(responseJSON));
            if (responseJSON.Response === "Success") {
              this._sharedService.setMessage("Successfully deleted the fido key.");
              this.getUserInfo();
              this.deSelectAll();
            }
            else {
              this._sharedService.setError("Failed to delete key.");
            }
          });

      }
      else {
        this._sharedService.setError("Please register a second key before deleting this one. To delete this account, use the Advanced Tab.");
        this.deSelectAll();
      }
    }
  }


  getUserInfo() {
    this.fidoKeys = [];
    this.restService.getUsersInfo()
      .then(resp => {
        let responseJSON = JSON.parse(JSON.stringify(resp));
        let body = responseJSON.body;
        let result = JSON.parse(body.Response);
        this.user.firstName = result.userInfo.firstName;
        this.user.lastName = result.userInfo.lastName;
        this.user.email = result.userInfo.email;
        let fidoKeys = result.keys.Response.keys;
        for (let i = 0; i < fidoKeys.length; i++) {
          let fidoKey = <SecurityKey>fidoKeys[i];
          this.fidoKeys.push(fidoKey);
        }
      });
  }

  deleteUser() {
    swal.queue([{
      title: 'Are you sure you would like to delete the account?',
      type: 'warning',
      showCancelButton: true,
      confirmButtonText: 'Yes, delete it!',
      cancelButtonText: 'No, cancel!',
      reverseButtons: true,
      text:
        'We are deleting the account and respective FIDO keys.',
      showLoaderOnConfirm: true,
      preConfirm: () => {
        return this.restService.deleteUser()
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
    console.log("profile component logout -- called");
    // let user = this._sharedService.getUser();
    this.restService.logout()
      .then(resp => {
        let responseJSON = JSON.parse(JSON.stringify(resp));
        console.log("resp - logout = " + JSON.stringify(responseJSON));

      })

    this._router.navigate([""]);

  }
}
