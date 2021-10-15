/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

import com.strongkey.skfsclient.common.Constants;
import com.strongkey.skfsclient.impl.rest.RestCreateFidoPolicy;
import com.strongkey.skfsclient.impl.rest.RestFidoActionsOnConfiguration;
import com.strongkey.skfsclient.impl.rest.RestFidoActionsOnKey;
import com.strongkey.skfsclient.impl.rest.RestFidoActionsOnPolicy;
import com.strongkey.skfsclient.impl.rest.RestFidoAuthenticate;
import com.strongkey.skfsclient.impl.rest.RestFidoAuthorize;
import com.strongkey.skfsclient.impl.rest.RestFidoGetConfiguration;
import com.strongkey.skfsclient.impl.rest.RestFidoGetKeysInfo;
import com.strongkey.skfsclient.impl.rest.RestFidoGetPolicyInfo;
import com.strongkey.skfsclient.impl.rest.RestFidoPing;
import com.strongkey.skfsclient.impl.rest.RestFidoRegister;
import com.strongkey.skfsclient.impl.soap.SoapFidoActionsOnKey;
import com.strongkey.skfsclient.impl.soap.SoapFidoAuthenticate;
import com.strongkey.skfsclient.impl.soap.SoapFidoAuthorize;
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
                     + "| CP (createpolicy) | PP (updatepolicy) | DP (deletepolicy) | GP (getpolicy)\n"
                     + "| GC (getconfiguration) | UC (updateconfiguration) | DC (deleteconfiguration)\n"
                     + "       java -jar skfsclient.jar R <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <username> <origin> <crossorigin>\n"
                     + "       java -jar skfsclient.jar A <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <username> <origin> <authcounter> <crossorigin>\n"
                     + "       java -jar skfsclient.jar AZ <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <username> <txid> <txpayload> <origin> <authcounter> <crossorigin> <verify>\n"
                     + "       java -jar skfsclient.jar G <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <username>\n"
                     + "       java -jar skfsclient.jar U <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <random-id> <displayname> <status>\n"
                     + "       java -jar skfsclient.jar D <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <random-id>\n"
                     + "       java -jar skfsclient.jar P <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ]\n"
                     + "       java -jar skfsclient.jar CP <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <status> <notes> <policy>\n"
                     + "       java -jar skfsclient.jar PP <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <sid> <pid> <status> <notes> <policy>\n"
                     + "       java -jar skfsclient.jar DP <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <sid> <pid>\n"
                     + "       java -jar skfsclient.jar GP <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <metatdataonly> <sid> <pid>\n"
                     + "       java -jar skfsclient.jar GC <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ]\n"
                     + "       java -jar skfsclient.jar UC <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <configkey> <configvalue> [<notes>]\n"
                     + "       java -jar skfsclient.jar DC <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <configkey>\n"
                     + "       java -jar skfsclient.jar UU <hostport> <did> <wsprotocol> <authtype> [ <accesskey> <secretkey> | <svcusername> <svcpassword> ] <oldusername> <newusername>\n\n"
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
                     + "         crossorigin         : Boolean that will determine if client data allows crossorigin or not - to be used for the simulator\n"
                     + "         authcounter         : Auth counter to be used by the fido client simulator\n"
                     + "         txid                : Unique identifier for the transaction (Base64URLSafe Strong)\n"
                     + "         txpayload           : Transaction payload to be used to generate the challenge for transaction authorization (Base64URLSafe Strong)\n"
                     + "         random-id           : String associated to a specific fido key registered to a\n"
                     + "                                 specific user. This is needed to perform actions on the key like\n"
                     + "                                 de-activate, activate and deregister.\n"
                     + "                                 Random-id can be obtained by calling 'G' option.\n"
                     + "         good/bad signature  : Optional; boolean value that simulates emiting good/bad signatures\n"
                     + "                                 true for good signature | false for bad signature\n"
                     + "                                 default is true\n"
                     + "         start-date          : Unix Timestamp (in milliseconds) when the policy should take effect\n"
                     + "         end-date            : Unix Timestamp (in milliseconds) when the policy should end. Can be \"null\"\n"
                     + "         cert-profile-name   : A human readable name for the policy\n"
                     + "         verify              : Verify the authorization once again once we receive the response (Boolean value)\n"
                     + "         version             : Version of the policy (currently only value of 1 is accepted)\n"
                     + "         status              : Active/Inactive. Status to set the key or policy to.\n"
                     + "         notes               : Optional notes to store with the policy or configuration.\n"
                     + "         policy              : A JSON object defining the FIDO2 policy.\n"
                     + "         sid                 : Server ID: Policy identifier returned by creating a policy.\n"
                     + "         pid                 : Policy ID: Policy identifier returned by creating a policy.\n"
                     + "         metadataonly        : Boolean. If true, returns only the metadata of the policy. If false, returns the metadata + the policy JSON.\n"
                     + "         configkey           : Configuration identifier of server setting.\n"
                     + "         configvalue         : Value connected to configuration identifier.\n"
                     + "         oldusername         : Existing username for a user.\n"
                     + "         newusername         : New username for a user.\n";

        // Used for R, A, G, U, D, P commands only
        String command;
        String hostport;
        int did;
        String wsprotocol;
        String authtype;
        String credential1;
        String credential2;
        String username;
        String txid;
        String txpayload;
        String randomid;
        String displayname;
        String status;
        String origin;
        String crossOrigin;
        int auth_counter;
        String configkey;
        String configvalue;
        String notes;
        String verifyAuthz;
        String oldusername;
        String newusername;

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

            switch (command.toUpperCase()) {

                case Constants.COMMANDS_REGISTER:
                    if (args.length != 10) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    username    = args[7];
                    origin      = args[8];
                    crossOrigin = args[9];

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoRegister.register(hostport, did, authtype, credential1, credential2, username, origin, crossOrigin);
                    } else {
                        SoapFidoRegister.register(hostport, did, authtype, credential1, credential2, username, origin, crossOrigin);
                    }

                    System.out.println("\nDone with Register!\n");
                    break;

                case Constants.COMMANDS_AUTHENTICATE:
                    if (args.length != 11) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    username        = args[7];
                    origin          = args[8];
                    auth_counter    = Integer.parseInt(args[9]);
                    crossOrigin          = args[10];
                    
                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoAuthenticate.authenticate(hostport, did, authtype, credential1, credential2, username, origin, auth_counter, crossOrigin);
                    } else {
                        SoapFidoAuthenticate.authenticate(hostport, did, authtype, credential1, credential2, username, origin, auth_counter, crossOrigin);
                    }

                    System.out.println("\nDone with Authenticate!\n");
                    break;
                    
                case Constants.COMMANDS_AUTHORIZE:
                    if (args.length != 14) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    username        = args[7];
                    txid            = args[8];
                    txpayload       = args[9];
                    origin          = args[10];
                    auth_counter    = Integer.parseInt(args[11]);
                    crossOrigin     = args[12];
                    verifyAuthz     = args[13];

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoAuthorize.authorize(hostport, did, authtype, credential1, credential2, username, txid, txpayload, origin, auth_counter, verifyAuthz, crossOrigin);
                    } else {
                        SoapFidoAuthorize.authorize(hostport, did, authtype, credential1, credential2, username, txid, txpayload, origin, auth_counter, verifyAuthz, crossOrigin);
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

                case Constants.COMMANDS_CREATE_POLICY:
                    if (args.length != 10) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)){
                        System.out.println("HMAC authentication not implemented for this webservice");
                        break;
                    }

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestCreateFidoPolicy.create(args[1], args[2], args[4], args[5], args[6], args[7], args[8], args[9]);
                    } else {
                        //TODO: Soap create policy method call 
                        System.out.println("Not yet implemented");
                    }

                    System.out.println("\nDone with Create!\n");
                    break;

                case Constants.COMMANDS_PATCH_POLICY:
                    if (args.length != 12) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)){
                        System.out.println("HMAC authentication not implemented for this webservice");
                        break;
                    }

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoActionsOnPolicy.patch(args[1], args[2], args[4], args[5], args[6], args[7],args[8], args[9], args[10], args[11]);
                    } else {
                        //TODO: Soap patch policy method call 
                        System.out.println("Not yet implemented");
                    }

                    System.out.println("\nDone with patch!\n");
                    break;

                case Constants.COMMANDS_DELETE_POLICY:
                    if (args.length != 9) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)){
                        System.out.println("HMAC authentication not implemented for this webservice");
                        break;
                    }
                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoActionsOnPolicy.delete(args[1], args[2], args[4], args[5], args[6],args[7],args[8]);
                    } else {
                        //TODO: Soap delete policy method call 
                        System.out.println("Not yet implemented");
                    }
                    
                    System.out.println("\nDone with delete!\n");
                    break;

                case Constants.COMMANDS_GET_POLICY:
                    if (args.length != 10) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    String metadataonly = args[7];
                    if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)){
                        System.out.println("HMAC authentication not implemented for this webservice");
                        break;
                    }
                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoGetPolicyInfo.getPolicyInfo(hostport, args[2], authtype, credential1, credential2, metadataonly, args[8], args[9]);
                    } else {
                        //TODO: Soap get policy method call 
                        System.out.println("Not yet implemented");
                    }
                    
                    System.out.println("\nDone with get policy!\n");
                    break;
                    
                case Constants.COMMANDS_GET_CONFIGURATION:
                    if (args.length != 7) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)){
                        System.out.println("HMAC authentication not implemented for this webservice");
                        break;
                    }

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoGetConfiguration.getConfiguration(hostport, did, authtype, credential1, credential2);
                    } else {
//                        SoapFidoGetConfiguration.getConfiguration(hostport, did, authtype, credential1, credential2);
                        System.out.println("Not yet implemented");
                    }

                    System.out.println("\nDone with Get Configuration!\n");
                    break;
                    
                case Constants.COMMANDS_UPDATE_CONFIGURATION:
                    // adding a note to the configuration is optional
                    if (args.length != 9 && args.length != 10) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    configkey   = args[7];
                    configvalue = args[8];
                    if (args.length == 10) {
                        notes   = args[9];
                    } else {
                        notes   = "";
                    }
                    if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)){
                        System.out.println("HMAC authentication not implemented for this webservice");
                        break;
                    }

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoActionsOnConfiguration.update(hostport, did, authtype, credential1, credential2, configkey, configvalue, notes);
                    } else {
//                        SoapFidoActionsOnConfiguration.update(hostport, did, authtype, credential1, credential2, configkey, configvalue, notes);
                        System.out.println("Not yet implemented");
                    }

                    System.out.println("\nDone with Update Configuration!\n");
                    break;
                    
                case Constants.COMMANDS_DELETE_CONFIGURATION:
                    if (args.length != 8) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)){
                        System.out.println("HMAC authentication not implemented for this webservice");
                        break;
                    }
                    configkey   = args[7];

                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoActionsOnConfiguration.delete(hostport, did, authtype, credential1, credential2, configkey);
                    } else {
//                        SoapFidoActionsOnConfiguration.delete(hostport, did, authtype, credential1, credential2, configkey);
                        System.out.println("Not yet implemented");
                    }

                    System.out.println("\nDone with Delete Configuration!\n");
                    break;

                case Constants.COMMANDS_UPDATE_USERNAME:
                    if (args.length != 9) {
                        System.out.println("Missing arguments...\n" + usage);
                        break;
                    }
                    if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)){
                        System.out.println("HMAC authentication not implemented for this webservice");
                        break;
                    }
                    oldusername = args[7];
                    newusername = args[8];
                    if (wsprotocol.equalsIgnoreCase(Constants.PROTOCOL_REST)) {
                        RestFidoActionsOnKey.updateUsername(hostport, did, authtype, credential1, credential2, oldusername, newusername);
                    } else {
//                        SoapFidoActionsOnConfiguration.delete(hostport, did, authtype, credential1, credential2, configkey);
                        System.out.println("Not yet implemented");
                    }

                    System.out.println("\nDone with Update Username!\n");
                    break;
                default:
                    System.out.println("Invalid Command...\n" + usage);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Missing arguments...\n" + usage);
        }
    }
}
