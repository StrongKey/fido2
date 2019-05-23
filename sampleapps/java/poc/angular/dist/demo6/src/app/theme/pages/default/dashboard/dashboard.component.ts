import { Component, OnInit, ViewEncapsulation, NgZone, ViewChild } from '@angular/core';
import { Helpers } from '../../../../helpers';
import { Router, ActivatedRoute } from "@angular/router";
import { User } from '../../../../auth/_models/user';
import { RestService } from '../../../../_services/rest.service';
import { SharedService } from '../../../../_services/shared.service';
import { CookieService } from 'ngx-cookie-service';
import { ConstantsService } from "../../../../_services/constants.service";
import swal from 'sweetalert2';


@Component({
    selector: "dashboard",
    templateUrl: "./dashboard.component.html",
    encapsulation: ViewEncapsulation.None,

})
export class DashboardComponent {
}
