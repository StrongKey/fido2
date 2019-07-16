/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */

import com.strongkey.apiclient.common.Constants;
import com.strongkey.apiclient.impl.RestCreateFidoPolicy;
import com.strongkey.apiclient.impl.RestFidoActionsOnKey;
import com.strongkey.apiclient.impl.RestFidoActionsOnPolicy;
import com.strongkey.apiclient.impl.RestFidoAuthenticate;
import com.strongkey.apiclient.impl.RestFidoGetKeysInfo;
import com.strongkey.apiclient.impl.RestFidoGetPolicyInfo;
import com.strongkey.apiclient.impl.RestFidoRegister;
import java.util.Calendar;
import java.util.Date;

public class FidoEngine {

    public static void main(String[] args) throws Exception {

        System.out.println();
        System.out.println("Copyright (c) 2001-"+Calendar.getInstance().get(Calendar.YEAR)+" StrongAuth, Inc. All rights reserved.");
        System.out.println();

        String usage = "Usage: java -jar apiclient.jar R <hostport> <did> <accesskey> <secretkey> <fidoprotocol> <username> <origin>\n"
                     + "       java -jar apiclient.jar A <hostport> <did> <accesskey> <secretkey> <fidoprotocol> <username> <origin> <authcounter>\n"
                     + "       java -jar apiclient.jar G <hostport> <did> <accesskey> <secretkey> <username> \n"
                     + "       java -jar apiclient.jar D <hostport> <did> <accesskey> <secretkey> <random-id> \n"
                     + "       java -jar apiclient.jar U <hostport> <did> <accesskey> <secretkey> <random-id> <Active/Inactive>\n"
                     + "       java -jar apiclient.jar CP <hostport> <did> <accesskey> <secretkey> <start-date> <end-date> <certificate-profile-name> <version> <status> <notes> <policy>\n"
                     + "       java -jar apiclient.jar PP <hostport> <did> <accesskey> <secretkey> <sid-pid> <start-date> <end-date> <version> <status> <notes> <policy>\n"
                     + "       java -jar apiclient.jar DP <hostport> <did> <accesskey> <secretkey> <sid-pid>\n"
                     + "       java -jar apiclient.jar DP <hostport> <did> <accesskey> <secretkey> <metatdataonly> <sid-pid>\n\n"
                     + "Acceptable Values:\n"
                     + "         hostport            : host and port to access the fido \n"
                     + "                                 SOAP & REST format : http://<FQDN>:<non-ssl-portnumber> or \n"
                     + "                                                      https://<FQDN>:<ssl-portnumber>\n"
                     + "                                 example            : https://fidodemo.strongauth.com:8181\n"
                     + "         did                 : Unique domain identifier that belongs to SKCE\n"
                     + "         accesskey           : access key for use in identifying a secret key\n"
                     + "         secretkey           : secret key for HMACing a request\n"
                     + "         fidoprotocol        : fido protocol; example U2F_V2 or FIDO20\n"
                     + "         username            : username for registration or authentication\n"
                     + "         command             : R  (registration) | A  (authentication) | G  (getkeysinfo) |\n"
                     + "                               DA (deactivate)   | AC (activate)       | DR (deregister) \n"
                     + "         origin              : Origin to be used by the fido client simulator\n"
                     + "         authcounter         : Auth counter to be used by the fido client simulator\n"
                     + "         random-id           : random-id string associated to a specific fido key registered to a\n"
                     + "                                 specific user. This is needed to perform actions on the key like\n"
                     + "                                 de-activate, activate and deregister.\n"
                     + "                                 Random-id can be obtained by calling 'G' option.\n"
                     + "         Active/Inactive     : status to set the fido-key to.\n"
                     + "         good/bad signature  : Optional; boolean value that simulates emiting good/bad signatures\n"
                     + "                                 true for good signature | false for bad signature\n"
                     + "                                 default is true\n";

        try {
            switch (args[0]) {

                case Constants.COMMANDS_REG:
                    if (args.length != 8)
                        System.out.println("Missing arguments...\n" + usage);

                    RestFidoRegister.register(args[1], args[2], args[3], args[4], args[5], args[6], args[7]);
                    System.out.println("\nDone with Register!\n");
                    break;

                case Constants.COMMANDS_AUTH:
                    if (args.length != 9)
                        System.out.println("Missing arguments...\n" + usage);

                    RestFidoAuthenticate.authenticate(args[1], args[2], args[3], args[4], args[5], args[6], args[7], args[8]);
                    System.out.println("\nDone with Authorize!\n");
                    break;

                case Constants.COMMANDS_GETKEYS:
                    if (args.length != 6)
                        System.out.println("Missing arguments...\n" + usage);

                    RestFidoGetKeysInfo.getKeysInfo(args[1], args[2], args[3], args[4], args[5]);
                    System.out.println("\nDone with GetKeysInfo!\n");
                    break;

                case Constants.COMMANDS_DEACT:
                    if (args.length != 6)
                        System.out.println("Missing arguments...\n" + usage);

                    RestFidoActionsOnKey.deregister(args[1], args[2], args[3], args[4], args[5]);
                    System.out.println("\nDone with Deactivate!\n");
                    break;

                case Constants.COMMANDS_UP:
                    if (args.length != 7) 
                        System.out.println("Missing arguments...\n" + usage);

                    RestFidoActionsOnKey.patch(args[1], args[2], args[3], args[4], args[5], args[6]);
                    System.out.println("\nDone with Update!\n");
                    break;

                case Constants.COMMANDS_CREATE_POLICY:
                    if (args.length != 12) 
                        System.out.println("Missing arguments...\n" + usage);

                    RestCreateFidoPolicy.create(args[1], args[2], args[3], args[4], new Date(Long.parseLong(args[5])), new Date(Long.parseLong(args[6])), args[7], Integer.parseInt(args[8]), args[9], args[10], args[11]);
                    System.out.println("\nDone with Create!\n");
                    break;

                case Constants.COMMANDS_PATCH_POLICY:
                    if (args.length != 11) 
                        System.out.println("Missing arguments...\n" + usage);

                    RestFidoActionsOnPolicy.patch(args[1], args[2], args[3], args[4], args[5], new Date(Long.parseLong(args[6])), new Date(Long.parseLong(args[7])), Integer.parseInt(args[8]), args[9], args[10], args[11]);
                    System.out.println("\nDone with patch!\n");
                    break;

                case Constants.COMMANDS_DELETE_POLICY:
                    if (args.length != 6) 
                        System.out.println("Missing arguments...\n" + usage);

                    RestFidoActionsOnPolicy.delete(args[1], args[2], args[3], args[4], args[5]);
                    System.out.println("\nDone with delete!\n");
                    break;

                case Constants.COMMANDS_GET_POLICY:
                    if (args.length != 6) 
                        System.out.println("Missing arguments...\n" + usage);

                    RestFidoGetPolicyInfo.getPolicyInfo(args[1], args[2], args[3], args[4], args[5], args[6]);
                    System.out.println("\nDone with get policy!\n");
                    break;

                default:
                    System.out.println("Invalid Command...\n" + usage);
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Missing arguments...\n" + usage);
        }
    }
}
