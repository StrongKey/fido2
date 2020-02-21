/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

import com.strongkey.skfsclient.common.Constants;
import com.strongkey.skfsclient.impl.rest.RestFidoActionsOnKey;
import com.strongkey.skfsclient.impl.rest.RestFidoAuthenticate;
import com.strongkey.skfsclient.impl.rest.RestFidoGetKeysInfo;
import com.strongkey.skfsclient.impl.rest.RestFidoPing;
import com.strongkey.skfsclient.impl.rest.RestFidoRegister;
import com.strongkey.skfsclient.impl.soap.SoapFidoActionsOnKey;
import com.strongkey.skfsclient.impl.soap.SoapFidoAuthenticate;
import com.strongkey.skfsclient.impl.soap.SoapFidoGetKeysInfo;
import com.strongkey.skfsclient.impl.soap.SoapFidoPing;
import com.strongkey.skfsclient.impl.soap.SoapFidoRegister;
import java.util.Calendar;

public class FidoEngine {

    public static void main(String[] args) throws Exception {

        System.out.println();
        System.out.println("Copyright (c) 2001-"+Calendar.getInstance().get(Calendar.YEAR)+" StrongAuth, Inc. All rights reserved.");
        System.out.println();

        String usage =
                       "Command: R (registration) | A (authentication) | G (getkeysinfo) | U (updatekey) | D (deregister) | P (ping)\n"
//                     + "| CP (createpolicy) | PP (updatepolicy) | DP (deletepolicy) | gp (getpolicy)\n"
                     + "       java -jar skfsclient.jar R <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <username> <origin>\n"
                     + "       java -jar skfsclient.jar A <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <username> <origin> <authcounter>\n"
                     + "       java -jar skfsclient.jar G <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <username>\n"
                     + "       java -jar skfsclient.jar U <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <random-id> <displayname> <Active/Inactive>\n"
                     + "       java -jar skfsclient.jar D <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <random-id>\n"
                     + "       java -jar skfsclient.jar P <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ]\n"
//                     + "       java -jar skfsclient.jar CP <hostport> <did> <accesskey> <secretkey> <start-date> <end-date> <cert-profile-name> <version> <status> <notes> <policy>\n"
//                     + "       java -jar skfsclient.jar PP <hostport> <did> <accesskey> <secretkey> <sid-pid> <start-date> <end-date> <version> <status> <notes> <policy>\n"
//                     + "       java -jar skfsclient.jar DP <hostport> <did> <accesskey> <secretkey> <sid-pid>\n"
//                     + "       java -jar skfsclient.jar GP <hostport> <did> <accesskey> <secretkey> <metatdataonly> <sid-pid>\n\n"
                     + "Acceptable Values:\n"
                     + "         hostport            : host and port to access the fido \n"
                     + "                                 SOAP & REST format : http://<FQDN>:<non-ssl-portnumber> or \n"
                     + "                                                      https://<FQDN>:<ssl-portnumber>\n"
                     + "                                 example            : https://fidodemo.strongauth.com:8181\n"
                     + "         did                 : Unique domain identifier that belongs to SKCE\n"
                     + "         wsprotocol          : Web service protocol; example REST | SOAP\n"
                     + "         authtype            : Authorization type; example HMAC | PASSWORD\n"
                     + "         accesskey           : Access key for use in identifying a secret key\n"
                     + "         secretkey           : Secret key for HMACing a request\n"
                     + "         svcusername         : Username used for PASSWORD-based authorization\n"
                     + "         svcpassword         : Password used for PASSWORD-based authorization\n"
                     + "         username            : Username for registration, authentication, or getting keys info\n"
                     + "         command             : R (registration) | A (authentication) | G (getkeysinfo) | U (updatekeyinfo) | D (deregister) | P (ping)\n"
                     + "         origin              : Origin to be used by the fido client simulator\n"
                     + "         authcounter         : Auth counter to be used by the fido client simulator\n"
                     + "         random-id           : String associated to a specific fido key registered to a\n"
                     + "                                 specific user. This is needed to perform actions on the key like\n"
                     + "                                 de-activate, activate and deregister.\n"
                     + "                                 Random-id can be obtained by calling 'G' option.\n"
                     + "         Active/Inactive     : Status to set the fido-key to.\n";
//                     + "         good/bad signature  : Optional; boolean value that simulates emiting good/bad signatures\n"
//                     + "                                 true for good signature | false for bad signature\n"
//                     + "                                 default is true\n"
//                     + "         start-date          : Unix Timestamp (in milliseconds) when the policy should take effect\n"
//                     + "         end-date            : Unix Timestamp (in milliseconds) when the policy should end. Can be \"null\"\n"
//                     + "         cert-profile-name   : A human readable name for the policy\n"
//                     + "         version             : Version of the policy (currently only value of 1 is accepted)\n"
//                     + "         status              : Active/Inactive. Status to set the policy to.\n"
//                     + "         notes               : Optional notes to store with the policy.\n"
//                     + "         policy              : A JSON object defining the FIDO2 policy.\n"
//                     + "         sid-pid             : Policy identifier returned by creating a policy.\n"
//                     + "         metadataonly        : Boolean. If true, returns only the metadata of the policy. If false, returns the metadata + the policy JSON.\n";

        // Used for R, A, G, U, D, P commands only
        String command;
        String hostport;
        int did;
        String wsprotocol;
        String authtype;
        String credential1;
        String credential2;
        String username;
        String randomid;
        String displayname;
        String status;
        String origin;
        int auth_counter;

        try {
            if (args.length == 0) {
                System.out.println(usage);
                return;
            } else if (args.length < 7) { // min length for commands: R, A, G, U, D, P
                System.out.println("Missing arguments...\n" + usage);
                return;
            } else {
                command = args[0];
                hostport = args[1];
                did = Integer.parseInt(args[2]);
                wsprotocol = args[3];
                authtype = args[4];
                credential1 = args[5];
                credential2 = args[6];
            }
            if (!wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)
                    && !wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_SOAP)) {
                System.out.println("Invalid wsprotocol...\n" + usage);
                return;
            } else if (!authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)
                    && !authtype.equalsIgnoreCase(Constants.AUTHORIZATION_PASSWORD)) {
                System.out.println("Invalid authtype...\n" + usage);
            }

            switch (command) {

                case Constants.COMMANDS_REGISTER:
                    if (args.length != 9) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    username    = args[7];
                    origin      = args[8];

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoRegister.register(hostport, did, authtype, credential1, credential2, username, origin);
                    } else {
                        SoapFidoRegister.register(hostport, did, authtype, credential1, credential2, username, origin);
                    }

                    System.out.println("\nDone with Register!\n");
                    break;

                case Constants.COMMANDS_AUTHENTICATE:
                    if (args.length != 10) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    username        = args[7];
                    origin          = args[8];
                    auth_counter    = Integer.parseInt(args[9]);

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoAuthenticate.authenticate(hostport, did, authtype, credential1, credential2, username, origin, auth_counter);
                    } else {
                        SoapFidoAuthenticate.authenticate(hostport, did, authtype, credential1, credential2, username, origin, auth_counter);
                    }

                    System.out.println("\nDone with Authenticate!\n");
                    break;

                case Constants.COMMANDS_GETKEYSINFO:
                    if (args.length != 8) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    username    = args[7];

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoGetKeysInfo.getKeysInfo(hostport, did, authtype, credential1, credential2, username);
                    } else {
                        SoapFidoGetKeysInfo.getKeysInfo(hostport, did, authtype, credential1, credential2, username);
                    }

                    System.out.println("\nDone with GetKeysInfo!\n");
                    break;

                case Constants.COMMANDS_UPDATE:
                    if (args.length != 10) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    randomid    = args[7];
                    displayname = args[8];
                    status      = args[9];

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoActionsOnKey.update(hostport, did, authtype, credential1, credential2, randomid, displayname, status);
                    } else {
                        SoapFidoActionsOnKey.update(hostport, did, authtype, credential1, credential2, randomid, displayname, status);
                    }

                    System.out.println("\nDone with Update!\n");
                    break;

                case Constants.COMMANDS_DEREGISTER:
                    if (args.length != 8) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    randomid    = args[7];

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoActionsOnKey.deregister(hostport, did, authtype, credential1, credential2, randomid);
                    } else {
                        SoapFidoActionsOnKey.deregister(hostport, did, authtype, credential1, credential2, randomid);
                    }

                    System.out.println("\nDone with Deregister!\n");
                    break;

                case Constants.COMMANDS_PING:
                    if (args.length != 7) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoPing.ping(hostport, did, authtype, credential1, credential2);
                    } else {
                        SoapFidoPing.ping(hostport, did, authtype, credential1, credential2);
                    }

                    System.out.println("\nDone with Ping!\n");
                    break;

//                case Constants.COMMANDS_CREATE_POLICY:
//                    if (args.length != 12) {
//                        System.out.println("Missing arguments...\n" + usage);
//                        break;
//                    }
//
//                    Long enddate = null;
//                    if (!args[6].equalsIgnoreCase("null"))
//                         enddate = Long.parseLong(args[6]);
//                    RestCreateFidoPolicy.create(args[1], args[2], args[3], args[4], Long.parseLong(args[5]), enddate, args[7], Integer.parseInt(args[8]), args[9], args[10], args[11]);
//                    System.out.println("\nDone with Create!\n");
//                    break;
//
//                case Constants.COMMANDS_PATCH_POLICY:
//                    if (args.length != 12) {
//                        System.out.println("Missing arguments...\n" + usage);
//                        break;
//                    }
//
//                    enddate = null;
//                    if (!args[7].equalsIgnoreCase("null"))
//                         enddate = Long.parseLong(args[7]);
//                    RestFidoActionsOnPolicy.patch(args[1], args[2], args[3], args[4], args[5], Long.parseLong(args[6]), enddate, Integer.parseInt(args[8]), args[9], args[10], args[11]);
//                    System.out.println("\nDone with patch!\n");
//                    break;
//
//                case Constants.COMMANDS_DELETE_POLICY:
//                    if (args.length != 6) {
//                        System.out.println("Missing arguments...\n" + usage);
//                        break;
//                    }
//
//                    RestFidoActionsOnPolicy.delete(args[1], args[2], args[3], args[4], args[5]);
//                    System.out.println("\nDone with delete!\n");
//                    break;
//
//                case Constants.COMMANDS_GET_POLICY:
//                    if (args.length != 7) {
//                        System.out.println("Missing arguments...\n" + usage);
//                        break;
//                    }
//
//                    RestFidoGetPolicyInfo.getPolicyInfo(args[1], args[2], args[3], args[4], args[5], args[6]);
//                    System.out.println("\nDone with get policy!\n");
//                    break;

                default:
                    System.out.println("Invalid Command...\n" + usage);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Missing arguments...\n" + usage);
        }
    }
}
