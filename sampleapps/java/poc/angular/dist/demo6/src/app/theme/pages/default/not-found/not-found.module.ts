import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Routes, RouterModule } from '@angular/router';
import { LayoutModule } from '../../../layouts/layout.module';
import { DefaultComponent } from '../default.component';
import { NotFoundComponent } from './not-found.component';
import { SweetAlert2Module } from '@toverux/ngx-sweetalert2';

const routes: Routes = [
    {
        'path': '',
        'component': DefaultComponent,
        'children': [
            {
                'path': '',
                'component': NotFoundComponent,
            },
        ],
    },
];

@NgModule({
    imports: [
        CommonModule, RouterModule.forChild(routes), LayoutModule, SweetAlert2Module
    ], exports: [
        RouterModule,
    ], declarations: [
        NotFoundComponent,
    ],
})
export class NotFoundModule {
}