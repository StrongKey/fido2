import { NgModule } from '@angular/core';
import { ThemeComponent } from './theme.component';
import { Routes, RouterModule } from '@angular/router';
import { AuthGuard } from "../auth/_guards/auth.guard";

const routes: Routes = [
    {
        "path": "",
        "component": ThemeComponent,
        "children": [
            {
                "path": "profile",
                "loadChildren": () => import('./pages/default/profile/profile.module').then(m => m.ProfileModule),
                "canActivate": [AuthGuard],
            },
            {
                "path": "dashboard",
                "loadChildren": () => import('./pages/default/dashboard/dashboard.module').then(m => m.DashboardModule),
                "canActivate": [AuthGuard],
            },
            {
                "path": "404",
                "loadChildren": () => import('./pages/default/not-found/not-found.module').then(m => m.NotFoundModule),
            },
            {
                "path": "",
                "redirectTo": "login",
                "pathMatch": "full",
            },
        ]
    },
    {
        "path": "login",
        "redirectTo": "login",
        "pathMatch": "full",
    },
    {
        "path": "**",
        "redirectTo": "404",
        "pathMatch": "full",
    }
];

@NgModule({
    imports: [RouterModule.forChild(routes)],
    exports: [RouterModule]
})
export class ThemeRoutingModule { }
