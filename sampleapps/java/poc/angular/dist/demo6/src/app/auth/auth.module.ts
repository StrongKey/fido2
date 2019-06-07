import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BaseRequestOptions, HttpModule } from '@angular/http';
import { Routes, RouterModule } from '@angular/router';
import { AuthRoutingModule } from './auth-routing.routing';
import { AuthComponent } from './auth.component';
import { LogoutComponent } from './logout/logout.component';
import { FIDOComponent } from './fido/fido.component';
import { AuthGuard } from './_guards/auth.guard';
import { RegisterComponent } from './register/register.component';
import { SweetAlert2Module } from '@toverux/ngx-sweetalert2';


const routes: Routes = [
];

@NgModule({
    declarations: [
        AuthComponent,
        LogoutComponent,
        FIDOComponent,
        RegisterComponent,
    ],
    imports: [
        CommonModule,
        FormsModule,
        HttpModule,
        AuthRoutingModule,
        RouterModule.forChild(routes),
        SweetAlert2Module.forRoot(),
    ]
})

export class AuthModule {
}
