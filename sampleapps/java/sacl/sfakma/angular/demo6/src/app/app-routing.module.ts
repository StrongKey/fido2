import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { LogoutComponent } from "./auth/logout/logout.component";
import { FIDOComponent } from "./auth/fido/fido.component";
import { RegisterComponent } from "./auth/register/register.component";
import { AuthGuard } from "./auth/_guards/auth.guard";
import { AuthComponent } from './auth/auth.component';

const routes: Routes = [
    // { path: '', redirectTo: 'login', pathMatch: 'full', canActivate: [AuthGuard] },
    { path: '', redirectTo: 'login', pathMatch: 'full' },
    { path: 'login', component: AuthComponent },
    { path: 'fido/:action', component: FIDOComponent },
    { path: 'logout', component: LogoutComponent },
    { path: 'register/:hash', component: RegisterComponent },
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule { }
