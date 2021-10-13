import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Routes, RouterModule } from '@angular/router';
import { ProfileComponent } from './profile.component';
import { LayoutModule } from '../../../layouts/layout.module';
import { DefaultComponent } from '../default.component';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MaterialModule } from '../../../../auth/material/material.module';
import {MatTableModule} from '@angular/material/table';


const routes: Routes = [
    {
        "path": "",
        "component": DefaultComponent,
        "children": [
            {
                "path": "",
                "component": ProfileComponent
            }
        ]
    }
];
@NgModule({
    imports: [
        CommonModule,
        RouterModule.forChild(routes),
        LayoutModule,
        FormsModule,
        MaterialModule,
        FlexLayoutModule,
        MatTableModule
    ], exports: [
        RouterModule,

    ], declarations: [
        ProfileComponent
    ]
})
export class ProfileModule {



}
