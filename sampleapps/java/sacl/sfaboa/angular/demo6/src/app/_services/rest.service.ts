import { Injectable } from "@angular/core";
import { HttpHeaders, HttpClient, HttpParams, HttpErrorResponse } from "@angular/common/http";
import { ActivatedRoute, Router } from '@angular/router';
import { User } from '../auth/_models/user';
import { SharedService } from './shared.service';
import { ConstantsService } from './constants.service';

@Injectable()
export class RestService {
    // private pocURL = ConstantsService.baseURL + ":8181/poc/fido2";
   private pocURL = ConstantsService.baseURL + "/sfaboa/fido2";
    private fidoHeaders = new HttpHeaders({ 'Content-Type': 'application/json' });
    constructor(private http: HttpClient, private _router: Router, private sharedService: SharedService) {
        this.fidoHeaders.append('Cache-Control', 'no-cache, no-store, must-revalidate');
        this.fidoHeaders.append('Pragma', 'no-cache');
        this.fidoHeaders.append('Expires', '-1');
        
    }

    pingSFA(){
    let sfaURL= ConstantsService.sfaURL+"/ping";
    return this.http.get(sfaURL).toPromise()
    .then(resp=> console.log(resp))
    .catch(this.handleError);
    }
    getUserTransactions(){
    let restURL= ConstantsService.sfaURL+"/userTransactions";
    return this.http.get(restURL)
            // .toPromise()
            // .then(resp => this.extractData(resp))
            // .catch(this.handleError);
    }
    //console.log(resp[0].fidoSignature)

    deleteUser() {
        let restURL = this.pocURL + "/deleteAccount";
        return this.http.post(restURL, { headers: this.fidoHeaders })
            .toPromise()
            .then(resp => this.extractDataAndHeaders(resp))
            .catch(this.handleError);
    }

    registerEmail(email: string) {
        let restURL = this.pocURL + "/registerEmail";
        let body = new HttpParams();
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
       
        let username = this.sharedService.getPreauthUsername();
         if(username===""){
             username = localStorage.getItem("username");
         }
        let body = { "username": username };

        if(localStorage.getItem("jwt_payload")!==null){
            console.log("here it is",localStorage.getItem("jwt_payload"));
            var headers_object = new HttpHeaders().set("Authorization", `Bearer ${localStorage.getItem("jwt_payload")}`);
        }
        else{
            console.log("no jwt yet");
            console.log(localStorage.getItem("jwt_payload"));
        }

        // console.log(this.fidoHeaders)
        return this.http.post(restURL,body, { headers: headers_object    })
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
        let body = error._body;
        let bodyJSON = JSON.parse(body);
        let message = bodyJSON.Message;
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
