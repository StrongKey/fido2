# Proof of Concept (PoC) Java Application
This project is a service provider web application written in JavaScript and Java to work with StrongKey's [FIDO Certified FIDO2 Server, Community Edition](https://github.com/StrongKey/fido2).

Web application developers worldwide face multiple challenges in the near future: learning about FIDO2, coding in FIDO2, demonstrating to decision makers what FIDO2 can do for their company, and acquiring budgets and resources to transition to FIDO2 strong authentication. Unless you spend many weeks (or months) understanding how FIDO2 works, addressing all these challenges remains daunting.

StrongKey has released this project to the open-source community to address these challenges. The FIDO2 Server allows developers to do the following:

- Setup a FIDO2-enabled single-page web application that can run unmodified and demonstrate FIDO2 registration, authentication, and some simple FIDO2 key management on the client side
- Substitute the stock graphics and logo with your own company's graphics and logo without additional programming&mdash;just replace the graphic image files and reload the application; this allows you to demonstrate to peers and management what FIDO2 can do for the company, and how the user experience (UX) might look in its most basic form
- Learn how FIDO2 works; all the code is available here in a web application framework
- Use the FIDO-certified, open-source FIDO2 server with your web application without having to anticipate deployment issues&mdash;you will have already deployed this FIDO2 Server proof of concept

While this web application can show you how to use W3C's WebAuthn (a subset of the FIDO2 specification) JavaScript, it is also intended to demonstrate how to use FIDO2 protocols with StrongKey's FIDO2 Server to enable strong authentication. Follow the instructions below to install this sample.

## Installation
Follow [the installation instructions](docs/Installation_Guide_Linux.md) to download and install the latest POC application.

## Upgrade
Follow [the upgrade instructions](docs/Upgrade_Guide_Linux.md) to upgrade your current version of poc application to the latest.