import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LogoutComponent } from "./auth/logout/logout.component";
import { FIDOComponent } from "./auth/fido/fido.component";
import { RegisterLoginComponent } from './auth/registerAndLogin/registerLogin.component'

const routes: Routes = [
    { path: '', redirectTo: 'registerAndLogin', pathMatch: 'full' },
    { path: 'registerAndLogin', component: RegisterLoginComponent },
    { path: 'fido/:action/:status', component: FIDOComponent },
    { path: 'logout', component: LogoutComponent },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
