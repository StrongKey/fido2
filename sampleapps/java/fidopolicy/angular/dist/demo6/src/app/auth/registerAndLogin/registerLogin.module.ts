import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { Routes, RouterModule } from '@angular/router';
import { RegisterLoginRoutingModule } from './registerLogin.routing';
import { RegisterLoginComponent } from './registerLogin.component';
import { LogoutComponent } from '../logout/logout.component';
import { FIDOComponent } from '../fido/fido.component';
import { PrettyjsonpipeModule } from '../pipes/prettyjsonpipe.module';
import { FlexLayoutModule } from '@angular/flex-layout';
import { MaterialModule } from '../material/material.module';



const routes: Routes = [
];

@NgModule({
    declarations: [
        RegisterLoginComponent,
        LogoutComponent,
        FIDOComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        HttpClientModule,
        RegisterLoginRoutingModule,
        RouterModule.forChild(routes),
        PrettyjsonpipeModule,
        FlexLayoutModule,
        MaterialModule,
    ]
})

export class RegisterLoginModule {
}
