package com.strongkey.SAMLVerify;


/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 * Copyright (c) 2001-2022 StrongAuth, Inc. (DBA StrongKey)
 *
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 * A program to validate a SAML Response and verify the XMLSignature in
 * the Assertion element
 *
 */
import com.strongkey.saml20.ResponseType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.crypto.*;
import javax.xml.crypto.dsig.*;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.bouncycastle.asn1.x500.X500Name;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ValidateSAMLResponse {

    static String SIGNED_XML = "<samlp:Response xmlns=\"urn:oasis:names:tc:SAML:2.0:assertion\" xmlns:ds=\"http://www.w3.org/2000/09/xmldsig#\" xmlns:samlp=\"urn:oasis:names:tc:SAML:2.0:protocol\" ID=\"_8d838368458dea19ec6aed378f9fff86\" InResponseTo=\"_a02bed0380d0b3494539c5dc5457a9f3\" Version=\"2.0\" IssueInstant=\"2022-09-09T05:40:47.903Z\" Destination=\"http://10.0.2.209/cgi/samlauth\"><Issuer Format=\"urn:oasis:names:SAML:2.0:nameid-format:entity\">http://login.example.com/</Issuer><samlp:Status><samlp:StatusCode Value=\"urn:oasis:names:tc:SAML:2.0:status:Success\"/></samlp:Status><Assertion Version=\"2.0\" ID=\"_73080760c7c0e907bca6b33cfdc9fda3\" IssueInstant=\"2022-09-09T05:40:47.942Z\"><Issuer Format=\"urn:oasis:names:SAML:2.0:nameid-format:entity\">http://login.example.com/</Issuer><ds:Signature><ds:SignedInfo><ds:CanonicalizationMethod Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/><ds:SignatureMethod Algorithm=\"http://www.w3.org/2001/04/xmldsig-more#rsa-sha256\"/><ds:Reference URI=\"#_73080760c7c0e907bca6b33cfdc9fda3\"><ds:Transforms><ds:Transform Algorithm=\"http://www.w3.org/2000/09/xmldsig#enveloped-signature\"/><ds:Transform Algorithm=\"http://www.w3.org/2001/10/xml-exc-c14n#\"/></ds:Transforms><ds:DigestMethod Algorithm=\"http://www.w3.org/2001/04/xmlenc#sha256\"/><ds:DigestValue>RlwAPG3cfYK390BhniC7mImnsG4lkm2v1pIEotbHThk=</ds:DigestValue></ds:Reference></ds:SignedInfo><ds:SignatureValue>dHbMse7I14De/mZVN7RFVFa22w6dKAiZa6SB06lWtw5Jhu0H5bQfBHcLe0W8+qyinth/po7vH+FedLTfQKTWUwI/v+UAoilhWpAhwZs7gQpf28rqoLEoyF+WDuwDJZ0oNSfcGCznVY6CZ47AnIT6pQN065JS2R3GLGOp7da0+BzsmjRAVVq5hl89XNoL8tpg3dynyAPr2YRrn+A5uMjfUOaQqzW3H5FyLc8A+5HuYYEJ3zGbVQa4UbghxnoRZRzsODMrmCW+WQeFWQrZxBIinielJL13kUiApUEU7Tk6z+prTlFv0xodg9lnXeUulmihEtjQEr+UeWqatw2tKR3xZQ==</ds:SignatureValue><ds:KeyInfo><ds:X509Data><ds:X509Certificate>MIIDXTCCAkWgAwIBAgIEX+U6EDANBgkqhkiG9w0BAQsFADBXMRIwEAYDVQQKEwlTdHJvbmdLZXkxGjAYBgNVBAsTEVN0cm9uZ0tleSBUZWxsYXJvMSUwIwYDVQQDExxTS0ZTLVNBTUwgUlNBIFNpZ25pbmcgS2V5IDAxMB4XDTIyMDkwMzE5MTUyM1oXDTMyMDYwNDE5MTUyM1owVzESMBAGA1UEChMJU3Ryb25nS2V5MRowGAYDVQQLExFTdHJvbmdLZXkgVGVsbGFybzElMCMGA1UEAxMcU0tGUy1TQU1MIFJTQSBTaWduaW5nIEtleSAwMTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBALdf7ZyU0jZKeqF4cWLUuU2uUexfzpVIdNP3CrvVaT8Q6L8xOwGrmLoHO6oAZfieTA58ulAiIX1nVxtTHYIo/03u/F3+wiNrJusFdXiUwVrQ5iRYEoVYXCIUKxKucvb6OKQemxy1pv4vJ0YBxwoWGXUs1WIUworheDpsoZuZ2oBal0jxs2FuvmhRAFtPO0d4cUdycrJYV89sQlmrckeQnGvrLC4WMRt5p+7R+i8Hr/P5EYcJPf5eSEh2NnletYElZKO8aYKIE2Hsdo4h996WB6q+lqqme5wzcd2dce5OcFyD12Jj3D6LAx89SdfyQpoJ6FAmwnQ4SwsV4DP7Ii3/VDsCAwEAAaMxMC8wHQYDVR0OBBYEFIJIVpMoLYH3C5KvamX8KWAy8tEGMA4GA1UdDwEB/wQEAwIHgDANBgkqhkiG9w0BAQsFAAOCAQEAQVb+3Y7tl68qqKSzQHtvVtBsS/k0gsXt76KzvBZuG4ksScADql/6JllAuVMQcI1FQVGJWASAtyqLMhsMOQrtFf/Q1RRuc0pEav/qMhCWQY8I8vodSrlYBHrt7S2cETOYtY0DsnoatRhsnhDKH7+UJtRmlD98my3YIKo0rDq3zxwzksOFG+NWDPL6jFKs6PKIakAmx0maLjYYQtbAkbGRwx63xLj5gVw2KWgMcXXEyhq593nxz8UvlHjFonrOr0J5d5PZdKOWyQ5+Nm62C7dV1YyzpIz0Ysp1kzM0zTSGP+qQt9MMJdTcVshB2yquMmZ7JWyqHhPuKf74odBEtjZszQ==</ds:X509Certificate></ds:X509Data></ds:KeyInfo></ds:Signature><Subject><NameID Format=\"urn:oasis:names:SAML:2.0:nameid-format:kerberos\">johndoe@login.example.com</NameID><SubjectConfirmation Method=\"urn:oasis:names:tc:SAML:2.0:cm:bearer\"><SubjectConfirmationData NotOnOrAfter=\"2022-09-09T05:55:47.943Z\" Recipient=\"http://10.0.2.209/cgi/samlauth\" InResponseTo=\"_a02bed0380d0b3494539c5dc5457a9f3\"/></SubjectConfirmation></Subject><Conditions NotBefore=\"2022-09-09T05:40:47.947Z\" NotOnOrAfter=\"2022-09-09T05:55:47.947Z\"><AudienceRestriction><Audience>http://10.0.2.209/cgi/samlauth</Audience></AudienceRestriction></Conditions><AuthnStatement AuthnInstant=\"2022-09-09T05:40:47.948Z\" SessionIndex=\"VPnVKyjwp1w33vHXA7gai3gKaGQ=\"><AuthnContext><AuthnContextClassRef>urn:oasis:names:tc:SAML:2.0:ac:classes:FIDO2</AuthnContextClassRef></AuthnContext></AuthnStatement></Assertion></samlp:Response>";

    public boolean validate(String saml) throws Exception {

        // Print out SAML Assertion
        showSAMLAssertion();
        
        // Setup XPath stuff
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();

        // Instantiate the document to be validated
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        ByteArrayInputStream bais = new ByteArrayInputStream(java.util.Base64.getDecoder().decode(saml.getBytes("UTF-8")));
        Document doc = dbf.newDocumentBuilder().parse(bais);
        bais.close();

        // Find Assertion element
        NodeList anl = doc.getElementsByTagNameNS("urn:oasis:names:tc:SAML:2.0:assertion", "Assertion");
        if (anl.getLength() == 0) {
            throw new Exception("Cannot find Assertion element");
        }
        XPathExpression exprAuthnRequest = xpath.compile("//*[local-name()='Response']//*[local-name()='Assertion']");
        Element assertionNode = (Element) exprAuthnRequest.evaluate(doc, XPathConstants.NODE);
//        printTree(assertionNode);
        System.out.println("Assertion ID: " + assertionNode.getAttribute("ID"));
        assertionNode.setIdAttribute("ID", true);

        // Find Signature element
        NodeList snl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "Signature");
        if (snl.getLength() == 0) {
            throw new Exception("Cannot find Signature element");
        }

        // Find the KeyInfo element
        NodeList knl = doc.getElementsByTagNameNS(XMLSignature.XMLNS, "KeyInfo");
        if (knl.getLength() == 0) {
            throw new Exception("Cannot find KeyInfoelement");
        }
         
        // Retreive X509Certificate Node to extract the public key for verification
        XPathExpression exprX509 = xpath.compile("//*[local-name()='Response']//*[local-name()='X509Certificate']");
        Element x509Node = (Element) exprX509.evaluate(doc, XPathConstants.NODE);
        String cert = x509Node.getTextContent();
        
        try {

            // Get PublicKey from certificate - comment out if using BCFKS
            String pemcert = "-----BEGIN CERTIFICATE-----\n".concat(cert).concat("\n-----END CERTIFICATE-----");
            PublicKey pbkey = getPublicKey(pemcert);

            // Setup validation context
            DOMValidateContext dvc = new DOMValidateContext(pbkey, snl.item(0));
            dvc.setIdAttributeNS(assertionNode, null, "ID");
            dvc.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);

            // Unmarshal the Signature
            XMLSignatureFactory xmlsf = XMLSignatureFactory.getInstance("DOM");
            XMLSignature signature = xmlsf.unmarshalXMLSignature(dvc);

            // Identify the Reference to element that is signed: the "Assertion"
            Reference r = signature.getSignedInfo().getReferences().get(0);
            System.out.println("Reference found in Signature: " + r.getURI());

            List<Reference> references = signature.getSignedInfo().getReferences();
            int s = references.size();
     
            // Validate the Signature
            boolean coreValidity = signature.validate(dvc);

            // Check core validation status.
            if (coreValidity == false) {
                boolean sv = signature.getSignatureValue().validate(dvc);
                System.out.println("Signature Validation status: " + sv);
                if (sv == false) {
                    // Check the validation status of each Reference.
                    int i = signature.getSignedInfo().getReferences().size();
                    List<Reference> refs = signature.getSignedInfo().getReferences();
                    for (int j = 0; j < i; j++) {
                        Reference ref = refs.get(j);
                        System.out.println("Verifying Reference only: " + ref.getURI());
                        boolean refValid = ref.validate(dvc);
                        System.out.println("ref[" + j + "] validity status: " + refValid);
                    }
                    return false;
                } else {
                    return true;
                }
            } else {
                System.out.println("Signature passed core validation");
                return true;
            }
        } catch (MarshalException | XMLSignatureException ex) {
            throw new Exception(ex.getLocalizedMessage());
        }
    }

    /**
     * Method to simply print out XML of what we are validating - need to
     * uncomment one line near main()
     */
    private static void showSAMLAssertion() {
        try {
            // Output SAML Assertion
            JAXBContext jc = JAXBContext.newInstance("com.strongkey.saml20");
            Unmarshaller unm = jc.createUnmarshaller();
            JAXBElement<ResponseType> response = (JAXBElement<ResponseType>) unm.unmarshal(new ByteArrayInputStream(SIGNED_XML.getBytes("UTF-8")));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            m.marshal(response, baos);
            String saml = baos.toString();
            baos.close();
            System.out.println(saml);
        } catch (JAXBException | IOException ex) {
            Logger.getLogger(ValidateSAMLResponse.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @param certificate String containing Base64-encoded certificate
     * @return PublicKey
     * @throws Exception
     */
    private static PublicKey getPublicKey(String certificate) throws Exception {

        PublicKey pbk = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(certificate.getBytes());
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) factory.generateCertificate(bais);
            X500Name xcdn = new X500Name(cert.getSubjectX500Principal().getName());
            System.out.println("Certificate found: " + xcdn);

            // Uncomment to validate against DigitalSignature keyUsage
//            // Collect key-usages in a string buffer for logging
//            boolean[] keyusage = cert.getKeyUsage();
//            java.io.StringWriter sw = new java.io.StringWriter();
//            for (int i = 0; i < keyusage.length; i++) {
//                sw.write("\nkeyusage[" + i + "]: " + keyusage[i]);
//            }
//            // Match for the signing bit    
//            if (keyusage[0]) {
//                // If true, this is the certificate we want
                pbk = cert.getPublicKey();
//            }
        } catch (CertificateException ex) {
            throw new Exception(ex.getLocalizedMessage());
        }

        if (pbk == null) {
            throw new Exception("No PublicKey found");
        }
        return pbk;
    }

    /**
     * Prints the tree of an XML element
     *
     * @param doc
     */
    static void printTree(Node doc) {
        if (doc == null) {
            System.out.println("Nothing to print!!");
            return;
        }
        try {
            System.out.println(doc.getNodeName() + "  " + doc.getNodeValue());
            NamedNodeMap cl = doc.getAttributes();
            if (cl != null) {
                for (int i = 0; i < cl.getLength(); i++) {
                    Node node = cl.item(i);
                    System.out.println("\t" + node.getNodeName() + " -> " + node.getNodeValue());
                }
            }
            NodeList nl = doc.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                printTree(node);
            }
        } catch (DOMException e) {
            System.out.println("Cannot print!! " + e.getMessage());
        }
    }
}
