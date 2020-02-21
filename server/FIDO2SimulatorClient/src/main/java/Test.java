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
 * A test file to test a canned response from the FIDO2 server
 */

import javax.json.Json;
import javax.json.JsonObjectBuilder;

public class Test
{
    public static void main(String[] args)
    {
        String input = "{\"id\":\"CiLR2zUAumkfL9bqQVNL6G6ky3ePa9IPwzagFg98hofrrcaXp8_FCEJc7JvvYaeU5CwJGCpGfm1aysLB4nc1CzkCxI2HMz1MgRhTe_F3iPJ-jY5YzlOI9-YnmffMVz36u6dqT_lbf93ltYPWNnm4J0Yo58HJuQxGrb-n5h4Z0FybtjP_T8_07958ryoJrJT_pEk1bJDZMxLcndNEilcMSY_BCmvSRzqhQR5nlatri6M\",\"rawId\":\"CiLR2zUAumkfL9bqQVNL6G6ky3ePa9IPwzagFg98hofrrcaXp8_FCEJc7JvvYaeU5CwJGCpGfm1aysLB4nc1CzkCxI2HMz1MgRhTe_F3iPJ-jY5YzlOI9-YnmffMVz36u6dqT_lbf93ltYPWNnm4J0Yo58HJuQxGrb-n5h4Z0FybtjP_T8_07958ryoJrJT_pEk1bJDZMxLcndNEilcMSY_BCmvSRzqhQR5nlatri6M\",\"response\":{\"attestationObject\":\"o2NmbXRmcGFja2VkZ2F0dFN0bXSjY2FsZyZjc2lnWEcwRQIgWURWjwAi-aHe2cZH6uJZAAoMYTf7e6PFfxVVnZR1BAYCIQCEe3MlwUvy1DF339IwEF20JjP4rqEEoeNuEI4dYAhXLGN4NWOBWQHkMIIB4DCCAYOgAwIBAgIEbCtY8jAMBggqhkjOPQQDAgUAMGQxCzAJBgNVBAYTAlVTMRcwFQYDVQQKEw5TdHJvbmdBdXRoIEluYzEiMCAGA1UECxMZQXV0aGVudGljYXRvciBBdHRlc3RhdGlvbjEYMBYGA1UEAwwPQXR0ZXN0YXRpb25fS2V5MB4XDTE5MDcxODE3MTEyN1oXDTI5MDcxNTE3MTEyN1owZDELMAkGA1UEBhMCVVMxFzAVBgNVBAoTDlN0cm9uZ0F1dGggSW5jMSIwIAYDVQQLExlBdXRoZW50aWNhdG9yIEF0dGVzdGF0aW9uMRgwFgYDVQQDDA9BdHRlc3RhdGlvbl9LZXkwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQx9IY-uvfEvZ9HaJX3yaYmOqSIYQxS3Oi3Ed7iw4zXGR5C4RaKyOQeIu1hK2QCgoq210KjwNFU3TpsqAMZLZmFoyEwHzAdBgNVHQ4EFgQUNELQ4HBDjTWzj9E0Z719E4EeLxgwDAYIKoZIzj0EAwIFAANJADBGAiEA7RbR2NCtyMQwiyGGOADy8rDHjNFPlZG8Ip9kr9iAKisCIQCi3cNAFjTL03-sk7C1lij7JQ6mO7rhfdDMfDXSjegwuWhhdXRoRGF0YVkBNGwty0L10Pkv6Zd10U6KkbEww3A6uDkOe1Uk1cBpmgRyQQAAAAAAAAAAAAAAAAAAAAAAAAAAALAKItHbNQC6aR8v1upBU0vobqTLd49r0g_DNqAWD3yGh-utxpenz8UIQlzsm-9hp5TkLAkYKkZ-bVrKwsHidzULOQLEjYczPUyBGFN78XeI8n6NjljOU4j35ieZ98xXPfq7p2pP-Vt_3eW1g9Y2ebgnRijnwcm5DEatv6fmHhnQXJu2M_9Pz_Tv3nyvKgmslP-kSTVskNkzEtyd00SKVwxJj8EKa9JHOqFBHmeVq2uLo6UBAgMmIAEhWCBIfYenJFpBAhc3dC6wF1oc3ICOrAXjYaX2H6s6YpCZPSJYIFQKYJFLUEelYb15Mmshv2zHSWcoOAl3LrATTFITnIow\",\"clientDataJSON\":\"eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiV3lkcEJCejZTVllPSVVJTHJqekUxdyIsIm9yaWdpbiI6Imh0dHBzOi8vc2FrYTIwOS5zdHJvbmdhdXRoLmNvbTo4MTgxIn0\"},\"type\":\"public-key\"}";
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("protocol", "FIDO2_0")
           .add("response", input);
        System.out.println(job.build().toString());
    }
}
