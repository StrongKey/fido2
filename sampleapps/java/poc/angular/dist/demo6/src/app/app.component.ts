import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Router, NavigationStart, NavigationEnd } from '@angular/router';
import { SharedService } from './_services/shared.service';
import { ConstantsService } from './_services/constants.service';
import { Helpers } from "./helpers";
import swal from 'sweetalert2';
declare let Modernizr: any;

@Component({
    selector: 'body',
    templateUrl: './app.component.html',
    encapsulation: ViewEncapsulation.None,
})
export class AppComponent implements OnInit {
    title = 'app';
    error: string = '';
    message: string = '';
    // isWebauthnSupported: boolean = ConstantsService.isWebauthnSupported;
    //removed  m-aside-left--minimize from the below class
    globalBodyClass = 'm-page--loading-non-block m-page--fluid m--skin- m-content--skin-light2 m-header--fixed m-header--fixed-mobile m-aside-left--enabled m-aside-left--skin-light m-aside-left--fixed m-aside-left--offcanvas m-brand--minimize m-footer--push m-aside--offcanvas-default';

    constructor(private _router: Router,
        private _sharedService: SharedService, ) {
        // use modernizer to check if the browser has the webauthn feature
        console.log("app component");
        let that = this;
        Modernizr.addTest('publicKeyCredential', function() {
            let credentialsContainer: any;
            credentialsContainer = window;
            if (credentialsContainer.PublicKeyCredential) {
                that._sharedService.setIsWebauthnSupported(true);
                return true;
            }
            else {
                that._sharedService.setIsWebauthnSupported(false);
                console.log("ConstantsService.isWebauthnSupported else == " + ConstantsService.isWebauthnSupported);
                return false;
            }
        });
    }

    ngOnInit() {
        this._router.events.subscribe((route) => {
            if (route instanceof NavigationStart) {
                Helpers.setLoading(true);
                Helpers.bodyClass(this.globalBodyClass);
            }
            if (route instanceof NavigationEnd) {
                Helpers.setLoading(false);
            }
        });

        this._sharedService.error.subscribe(resp => {
            if (resp.includes("ERR")) {
                this.error = resp.split(':')[1];
            }
            else {
                this.error = resp;
            }
            if (resp != null && resp.length !== 0 && resp.trim()) {
                swal({
                    position: 'top',
                    type: 'error',
                    title: 'Oops...',
                    html: '<br/>' + this.error + '<br/>',
                    confirmButtonText: 'OK'
                })

            }
        });

        this._sharedService.message.subscribe(resp => {
            if (resp.includes("MSG")) {
                this.message = resp.split(':')[1];
            }
            else {
                this.message = resp;
            }

            if (resp != null && resp.length !== 0 && resp.trim()) {
                swal({
                    position: 'top',
                    type: 'success',
                    title: 'All done!',
                    html: '<br/>' + this.message + '<br/>',
                    confirmButtonText: 'OK'
                })

            }
        });
    }


}
