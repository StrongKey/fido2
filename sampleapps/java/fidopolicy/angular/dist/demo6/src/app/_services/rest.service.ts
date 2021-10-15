import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient, HttpParams, HttpErrorResponse } from "@angular/common/http";
import { ActivatedRoute, Router } from '@angular/router';
import { User } from '../auth/_models/user';
import { SharedService } from './shared.service';
import { ConstantsService } from './constants.service';
import { updateRestTypeNode } from "typescript";
import { yearsPerRow } from "@angular/material";

@Injectable()
export class RestService {
    private pocURL = ConstantsService.baseURL + "/fidopolicyboa/fido2";
    private fidoHeaders = new HttpHeaders({ 'Content-Type': 'application/json' });
    constructor(private http: HttpClient, private _router: Router, private sharedService: SharedService) {
        this.fidoHeaders.append('Cache-Control', 'no-cache, no-store, must-revalidate');
        this.fidoHeaders.append('Pragma', 'no-cache');
        this.fidoHeaders.append('Expires', '-1');
    }

    deleteUser(user:User) {
        let restURL = this.pocURL + "/deleteAccount";
        let body = { "username": user.username,"policy": user.policy, "jwt": user.jwt };
        return this.http.post(restURL, body, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);
    }

    preregister(user: User) {
        let restURL = this.pocURL + "/preregister";
        let body = { "displayName": "Initial registration", "username": user.username, "policy": user.policy };
        return this.http.post(restURL, body, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractData(resp))
            .catch(this.handleError);
    }

    register(registerJSON: any, user: User) {
        let restURL = this.pocURL + "/register";
        registerJSON["policy"]=user.policy;
        registerJSON["username"]=user.username;
        return this.http.post(restURL, registerJSON, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);
    }

    preauthenticate(user: any) {
        let restURL = this.pocURL + "/preauthenticate";
        let body = { "username": user.username, "policy": user.policy};
        return this.http.post(restURL, body, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractData(resp))
            .catch(this.handleError);
    }

    authenticate(authenticateJSON: any, user: User) {
        let restURL = this.pocURL + "/authenticate";
        authenticateJSON["policy"]=user.policy;
        authenticateJSON["username"]=user.username;
        return this.http.post(restURL, authenticateJSON, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);

    }

    preregisterExisting(displayName: string, user: User) {
        let restURL = this.pocURL + "/preregisterExisting";
        let body = { "displayName": displayName, "policy": user.policy, "jwt": user.jwt, "username": user.username };
        return this.http.post(restURL, body, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);

    }

    registerExisting(registerJSON: any, user: User) {
        let restURL = this.pocURL + "/registerExisting";
        registerJSON["policy"]=user.policy;
        registerJSON["username"]=user.username;
        registerJSON["jwt"]=user.jwt;
        return this.http.post(restURL, registerJSON, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);
    }

    isLoggedIn(user: User) {
        let restURL = this.pocURL + "/isLoggedIn";
            let body = { "username": user.username,"policy": user.policy, "jwt": user.jwt };
            return this.http.post(restURL, body, { headers: this.fidoHeaders })
                .toPromise()
                .then(resp => this.extractDataAndHeaders(resp))
                .catch(this.handleError);
    }

    getUsersInfo(user: User) {
        let restURL = this.pocURL + "/getuserinfo";
        return this.http.post(restURL, {"username": user.username,"policy":user.policy,"jwt":user.jwt}, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);
    }

    deleteFIDOKeys(randomIDs: string[],user:User) {
        let restURL = this.pocURL + "/removeKeys";
        let body = { "keyIds": randomIDs, "policy":user.policy,"jwt":user.jwt, "username":user.username };
        return this.http.post(restURL, body, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractData(resp))
            .catch(this.handleError);
    }

    logout() {
        this.sharedService.deleteUser();
        let restURL = this.pocURL + "/logout";
        return this.http.post(restURL, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);
    }


    private extractDataAndHeaders(res) {
        let header = res.headers;
        let body = JSON.parse(JSON.stringify(res));
        let responseJSON = JSON.parse(JSON.stringify(body));
        if (responseJSON.Error == "True") {
            this.sharedService.setError("Error has occured. Check the logs.");
        }
        return { header, body } || {};
    }


    private extractData(res) {
        let body = JSON.parse(JSON.stringify(res));
        let responseJSON = JSON.parse(JSON.stringify(body));
        if (responseJSON.Error == "true") {
            if (responseJSON.Message.includes("Invalid JWT received")) this._router.navigateByUrl('');
            console.log("extractData - true = ");
            this.sharedService.setError(responseJSON.Message);
        }

        return body || {};
    }
    private handleError(error: any) {
        console.log("handle Error");
        console.log("Error =  " + JSON.stringify(error));
        let message:string;
        let err = error.error;
        console.log(err);
        message = err.Message;
        console.log(message);
        if(message.includes("POC-WS-ERR-"))
        {
          message = message.split(":")[1];
        }
        else if(message.includes("POC-ERR-"))
        {
          var index = message.indexOf("FIDO 2 Error Message");
          index = index + message.substring(index).indexOf(":");
          message = message.substring(index+1);
          message = message.replace('{0}','');
          message = message.replace('}','');
          message = message.replace('"','');
        }
        let errMsg: string = error.toString();
        if (error instanceof XMLHttpRequest) {
            errMsg = error.statusText;
        }
        if (error instanceof HttpErrorResponse) {
            let body = JSON.parse(JSON.stringify(error)) || '';
            let err = body.error || JSON.stringify(body);
            errMsg = error.status + error.statusText || err;
        }
        return Promise.reject(message);
    }

}
