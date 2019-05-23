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
                "loadChildren": ".\/pages\/default\/profile\/profile.module#ProfileModule",
                "canActivate": [AuthGuard],
            },
            {
                "path": "dashboard",
                "loadChildren": ".\/pages\/default\/dashboard\/dashboard.module#DashboardModule",
                "canActivate": [AuthGuard],
            },
            {
                "path": "404",
                "loadChildren": ".\/pages\/default\/not-found\/not-found.module#NotFoundModule",
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
