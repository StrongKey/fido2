import { InjectionToken } from '@angular/core';

// import translations
import { LANG_EN_NAME, LANG_EN_TRANS } from './lang-en';
import { LANG_JP_NAME, LANG_JP_TRANS } from './lang-jp';
// import { LANG_ZH_NAME, LANG_ZH_TRANS } from './lang-zh';

// translation token
export const TRANSLATIONS = new InjectionToken('translations');

// all traslations
export const dictionary = {
    [LANG_EN_NAME]: LANG_EN_TRANS,
    [LANG_JP_NAME]: LANG_JP_TRANS,
    // [LANG_ZH_NAME]: LANG_ZH_TRANS,
};

// providers
export const TRANSLATION_PROVIDERS = [
    { provide: TRANSLATIONS, useValue: dictionary },
];
