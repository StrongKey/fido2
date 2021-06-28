export class ConstantsService {

    // The build number corresponding to the github.
    public static buildNumber: number = 2;

    //---------------------------------------------------------------------------------------------------
    //  .d8888b.                     .d888 d8b                                    888      888
    // d88P  Y88b                   d88P"  Y8P                                    888      888
    // 888    888                   888                                           888      888
    // 888         .d88b.  88888b.  888888 888  .d88b.  888  888 888d888  8888b.  88888b.  888  .d88b.
    // 888        d88""88b 888 "88b 888    888 d88P"88b 888  888 888P"       "88b 888 "88b 888 d8P  Y8b
    // 888    888 888  888 888  888 888    888 888  888 888  888 888     .d888888 888  888 888 88888888
    // Y88b  d88P Y88..88P 888  888 888    888 Y88b 888 Y88b 888 888     888  888 888 d88P 888 Y8b.
    // " Y8888P"   "Y88P"  888  888 888    888  "Y88888  "Y88888 888     "Y888888 88888P"  888  "Y8888
    //                                             888
    //                                        Y8b d88P
    //                                         "Y88P"
    //---------------------------------------------------------------------------------------------------

    // The name of the company which owns this application.

    // Application language
    public static language: string = 'en';
    // flag that determines if browser is webauthn supported or not

    public static isWebauthnSupported: boolean = false;

    // self-register property used on FSO demo
    public static isSelfRegisterOn: boolean = true;

    public static isDemo: boolean = false;

    public static isSessionValid: boolean = false;


    //-----------------------------------------------
    //  .d8888b.  888             888    d8b
    //d 88P  Y88b 888             888    Y8P
    // Y88b.      888             888
    // "Y888b.   888888  8888b.  888888 888  .d8888b
    //    "Y88b. 888        "88b 888    888 d88P"
    //      "888 888    .d888888 888    888 888
    //Y88b  d88P Y88b.  888  888 Y88b.  888 Y88b.
    // "Y8888P"   "Y888 "Y888888  "Y888 888  "Y8888P
    //-----------------------------------------------

    // the URL of the application
    public static baseURL: string = window.location.protocol + "//" + window.location.hostname;
    public static sfaURL: string = "https://rsa-demo02.strongkey.com/sfaeco-web/rest"
    public static boaLoginURL: string = window.location.protocol + "//" + window.location.hostname+"/boa/#/login";
}
