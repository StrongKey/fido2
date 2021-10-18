
# StrongKey FIDO Demo iOS/iPadOS/macOS

## About

This is a demo application to show StrongKey FIDO Server (SKFS) capabilities on a rich-client native application running on Apple's iPhone, iPad, and Mac devices.
This project is built using the Model View ViewModel (MVVM) design pattern, and makes use of Apple's latest [Authentication Services](https://developer.apple.com/documentation/authenticationservices) for Passkey APIs

## Features

- Register a new account using Passkey, powered by SKFS
- Login to an account using Passkey or a security key
	- Manage keys
	- Add new keys
	- Delete keys
- View details
- Delete account(s) for testing
- Support for iPhones, iPads, and Macs

## System Requirements

This app is designed to support all iPhones and iPads with iOS 15.0/iPadOS 15.0 and above using the latest SwiftUI 3.0 features.
For macOS this app can run on all Macs with macOS 12.0 and TouchID support in both Native Interface mode or iPad interface using Mac Catalyst.
- iOS 15.0+
- macOS 12.0 and above

## Third-party Libraries

This project does not uses any external libraries. Some extensions are used from `/Utilities`

## Build Requirements

- [Xcode 13.0+](https://developer.apple.com/xcode/)

## Getting Started

You will need a paid Apple Developer account to make use of the Passkey APIs.
 1. Clone or download the source code.
 2. Make sure that your iPhone/iPad/macOS or Simulator has FaceID/TouchID enrolled.
 3. Turn on Syncing Platform Authenticator option for iPhones and iPads. `Settings -> Developer -> Syncing Platform Authenticator`
 4. Build and Run on the target device.
