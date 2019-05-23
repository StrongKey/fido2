import { Component, OnInit, ViewEncapsulation, NgZone } from '@angular/core';
import { Router } from "@angular/router";
import swal from 'sweetalert2';

@Component({
    selector: '.m-wrapper',
    templateUrl: './not-found.component.html',
    encapsulation: ViewEncapsulation.None,
})
export class NotFoundComponent implements OnInit {

    constructor(private zone: NgZone, private _router: Router, ) {
    }

    ngOnInit() {
        let that = this;
        swal({
            type: 'error',
            title: 'Oops...',
            text: 'The requested page does not exist.',
            confirmButtonText: 'Go To Homepage',
            confirmButtonColor: "#00C4D9"
        }).then(function() {
            // window.location.href = "/";
            that.zone.run(() => that._router.navigateByUrl("/login"));
        });
    }
}
