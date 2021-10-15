import { Pipe, PipeTransform } from '@angular/core';
import { ConstantsService } from '../_services/constants.service';
import { TranslationService } from './'; // our translate service

@Pipe({
    name: 'translate',
    pure: false // impure pipe, update value when we change language
})

export class TranslatePipe implements PipeTransform {

    constructor(private _translate: TranslationService) {
        _translate.use(ConstantsService.language);
    }

    transform(value: string, args: any[]): any {
        if (!value) return;

        return this._translate.instant(value);
    }
}
