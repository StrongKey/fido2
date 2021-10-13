import { Component, OnInit, ViewEncapsulation, AfterViewInit } from '@angular/core';
import { Helpers } from '../../../helpers';
import { CookieService } from 'ngx-cookie-service';
import { SharedService } from '../../../_services/shared.service';
import { User } from '../../../auth/_models';

declare let mLayout: any;
@Component({
    selector: "app-header-nav",
    templateUrl: "./header-nav.component.html",
    encapsulation: ViewEncapsulation.None,
})
export class HeaderNavComponent implements OnInit, AfterViewInit {

    username: string;
    email: string;
    user: User = new User();
    showUserMenu = false;

    constructor(private sharedService: SharedService,
        private cookieService: CookieService) {
        sharedService.username.subscribe(resp => {
            console.log("resp of username - subscribe : " + resp);
            this.username = resp;
        });
    }
    ngOnInit() {
    }
    ngAfterViewInit() {
        mLayout.initHeader();
        this.showUserMenu = true;
    }

}
