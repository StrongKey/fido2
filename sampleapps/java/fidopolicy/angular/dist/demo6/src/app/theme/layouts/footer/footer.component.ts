import { Component, OnInit, ViewEncapsulation } from '@angular/core';
import { Helpers } from '../../../helpers';


@Component({
    selector: "app-footer",
    templateUrl: "./footer.component.html",
    encapsulation: ViewEncapsulation.None,
    styles: [`
        .m-footer{
            margin-left: 0px !important;
        }
      `]
})
export class FooterComponent implements OnInit {


    constructor() {

    }
    ngOnInit() {

    }

}
