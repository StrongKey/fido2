import { Component, OnInit, ViewEncapsulation, NgZone, ViewChild } from '@angular/core';
import { Helpers } from '../../../../helpers';
import { Router, ActivatedRoute } from "@angular/router";
import { User } from '../../../../auth/_models/user';
import { RestService } from '../../../../_services/rest.service';
import { SharedService } from '../../../../_services/shared.service';
import { CookieService } from 'ngx-cookie-service';
import { ConstantsService } from "../../../../_services/constants.service";
import Swal from 'sweetalert2';
//import {MatTableModule} from '@angular/material/table'; 
import {Inject} from '@angular/core';
import {MatDialog, MAT_DIALOG_DATA} from '@angular/material';
import { DialogDataExampleDialog } from '../TransactionModal/transactionmodal.component';
import {MatTableDataSource} from '@angular/material/table';
// export interface DialogData {
//   animal: 'panda' | 'unicorn' | 'lion';
// }
export interface UtxFidoAuthenticatorReferences{
        farid: number;
        protocol: string;
        fidoid: string;
        rawid :string;
        rpid: string;
        authenticatorData : string ;
        clientDataJson : string;
        aaguid : string;
        authTime : string;
        up : string;
        uv : string;
        usedForThisTransaction : string;
        fidoSignature : string;
        createDate : string;
        userHandle: string;
        signingKeyType: string;
        signingKeyAlgorithm: string;
        signerPublicKey: string;
        signature: string;
}
export interface TransactionElement{

	did: number;
	sid: number;
	uid: number;
	utxid: number;
	merchantId: string;
	status: string;
	createDate: string;
	modifyDate: string;
	signature: string;
	notes: string;
	totalPrice: number;
	totalProducts: number;
    paymentBrand: string;
    paymentCardNumber: string;
    currency: string;
    txtime: string;
    txid: string;
    txpayload: string;
    nonce: string;
    challenge: string;
    username: string;
	utxfidoauthreferences: UtxFidoAuthenticatorReferences;
}
export interface UtxFidoAuthenticatorReferencesArray{
	utxfidoauthreferencesarray:UtxFidoAuthenticatorReferences[];
}
export interface TransactionArray{

	transactions:TransactionElement
}



@Component({
    selector: "dashboard",
    templateUrl: "./dashboard.component.html",
    encapsulation: ViewEncapsulation.None,
    styles: [`
    
    table{
    width:50%;
    }
    .row-style{
    	cursor:pointer;
    }
    tr:hover {
    background: #FFEBCD;
	}
    `]

})
export class DashboardComponent implements OnInit{
  //displayedColumns: string[] = ['position', 'name', 'weight', 'symbol'];
   ELEMENT_DATA : TransactionElement[]=[];
   multipleTransaction : TransactionElement[]=[];
   singleTransaction : TransactionElement[]=[];

  displayedColumns: string[] = ['did', 'uid', 'utxid','createDate', 'status'];
  //,'fidoSignature','status','createDate','signature'
  dataSource = new MatTableDataSource<TransactionElement>(this.ELEMENT_DATA);

  constructor(private restService: RestService,public dialog: MatDialog) {
    }

    ngOnInit(){

    	let resp = this.restService.getUserTransactions();
    	resp.subscribe(report=> {
        
    		this.multipleTransaction=report as TransactionElement[];
    		this.dataSource.data=report as TransactionElement[];
    	});
    }

    openDialog(row) {
    	console.log(this.multipleTransaction);
    	this.singleTransaction = this.multipleTransaction.filter(transaction=>{
    		return(
    		transaction.did===row.did||
    		transaction.utxid===row.utxid
    	)
    	})

    	console.log(typeof this.singleTransaction);
		this.dialog.open(DialogDataExampleDialog, {data:{
				transactions:this.singleTransaction[0] 
			//row
			// did: 1,
			// sid: 2,
			// uid: 3,
			// utxid: 4,
			// plaintext: "dummy text",
			// fidoSignature: "signature",
			// status: "status",
			// createDate: "12 March",
			// signature: "PJS"
		}});
		
		}


    getRecord(row){
    	console.log(row.name);
    }

}
