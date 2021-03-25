import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';
import { DashboardComponent } from './dashboard.component';
import { LayoutModule } from '../../../layouts/layout.module';
import { DefaultComponent } from '../default.component';
import { MatTableModule } from '@angular/material/table';
import {MatDialogModule} from '@angular/material/dialog'; 

const routes: Routes = [
    {
        "path": "",
        "component": DefaultComponent,
        "children": [
            {
                "path": "",
                "component": DashboardComponent
            }
        ]
    }
];
@NgModule({
    imports: [
        CommonModule, RouterModule.forChild(routes), LayoutModule,
        FormsModule,
        MatTableModule,
        MatDialogModule,
    ], exports: [
        RouterModule,

    ], declarations: [
        DashboardComponent
    ]
})
export class DashboardModule {



}
