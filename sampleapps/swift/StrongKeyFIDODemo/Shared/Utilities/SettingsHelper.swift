//
//  SettingsHelper.swift
//  StrongKeyFIDODemo (iOS)
//
//  Created by Strongkey Engg on 10/19/21.
//

import SwiftUI

class SettingsBundleHelper {
    
    struct SettingsBundleKeys {
        static let fqdnURL = "fqdn_url"
        static let BuildVersionKey = "build_preference"
        static let AppVersionKey = "version_preference"
    }
    class func checkAndExecuteSettings() {
//        if UserDefaults.standard.bool(forKey: SettingsBundleKeys.Reset) {
//            UserDefaults.standard.set(false, forKey: SettingsBundleKeys.Reset)
//            let appDomain: String? = Bundle.main.bundleIdentifier
//            UserDefaults.standard.removePersistentDomain(forName: appDomain!)
//            // reset userDefaults..
//            // CoreDataDataModel().deleteAllData()
//            // delete all other user data here..
//        }
    }
    
    class func setVersionAndBuildNumber() {
        UserDefaults.standard.set(FidoService.fidoServiceBaseURL, forKey: "fqdn_url")
    }
}
