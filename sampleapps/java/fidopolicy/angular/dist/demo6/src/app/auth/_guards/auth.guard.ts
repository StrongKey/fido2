import { Injectable } from "@angular/core";
import { ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot } from "@angular/router";
import { HttpClient } from '@angular/common/http';
import { Observable } from "rxjs";

import { RestService, SharedService } from "../../_services";

@Injectable()
export class AuthGuard implements CanActivate {

    constructor(private _router: Router, private _http: HttpClient, private _restService: RestService, private _sharedService: SharedService) {
    }
    canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
        console.log("canActivate component");
        let user = this._sharedService.getUser();
        if(user.jwt === undefined || user.username === undefined){
            this._router.navigate([""]);
        } else {
            return this._restService.isLoggedIn(user).then(resp => {
                let responseJSON = JSON.parse(JSON.stringify(resp || null));
                let body = responseJSON.body;
                if (body.Response.length != 0 && body.Response != "") {
                    this._sharedService.setUsername(body.Response);
                    return true;
                }
                this._router.navigate(['/']);
                return false;
            },
                error => {
                    this._router.navigate(['/']);
                    return false;
                });
            }
    }
}
