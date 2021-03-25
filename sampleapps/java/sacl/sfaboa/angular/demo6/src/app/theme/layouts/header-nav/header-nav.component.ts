import { Component, OnInit, ViewEncapsulation, AfterViewInit } from '@angular/core';
import { Helpers } from '../../../helpers';
import { CookieService } from 'ngx-cookie-service';
import { SharedService } from '../../../_services/shared.service';
import { User } from '../../../auth/_models';
import { ConstantsService } from '../../../_services/constants.service';
import { DOCUMENT } from '@angular/common';
import { Inject } from '@angular/core';  
declare let mLayout: any;
@Component({
    selector: "app-header-nav",
    templateUrl: "./header-nav.component.html",
    encapsulation: ViewEncapsulation.None,
     styles: [`
   
    .profile-select{
        cursor:pointer;
    }
   
    `]
})
export class HeaderNavComponent implements OnInit, AfterViewInit {

    username: string;
    email: string;
    user: User = new User();
    showUserMenu = false;
    profileURL: string= ConstantsService.profileURL; 

    constructor(@Inject(DOCUMENT) private document: Document,private sharedService: SharedService,
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
    goToProfile():void{
        console.log(this.profileURL);
         // this.document.location.href = this.profileURL;
         window.open(this.profileURL,'_blank')
    }

}
