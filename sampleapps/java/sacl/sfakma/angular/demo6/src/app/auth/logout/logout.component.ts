import { Component, OnInit, ViewEncapsulation } from "@angular/core";
import { Router } from "@angular/router";
import { RestService } from '../../_services/rest.service';
import { Helpers } from "../../helpers";

@Component({
    selector: 'app-logout',
    templateUrl: './logout.component.html',
    encapsulation: ViewEncapsulation.None,
})

export class LogoutComponent implements OnInit {

    constructor(private _router: Router,
        private _restService: RestService) {
    }

    ngOnInit(): void {
        Helpers.setLoading(true);
          localStorage.clear();
        this._restService.logout();
        this._router.navigate(['/login']);
        sessionStorage.clear();
    }
}
