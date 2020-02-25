/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 * ************************************************
 *
 *  888b    888          888
 *  8888b   888          888
 *  88888b  888          888
 *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
 *  888  Y88888 888  888 888    88888888 "Y8888b.
 *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
 *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * ************************************************
 *
 * Test class for JUnit tests.
 * This is the class to be run in netbeans (or any IDE) and this class will
 * in turn runs all other test cases from other classes.
 *
 * The test calls that are part of this test suite are made to the web-services
 * hosted at the host url specified by "fido.cfg.property.junit.fidourl.*" property in
 * <skcews-home>/etc/skcews-configuration.properties file.
 *
 */
package com.strongauth.fido.u2f.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/*******************************************************************************
8888888b.  888     888 888b    888     88888888888 888    888 8888888  .d8888b.      8888888888 8888888 888      8888888888
888   Y88b 888     888 8888b   888         888     888    888   888   d88P  Y88b     888          888   888      888
888    888 888     888 88888b  888         888     888    888   888   Y88b.          888          888   888      888
888   d88P 888     888 888Y88b 888         888     8888888888   888    "Y888b.       8888888      888   888      8888888
8888888P"  888     888 888 Y88b888         888     888    888   888       "Y88b.     888          888   888      888
888 T88b   888     888 888  Y88888         888     888    888   888         "888     888          888   888      888
888  T88b  Y88b. .d88P 888   Y8888         888     888    888   888   Y88b  d88P     888          888   888      888
888   T88b  "Y88888P"  888    Y888         888     888    888 8888888  "Y8888P"      888        8888888 88888888 8888888888

 *******************************************************************************/

@RunWith(Suite.class)
@Suite.SuiteClasses({
//    com.strongauth.fido.u2f.tests.U2F_SOAP_RegistrationTest.class,
//    com.strongauth.fido.u2f.tests.U2F_REST_AuthenticationTest.class,
//    com.strongauth.fido.u2f.tests.U2F_REST_RegistrationTest.class,
//                     com.strongauth.fido.u2f.tests.U2F_SOAP_AuthenticationTest.class
                     })

public class U2F_TestSuite_RUNTHIS {

}
