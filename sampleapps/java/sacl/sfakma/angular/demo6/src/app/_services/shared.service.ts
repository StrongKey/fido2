import { Injectable } from '@angular/core';
import { Observable ,  BehaviorSubject } from 'rxjs';
import { User } from '../auth/_models/user';
import { CookieService } from 'ngx-cookie-service';
import { HttpHeaders, HttpClient } from '@angular/common/http';
import { ConstantsService } from './../_services/constants.service';

@Injectable()
export class SharedService {
    private _error: BehaviorSubject<string> = new BehaviorSubject<string>("");
    private _message: BehaviorSubject<string> = new BehaviorSubject<string>("");
    private _preregObject: BehaviorSubject<string> = new BehaviorSubject<string>("");
    private _preauthUsername: BehaviorSubject<string> = new BehaviorSubject<string>("");
    private _preauthCID: BehaviorSubject<number> = new BehaviorSubject<number>(1);
    private _registerUserHash: BehaviorSubject<string> = new BehaviorSubject<string>("");
    private _isLoggedIn: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
    private _username: BehaviorSubject<string> = new BehaviorSubject<string>("");
    private _user: BehaviorSubject<User> = new BehaviorSubject<User>(new User());
    private _isWebauthnSupported: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);


    // Public Observables of Behavior Subjects
    public error: Observable<string> = this._error.asObservable();
    public message: Observable<string> = this._message.asObservable();
    public preregObject: Observable<string> = this._preregObject.asObservable();
    public preauthUsername: Observable<string> = this._preauthUsername.asObservable();
    public registerUserHash: Observable<string> = this._registerUserHash.asObservable();
    public isLoggedIn: Observable<boolean> = this._isLoggedIn.asObservable();
    public username: Observable<string> = this._username.asObservable();
    public user: Observable<User> = this._user.asObservable();
    public isWebauthnSupported: Observable<boolean> = this._isWebauthnSupported.asObservable();



    constructor(private _cookieService: CookieService,
        private http: HttpClient) {
        this.getIsLoggedInFromServer();
    }

  getIsLoggedInFromServer() {
        // let restURL = ConstantsService.baseURL + "/poc/fido2/isLoggedIn";
        let restURL = ConstantsService.baseURL + "/sfakma/fido2/isLoggedIn";

        var headers_object = new HttpHeaders().set("Authorization", `Bearer ${localStorage.getItem("jwt_payload")}`);

         let username = this.getPreauthUsername();
         if(username===""){
             username = localStorage.getItem("username");
         }
        let body = { "username": username };
        let _headers = new HttpHeaders({ 'Content-Type': 'application/json' });
        this.http.post(restURL,body, { headers: headers_object })
            .toPromise()
            .then(resp => {
                let responseJSON = JSON.parse(JSON.stringify(resp));
                if (responseJSON.Response.length > 0 && responseJSON.Response != "") {
                    this._username.next(responseJSON.Response);
                    this._isLoggedIn.next(true);
                }
                else
                    this._isLoggedIn.next(false);
            });
    }

    setUser(user: User) {
        this._user.next(user);
    }

    getUser() {
        return this._user.getValue();
    }

    getPreregObject() {
        return this._preregObject.getValue();
    }

    setPreregObject(preregObject: string) {
        this._preregObject.next(preregObject);
    }
    getPreauthCID() {
        return this._preauthCID.getValue();
    }

    setPreauthCID(cid: number) {
        this._preauthCID.next(cid);
    }

    getPreauthUsername() {
        return this._preauthUsername.getValue();
    }

    setPreauthUsername(username: string) {
        this._preauthUsername.next(username);
    }


    getRegisterUserURLHash() {
        return this._registerUserHash.getValue();
    }

    setRegisterUserURLHash(hash: string) {
        return this._registerUserHash.next(hash);
    }

    getIsWebauthnSupported() {
        return this._isWebauthnSupported.getValue();
    }

    setIsWebauthnSupported(isWebauthnSupported: boolean) {
        this._isWebauthnSupported.next(isWebauthnSupported);
    }

    getIsLoggedIn() {
        return this._isLoggedIn.getValue();
    }

    setIsLoggedIn(isLoggedIn: boolean) {
        this._isLoggedIn.next(isLoggedIn);
    }

    getUsername() {
        return this._username.getValue();
    }

    setUsername(username: string) {
        this._username.next(username);
    }

    setError(error: string) {
        this._error.next(error);
    }

    getError() {
        return this._error.getValue();
    }

    setMessage(message: string) {
        this._message.next(message);
    }

    getMessage() {
        return this._message.getValue();
    }

}
