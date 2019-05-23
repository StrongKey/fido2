import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { User } from '../auth/_models/user';
import "rxjs/add/operator/map";
import { CookieService } from 'ngx-cookie-service';
import { Headers, Http, URLSearchParams } from '@angular/http';
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
        private http: Http) {
        this.getIsLoggedInFromServer();
    }

    getIsLoggedInFromServer() {
        let restURL = ConstantsService.baseURL + "/basicserver/fido2";
        let _headers = new Headers({ 'Content-Type': 'application/json' });
        this.http.post(restURL, { headers: _headers })
            .toPromise()
            .then(resp => {
                let responseJSON = resp.json();
                console.log("response JSON in sharedService == " + JSON.stringify(responseJSON));
                console.log("responseJSON response = " + responseJSON.Response);
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
