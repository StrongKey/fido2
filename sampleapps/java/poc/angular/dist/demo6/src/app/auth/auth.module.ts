import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { Routes, RouterModule } from '@angular/router';
import { AuthRoutingModule } from './auth-routing.routing';
import { AuthComponent } from './auth.component';
import { LogoutComponent } from './logout/logout.component';
import { FIDOComponent } from './fido/fido.component';
import { AuthGuard } from './_guards/auth.guard';
import { RegisterComponent } from './register/register.component';


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
        HttpClientModule,
        AuthRoutingModule,
        RouterModule.forChild(routes),
    ]
})

export class AuthModule {
}
