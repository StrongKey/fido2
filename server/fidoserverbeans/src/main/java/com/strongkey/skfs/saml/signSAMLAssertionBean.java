/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.skfs.saml;

import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Stateless
public class signSAMLAssertionBean implements signSAMLAssertionBeanLocal {

    @Override
    public String execute(String did, String saml) {
        try {
            // Instance main XML Signature Toolkit.
            XMLSignatureFactory fac = XMLSignatureFactory.getInstance("DOM");
            XPathFactory xPathfactory = XPathFactory.newInstance();

            // Retreive PrivateKey and Public Certificate from Specified KeyStore.
            //Get signing key and certificate from queue
            List<Object> signinglist = null;
            try {
                signinglist = SKFSCommon.takeSAMLSignList(did);
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, signSAMLAssertionBean.class.getName(), "signing list",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6003"), "signing list: " + signinglist);
            } catch (InterruptedException ex) {
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, signSAMLAssertionBean.class.getName(), "signing list retieval failed",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6003"), ex.getMessage());
            }
            PrivateKey privateKey = (PrivateKey) signinglist.get(0);
            X509Certificate publicCertificate = (X509Certificate) signinglist.get(1);

            // Build up Document
            ByteArrayInputStream bais = new ByteArrayInputStream(saml.getBytes("UTF-8"));
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(bais);
            bais.close();

            // Retreive Assertion Node to be signed.
            XPath xpath = xPathfactory.newXPath();
            XPathExpression exprAssertion = xpath.compile("//*[local-name()='Assertion']");
            Element assertionNode = (Element) exprAssertion.evaluate(doc, XPathConstants.NODE);        
            assertionNode.setIdAttribute("ID", true);

            // Retreive Assertion ID because it is used in the URI attribute of the signature.
            XPathExpression exprAssertionID = xpath.compile("//*[local-name()='Assertion']//@ID");
            String assertionID = (String) exprAssertionID.evaluate(doc, XPathConstants.STRING);

            // Retreive Subject Node because the signature will be inserted before.
            XPathExpression exprAssertionSubject = xpath.compile("//*[local-name()='Assertion']//*[local-name()='Subject']");
            Node insertionNode = (Node) exprAssertionSubject.evaluate(doc, XPathConstants.NODE);  

            // Create the DOMSignContext by specifying the signing informations: Private Key, Node to be signed, Where to insert the Signature.
            DOMSignContext dsc = new DOMSignContext(privateKey, assertionNode, insertionNode);
            dsc.setDefaultNamespacePrefix("ds");
            dsc.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);

            // Create a CanonicalizationMethod which specify how the XML will be canonicalized before signed.
            CanonicalizationMethod canonicalizationMethod = fac.newCanonicalizationMethod(CanonicalizationMethod.EXCLUSIVE, (C14NMethodParameterSpec) null);
            // Create a SignatureMethod which specify how the XML will be signed.
            SignatureMethod signatureMethod = null;
            switch (SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.signature.type").toLowerCase()) {
                case "rsa":
                    signatureMethod = fac.newSignatureMethod("http://www.w3.org/2001/04/xmldsig-more#rsa-sha256", null);
                    break;
                case "ec":
                    signatureMethod = fac.newSignatureMethod(SignatureMethod.ECDSA_SHA256, null);
                    break;
                default:
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "signSAML", "CRYPTO-MSG-0000", "Invalid signature type: " + SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.signature.type"));
            }

            // Create an Array of Transform, add it one Transform which specify the Signature ENVELOPED method.
            List<Transform> transformList = new ArrayList<>(2);
            transformList.add(fac.newTransform(Transform.ENVELOPED, (TransformParameterSpec) null));  
            transformList.add(fac.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#", (TransformParameterSpec) null));

            String digestmethod = null;
            switch (SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.digest.type").toLowerCase()) {
                case "sha256":
                    digestmethod = DigestMethod.SHA256;
                    break;
                case "sha1":
                    digestmethod = DigestMethod.SHA1;
                    break;
                default:
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "signSAML", "CRYPTO-MSG-0000", "Invalid digest type: " + SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.digest.type"));
            }
            // Create a Reference which contain: An URI to the Assertion ID, the Digest Method and the Transform List which specify the Signature ENVELOPED method.
            Reference reference = fac.newReference("#" + assertionID, fac.newDigestMethod(digestmethod, null), transformList, null, null);        
            List<Reference> referenceList = Collections.singletonList(reference);        
        
            // Create a SignedInfo with the pre-specified: Canonicalization Method, Signature Method and List of References.
            SignedInfo si = fac.newSignedInfo(canonicalizationMethod, signatureMethod, referenceList);

            // Create a new KeyInfo and add it the Public Certificate.
            KeyInfoFactory kif = fac.getKeyInfoFactory();
            List x509Content = new ArrayList();
            x509Content.add(publicCertificate);
            X509Data xd = kif.newX509Data(x509Content);
            KeyInfo ki = kif.newKeyInfo(Collections.singletonList(xd));

            // Create a new XML Signature with the pre-created : Signed Info & Key Info
            XMLSignature signature = fac.newXMLSignature(si, ki);
            signature.sign(dsc);
            
            // Put key back in queue
            SKFSCommon.putSAMLSignList(did, signinglist);

            // Transform the output to remove XML declaration
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Transformer trf = TransformerFactory.newInstance().newTransformer();
            trf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            trf.transform(new DOMSource(doc), new StreamResult(baos));
            return baos.toString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
