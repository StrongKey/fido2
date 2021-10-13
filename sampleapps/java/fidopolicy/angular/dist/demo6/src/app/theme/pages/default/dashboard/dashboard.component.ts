import { Component, OnInit, ViewEncapsulation, NgZone, ViewChild } from '@angular/core';
import { Router, ActivatedRoute } from "@angular/router";
import { RestService } from '../../../../_services/rest.service';
import { SharedService } from '../../../../_services/shared.service';


@Component({
    selector: "dashboard",
    templateUrl: "./dashboard.component.html",
    encapsulation: ViewEncapsulation.None,

})
export class DashboardComponent {
    // constructor( private _router: Router,
    //     private _restService: RestService,
    //     private _sharedService: SharedService ) {
    // }
    // ngOnInit(): void {
    //     let user = this._sharedService.getUser();
    //     this._restService.isLoggedIn(user).
    //     then(resp => {
    //         let responseJSON = JSON.parse(JSON.stringify(resp));
    //         let body = responseJSON.body;
    //         if (body.Response === undefined) {
    //             this._router.navigate([""]);
    //         }
    //     });
    // }
}

