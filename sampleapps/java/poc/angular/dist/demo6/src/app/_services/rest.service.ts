import { Injectable } from "@angular/core";
import { Headers, Http, Response, URLSearchParams } from "@angular/http";
import { ActivatedRoute, Router } from '@angular/router';
import { User } from '../auth/_models/user';
import { SharedService } from './shared.service';
import { ConstantsService } from './constants.service';
import "rxjs/add/operator/map";
import { DefaultUrlHandlingStrategy } from "@angular/router/src/url_handling_strategy";

@Injectable()
export class RestService {
    private pocURL = ConstantsService.baseURL + ":8443/poc/fido2";
    private fidoHeaders = new Headers({ 'Content-Type': 'application/json' });
    constructor(private http: Http, private _router: Router, private sharedService: SharedService) {
        this.fidoHeaders.append('Cache-Control', 'no-cache, no-store, must-revalidate');
        this.fidoHeaders.append('Pragma', 'no-cache');
        this.fidoHeaders.append('Expires', '-1');
    }

    deleteUser() {
        let restURL = this.pocURL + "/deleteAccount";
        return this.http.post(restURL, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);
    }

    registerEmail(email: string) {
        let restURL = this.pocURL + "/registerEmail";
        let body = new URLSearchParams();
        return this.http.post(restURL, { "email": email }, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractData(resp))
            .catch(this.handleError);
    }

    preregister(user: User, nonce: string) {
        let restURL = this.pocURL + "/preregister";
        let body = { "firstName": user.firstName, "lastName": user.lastName, "displayName": user.displayName, "username": user.username, "nonce": nonce };
        return this.http.post(restURL, body, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractData(resp))
            .catch(this.handleError);
    }

    register(registerJSON: any) {
        let restURL = this.pocURL + "/register";
        let body = { "payload": registerJSON };
        return this.http.post(restURL, registerJSON, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);
    }

    preauthenticate(username: string) {
        let restURL = this.pocURL + "/preauthenticate";
        let body = { "username": username };
        return this.http.post(restURL, body, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractData(resp))
            .catch(this.handleError);
    }

    authenticate(authenticateJSON: any) {
        let restURL = this.pocURL + "/authenticate";
        return this.http.post(restURL, authenticateJSON, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);

    }

    preregisterExisting(displayName: string) {
        let restURL = this.pocURL + "/preregisterExisting";
        let body = { "displayName": displayName };
        return this.http.post(restURL, body, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);

    }

    registerExisting(registerJSON: any) {
        let restURL = this.pocURL + "/registerExisting";
        let body = { "payload": registerJSON };
        return this.http.post(restURL, registerJSON, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);

    }

    isLoggedIn() {
        let restURL = this.pocURL + "/isLoggedIn";
        return this.http.post(restURL, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);
    }

    getUsersInfo() {
        let restURL = this.pocURL + "/getuserinfo";
        return this.http.post(restURL, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);
    }

    deleteFIDOKeys(randomIDs: string[]) {
        let restURL = this.pocURL + "/removeKeys";
        let body = { "keyIds": randomIDs };
        return this.http.post(restURL, body, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractData(resp))
            .catch(this.handleError);
    }



    logout() {
        let restURL = this.pocURL + "/logout";
        return this.http.post(restURL, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);
    }


    private extractDataAndHeaders(res: Response) {
        let header = res.headers;
        let body = res.json();
        let responseJSON = JSON.parse(JSON.stringify(body));
        if (responseJSON.Error == "True") {
            this.sharedService.setError("Error has occured. Check the logs.");
        }
        return { header, body } || {};
    }


    private extractData(res: Response) {
        let body = res.json();
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
        let body = error._body;
        let bodyJSON = JSON.parse(body);
        let message = bodyJSON.Message;
        let errMsg: string = error.toString();
        if (error instanceof XMLHttpRequest) {
            errMsg = error.statusText;
        }
        if (error instanceof Response) {
            let body = error.json() || '';
            let err = body.error || JSON.stringify(body);
            errMsg = error.status + error.statusText || err;
        }
        return Promise.reject(message);
    }

}
