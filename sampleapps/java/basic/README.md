# Basic Java Sample Application
This project is a basic service provider web application written in Java to work with [StrongKey FIDO Server (SKFS), Community Edition](https://github.com/StrongKey/fido2). This project also includes sample JavaScript files providing a basic user interface to test FIDO2 registration and authentication.

The goals of this project are to demonstrate how to call SKFS APIs and how to properly manage users' FIDO2 keys. It is meant to serve as a reference implementation of a project that leverages SKFS to enable FIDO2 authentication. **If you are an application developer looking to FIDO2-enable an application, this code uses examples of the FIDO2 API calls.**

For additional information on FIDO2, visit the technical specification:

- [Complete WebAuthn specification](https://www.w3.org/TR/webauthn)
- [A useful diagram of WebAuthn functional flow](https://www.w3.org/TR/webauthn/#api)

For more information on the originating jargon and related terms, visit the Internet Engineering Task Force (IETF) Request for Comments (RFC):

- The definition of service provider is in the [second paragraph of 1.1. Background](https://tools.ietf.org/html/rfc3647#section-1.1), described as "relying parties."

## Installation
Follow [the installation instructions](docs/Installation_Guide_Linux.md) to download and install the latest basic demo sample.

## Upgrade
Follow [the upgrade instructions](docs/Upgrade_Guide_Linux.md) to upgrade your current version of basic demo to the latest.
