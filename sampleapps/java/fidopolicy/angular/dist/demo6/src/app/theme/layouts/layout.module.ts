import { NgModule } from '@angular/core';
import { DefaultComponent } from '../pages/default/default.component';
import { HeaderNavComponent } from './header-nav/header-nav.component';
import { FooterComponent } from './footer/footer.component';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { HrefPreventDefaultDirective } from '../../_directives/href-prevent-default.directive';
import { UnwrapTagDirective } from '../../_directives/unwrap-tag.directive';

@NgModule({
    declarations: [
        DefaultComponent,
        HeaderNavComponent,
        FooterComponent,
        HrefPreventDefaultDirective,
        UnwrapTagDirective,
    ],
    exports: [
        DefaultComponent,
        HeaderNavComponent,
        FooterComponent,
        HrefPreventDefaultDirective,
    ],
    imports: [
        CommonModule,
        RouterModule,
    ]
})
export class LayoutModule {
}
