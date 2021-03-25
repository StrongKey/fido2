import {Component, Inject, OnInit} from '@angular/core';
import {MatDialog, MAT_DIALOG_DATA} from '@angular/material';
import {TransactionElement,TransactionArray} from '../dashboard/dashboard.component';
import {MatCardModule} from '@angular/material/card';  
import {MatButtonModule} from '@angular/material/button'; 
import {ScrollingModule} from '@angular/cdk/scrolling'; 
import {MatExpansionModule} from '@angular/material/expansion'; 
import {MatDialogModule} from '@angular/material/dialog'; 
import { Helpers } from "../../../../helpers";
import Swal from 'sweetalert2';

/**
 * @title Injecting data when opening a dialog
 */


@Component({
  selector: 'dialog-data-example-dialog',
  templateUrl: 'transactionmodal.component.html',
    styles: [`
    .card-style{
      width: 100%
    }
    .card-content-style{
       display: flex;
       justify-content:space-between; 
    }
    .card-content-space{
       margin-top: 1.5rem !important;
    }
    .indent-class{
      margin-left:25px;
    }
    .modal-value-color{
      color:#013E51;
    }
    `]
})
export class DialogDataExampleDialog implements OnInit{

  singleTransaction : TransactionElement;

  constructor(@Inject(MAT_DIALOG_DATA) public data: TransactionArray,public dialog: MatDialog) {}
  ngOnInit(){
    //const temp=this.data;
    this.singleTransaction=this.data.transactions;
    console.log(this.data.transactions.createDate);
    console.log(this.data);
    }
    disableAnimation = true;
  ngAfterViewInit(): void {
    // timeout required to avoid the dreaded 'ExpressionChangedAfterItHasBeenCheckedError'
    setTimeout(() => this.disableAnimation = false);
  }
  async simulate3ds(){
   const delay = ms => new Promise(res => setTimeout(res, ms));
   Helpers.setLoading(true);
   await delay(2000);
   console.log("Simulating 3ds");
   Helpers.setLoading(false);

   // this.dialog.open(SimulationDialog,{
   //   height: '40%',
   //    width: '520px'
   // });
    Swal.fire({
                    position: 'center',
                    icon: 'success',
                    html: '<br/>' +"Success"+ '<br/>',
                    confirmButtonText: 'OK',
                    footer: 'Note: The message was NOT actually sent over 3DS, but its just a simulation to show how the FIDO Authenticator References can be sent to the Issuing Bank to get an approval' 
                })
        // this.isHidden=false;

}


}

@Component({
  selector: 'simulation-dialog',
  templateUrl: 'simulationdialog.component.html',
  styles:[
  `
    .green-tick-size{
      font-size:28px;
    }
  `]
})
export class SimulationDialog implements OnInit {

    // isHidden:boolean=true;
      async ngOnInit() {
      
     


  }


}
