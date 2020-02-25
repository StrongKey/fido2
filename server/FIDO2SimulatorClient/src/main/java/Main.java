/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 * **********************************************
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
 * **********************************************
 *
 * The main program that drives the simulator client and library
 */

import com.strongauth.skfs.fido2.client.Constants;
import com.strongauth.skfs.fido2.client.RestCreateFidoPolicy;
import com.strongauth.skfs.fido2.client.RestFidoActionsOnKey;
import com.strongauth.skfs.fido2.client.RestFidoActionsOnPolicy;
import com.strongauth.skfs.fido2.client.RestFidoAuthenticate;
import com.strongauth.skfs.fido2.client.RestFidoGetKeysInfo;
import com.strongauth.skfs.fido2.client.RestFidoGetPolicyInfo;
import com.strongauth.skfs.fido2.client.RestFidoRegister;
import java.util.Calendar;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("\nCopyright (c) 2001-"+Calendar.getInstance().get(Calendar.YEAR)+" StrongAuth, Inc. (DBA StrongKey) All rights reserved.\n");

        String usage = "Usage: java -jar Main.jar R <hostport> <did> <accesskey> <secretkey> <fidoprotocol> <username> <origin>\n"
                     + "       java -jar Main.jar A <hostport> <did> <accesskey> <secretkey> <fidoprotocol> <username> <origin> <authcounter>\n"
                     + "       java -jar Main.jar G <hostport> <did> <accesskey> <secretkey> <username> \n"
                     + "       java -jar Main.jar D <hostport> <did> <accesskey> <secretkey> <random-id> \n"
                     + "       java -jar Main.jar U <hostport> <did> <accesskey> <secretkey> <random-id> <Active/Inactive>\n"
                     + "       java -jar Main.jar CP <hostport> <did> <accesskey> <secretkey> <start-date> <end-date> <certificate-profile-name> <version> <status> <notes> <policy>\n"
                     + "       java -jar Main.jar PP <hostport> <did> <accesskey> <secretkey> <sid-pid> <start-date> <end-date> <version> <status> <notes> <policy>\n"
                     + "       java -jar Main.jar DP <hostport> <did> <accesskey> <secretkey> <sid-pid>\n"
                     + "       java -jar Main.jar GP <hostport> <did> <accesskey> <secretkey> <metatdataonly> <sid-pid>\n\n"
                     + "Acceptable Values:\n"
                     + "       hostport            : Host and port to access the fido \n"
                     + "                             SOAP & REST format : http://<FQDN>:<non-ssl-portnumber> or \n"
                     + "                                                  https://<FQDN>:<ssl-portnumber>\n"
                     + "                             example            : https://fidodemo.strongauth.com:8181\n"
                     + "       did                 : Unique domain identifier that belongs to SKCE\n"
                     + "       accesskey           : Access key for use in identifying a secret key\n"
                     + "       secretkey           : Secret key for HMACing a request\n"
                     + "       fidoprotocol        : Fido protocol; example U2F_V2 or FIDO20\n"
                     + "       username            : Username for registration or authentication\n"
                     + "       command             : R  (registration) | A  (authentication) | G  (getkeysinfo) |\n"
                     + "                             DA (deactivate)   | AC (activate)       | DR (deregister) \n"
                     + "       origin              : Origin to be used by the fido client simulator\n"
                     + "       authcounter         : Auth counter to be used by the fido client simulator\n"
                     + "       random-id           : Random-id string associated to a specific fido key registered to a\n"
                     + "                             specific user. This is needed to perform actions on the key like\n"
                     + "                             de-activate, activate and deregister.\n"
                     + "                             Random-id can be obtained by calling 'G' option.\n"
                     + "       Active/Inactive     : Status to set the fido-key to.\n"
                     + "       good/bad signature  : Optional; boolean value that simulates emiting good/bad signatures\n"
                     + "                             true for good signature | false for bad signature\n"
                     + "                             default is true\n";

        try {
            switch (args[0])
            {
                case Constants.COMMANDS_REGISTER:
                    if (args.length != 8) {
                        System.err.println("ERROR: Missing arguments...\n\n" + usage);
                        break;
                    }
                    RestFidoRegister.register(args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
                    System.out.println("\nDone with Register!\n");
                    break;

                case Constants.COMMANDS_AUTHENTICATE:
                    if (args.length != 9) {
                        System.err.println("ERROR: Missing arguments...\n\n" + usage);
                        break;
                    }
                    RestFidoAuthenticate.authenticate(args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
                    System.out.println("\nDone with Authorize!\n");
                    break;

                case Constants.COMMANDS_GETKEYS:
                    if (args.length != 6) {
                        System.err.println("ERROR: Missing arguments...\n\n" + usage);
                        break;
                    }
                    RestFidoGetKeysInfo.getKeysInfo(args[1], args[2], args[3], args[4], args[5]);
                    System.out.println("\nDone with GetKeysInfo!\n");
                    break;

                case Constants.COMMANDS_DEACTIVATE:
                    if (args.length != 6) {
                        System.err.println("ERROR: Missing arguments...\n\n" + usage);
                        break;
                    }
                    RestFidoActionsOnKey.deregister(args[1], args[2], args[3], args[4], args[5]);
                    System.out.println("\nDone with Deactivate!\n");
                    break;

                case Constants.COMMANDS_UPDATE:
                    if (args.length != 7) {
                        System.err.println("ERROR: Missing arguments...\n\n" + usage);
                        break;
                    }
                    RestFidoActionsOnKey.update(args[1], args[2], args[3], args[4], args[5], args[6]);
                    System.out.println("\nDone with Update!\n");
                    break;

                case Constants.COMMANDS_CREATE_POLICY:
                    if (args.length != 12) {
                        System.err.println("ERROR: Missing arguments...\n\n" + usage);
                        break;
                    }
                    RestCreateFidoPolicy.create(args[1], args[2], args[3], args[4], Long.parseLong(args[5]), Long.parseLong(args[6]), args[7], Integer.parseInt(args[8]), args[9], args[10], args[11]);
                    System.out.println("\nDone with Create!\n");
                    break;

                case Constants.COMMANDS_UPDATE_POLICY:
                    if (args.length != 12) {
                        System.err.println("ERROR: Missing arguments...\n\n" + usage);
                        break;
                    }
                    RestFidoActionsOnPolicy.update(args[1], args[2], args[3], args[4], args[5], Long.parseLong(args[6]), Long.parseLong(args[7]), Integer.parseInt(args[8]), args[9], args[10], args[11]);
                    System.out.println("\nDone with patch!\n");
                    break;

                case Constants.COMMANDS_DELETE_POLICY:
                    if (args.length != 6) {
                        System.err.println("ERROR: Missing arguments...\n\n" + usage);
                        break;
                    }
                    RestFidoActionsOnPolicy.delete(args[1], args[2], args[3], args[4], args[5]);
                    System.out.println("\nDone with delete!\n");
                    break;

                case Constants.COMMANDS_GET_POLICY:
                    if (args.length != 7) {
                        System.err.println("ERROR: Missing arguments...\n\n" + usage);
                        break;
                    }
                    RestFidoGetPolicyInfo.getPolicyInfo(args[1], args[2], args[3], args[4], args[5], args[6]);
                    System.out.println("\nDone with get policy!\n");
                    break;

                default:
                    System.err.println("Invalid Command...\n" + usage);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.err.println("ERROR: Missing arguments...\n\n" + usage);
        }
    }
}
