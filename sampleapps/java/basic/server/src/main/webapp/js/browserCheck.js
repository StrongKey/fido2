 /**  
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */

// internet explorer
var isIE = /*@cc_on!@*/false || !!document.documentMode;

// FIDO support
var hasCredentials = !!window.navigator.credentials;

// ES6 support
var hasClasses = true;

try {
    eval("class ThisClassRequiresES6 {}");
}
catch (e) {
    hasClasses = false;
}

console.log('ie: ' + isIE + ', credentials: ' + hasCredentials + ', classes: ' + hasClasses);
var supportedBrowser = !isIE && hasCredentials && hasClasses;

if (!supportedBrowser) {
    $('.loading-spinner').hide();
    $('#unsupportedBrowser').css('display', 'block');
}
