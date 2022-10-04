/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.skfs.saml;

import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.saml20.AssertionType;
import com.strongkey.saml20.AudienceRestrictionType;
import com.strongkey.saml20.AuthnContextType;
import com.strongkey.saml20.AuthnStatementType;
import com.strongkey.saml20.ConditionsType;
import com.strongkey.saml20.NameIDType;
import com.strongkey.saml20.ObjectFactory;
import com.strongkey.saml20.ResponseType;
import com.strongkey.saml20.StatusCodeType;
import com.strongkey.saml20.StatusType;
import com.strongkey.saml20.SubjectConfirmationDataType;
import com.strongkey.saml20.SubjectConfirmationType;
import com.strongkey.saml20.SubjectType;
import com.strongkey.skfs.core.U2FUtility;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.util.JAXBResult;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.bouncycastle.util.encoders.HexEncoder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

@Stateless
public class getSAMLAssertion implements getSAMLAssertionLocal {

    /**
     ** This class's name - used for logging & not persisted
     *
     */
    private final String classname = this.getClass().getName();
    private String ASSERTION_ID;
    @EJB
    signSAMLAssertionBeanLocal signSamlejb;

    @Override
    public String execute(String did, String username, String samlrequest) {
        
        String SAML_VERSION = SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.version");
        String SAML_SUBJECT_NAMEID_FORMAT = SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.subject.nameid.format");
        String SAML_SUBJECT_CONFIRMATION_METHOD = SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.subject.confirmation.method");
        String SAML_ISSUER_ENTITY_NAMEID_FORMAT = SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.issuer.entity.nameid.format");
        String SAML_ISSUER_ENTITY_NAME = SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.issuer.entity.name");
        String SAML_AUTHNCONTEXT_CLASSREF = SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.authcontext.classref");
        int SAML_DURATION = Integer.parseInt(SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.assertion.duration"));
        String SAML_RESPONSE_SUCCESS = SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.response.status.success");
        String SAML_SIGNATURE_TYPE = SKFSCommon.getConfigurationProperty(Long.parseLong(did), "skfs.cfg.property.saml.signature.type");
        
        try {
            SecureRandom rng = cryptoCommon.getSecureRandom();
            HexEncoder hex = new HexEncoder();
            ByteArrayOutputStream rngbaos;
            byte[] rngbytes = new byte[16];

            // Get SAML AuthnRequest XML object for input values
            ByteArrayInputStream bais = new ByteArrayInputStream(java.util.Base64.getDecoder().decode(samlrequest.getBytes("UTF-8"))); 
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            Document doc = dbf.newDocumentBuilder().parse(bais);
            bais.close();

            // Retreive Issuer Node to ensure SAML Service Provider is 
            // calling the right Identity Provider (the SKFS
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression exprAuthnRequest = xpath.compile("//*[local-name()='AuthnRequest']//*[local-name()='Issuer']");
            Element issuerNode = (Element) exprAuthnRequest.evaluate(doc, XPathConstants.NODE);
            
            // Retreive the AssertionConsumerServiceURL attribute
            XPathExpression exprServiceUrl = xpath.compile("//*[local-name()='AuthnRequest']//@AssertionConsumerServiceURL");
            String serviceProviderUrl = (String) exprServiceUrl.evaluate(doc, XPathConstants.STRING);

            // Retrieve the ID attribute
            XPathExpression exprAuthnRequestID = xpath.compile("//*[local-name()='AuthnRequest']//@ID");
            String requestID = (String) exprAuthnRequestID.evaluate(doc, XPathConstants.STRING);

            // Begin creating the SAML Response XML object
            // Create DatatypeFactory and ObjectFactory instances for repeated use
            DatatypeFactory dtfactory = DatatypeFactory.newInstance();
            ObjectFactory samlpObjectFactory = new ObjectFactory();
            
            // Create a SAML Protocol Response XML object
            ResponseType samlResponse = samlpObjectFactory.createResponseType();
            rngbaos = new ByteArrayOutputStream(16);
            rng.nextBytes(rngbytes);
            hex.encode(rngbytes, 0, 16, rngbaos);
            String responseID = "_".concat(rngbaos.toString("UTF-8"));
            samlResponse.setDestination(serviceProviderUrl);
            samlResponse.setID(responseID);
            samlResponse.setInResponseTo(requestID);
            samlResponse.setVersion(SAML_VERSION);
            samlResponse.setIssueInstant(getCurrentXgc(dtfactory, 0));

            // Create and set Issuer information
            NameIDType entityNameid = samlpObjectFactory.createNameIDType();
            entityNameid.setFormat(SAML_ISSUER_ENTITY_NAMEID_FORMAT);
            entityNameid.setValue(SAML_ISSUER_ENTITY_NAME);
            samlResponse.setIssuer(entityNameid);

            // Create and set StatusCode information
            StatusCodeType statusCode = samlpObjectFactory.createStatusCodeType();
            statusCode.setValue(SAML_RESPONSE_SUCCESS);
            
            // Create Status element
            StatusType status = samlpObjectFactory.createStatusType();
            status.setStatusCode(statusCode);
            samlResponse.setStatus(status);
            
            // Create the SAML Assertion
            // Create an Assertion, set version and creaton date/time
            AssertionType samlAssertionType = samlpObjectFactory.createAssertionType();
            rngbaos = new ByteArrayOutputStream(16);
            rng.nextBytes(rngbytes);
            hex.encode(rngbytes, 0, 16, rngbaos);
            ASSERTION_ID = "_".concat(rngbaos.toString("UTF-8"));
            samlAssertionType.setID(ASSERTION_ID);
            samlAssertionType.setVersion(SAML_VERSION);
            samlAssertionType.setIssueInstant(getCurrentXgc(dtfactory, 0));

            // Create and set Issuer information
            NameIDType issuerNameid = samlpObjectFactory.createNameIDType();
            issuerNameid.setFormat(SAML_ISSUER_ENTITY_NAMEID_FORMAT);
            issuerNameid.setValue(SAML_ISSUER_ENTITY_NAME);
            samlAssertionType.setIssuer(issuerNameid);

            // Create and set SubjectConfirmationData information
            SubjectConfirmationDataType subjectConfDataType = samlpObjectFactory.createSubjectConfirmationDataType();
            subjectConfDataType.setInResponseTo(requestID);
            XMLGregorianCalendar subjectConfValid = getCurrentXgc(dtfactory, SAML_DURATION);
            subjectConfDataType.setNotOnOrAfter(subjectConfValid);
            subjectConfDataType.setRecipient(serviceProviderUrl);
            
            // Create and set SubjectConfirmation element
            SubjectConfirmationType subjectConfType = samlpObjectFactory.createSubjectConfirmationType();
            subjectConfType.setMethod(SAML_SUBJECT_CONFIRMATION_METHOD);
            subjectConfType.setSubjectConfirmationData(subjectConfDataType);
            JAXBElement<SubjectConfirmationType> subjectConfirmation = samlpObjectFactory.createSubjectConfirmation(subjectConfType);
            
            // Create and set NameID element
            NameIDType subjectNameidType = samlpObjectFactory.createNameIDType();
            subjectNameidType.setFormat(SAML_SUBJECT_NAMEID_FORMAT);
            subjectNameidType.setValue(username);
            JAXBElement<NameIDType> nameID = samlpObjectFactory.createNameID(subjectNameidType);
            
            // Create and set Subject information
            SubjectType subjectType = samlpObjectFactory.createSubjectType();
            subjectType.getContent().add(nameID);
            subjectType.getContent().add(subjectConfirmation);
            samlAssertionType.setSubject(subjectType);

            // Create AudieceRestrion element
            AudienceRestrictionType audRestType = samlpObjectFactory.createAudienceRestrictionType();
            audRestType.getAudience().add(issuerNode.getTextContent());
            
            // Set Conditions information
            ConditionsType conditions = samlpObjectFactory.createConditionsType();
            XMLGregorianCalendar current = getCurrentXgc(dtfactory, 0);
            conditions.setNotBefore(current);
            XMLGregorianCalendar valid = getCurrentXgc(dtfactory, SAML_DURATION);
            conditions.setNotOnOrAfter(valid);
            conditions.getConditionOrAudienceRestrictionOrOneTimeUse().add(audRestType);
            samlAssertionType.setConditions(conditions);

            // Create AuthnStatement element
            AuthnStatementType authstat = samlpObjectFactory.createAuthnStatementType();
            authstat.setAuthnInstant(getCurrentXgc(dtfactory, 0));
            authstat.setSessionIndex(U2FUtility.getRandom(20));

            // Create AuthnContext element
            AuthnContextType authctxt = samlpObjectFactory.createAuthnContextType();
            JAXBElement<String> classref = samlpObjectFactory.createAuthnContextClassRef(SAML_AUTHNCONTEXT_CLASSREF);
            authctxt.getContent().add(classref);

            // Set AuthenticationStatement information
            authstat.setAuthnContext(authctxt);
            samlAssertionType.getStatementOrAuthnStatementOrAttributeStatement().add(authstat);

            // Generate the SAML Assertion
            JAXBElement<AssertionType> assertion = samlpObjectFactory.createAssertion(samlAssertionType);

            // Create JAXBContext
            JAXBContext jc = JAXBContext.newInstance("com.strongkey.saml20");

            // Write out the XML content
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
            m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            m.marshal(assertion, baos);
            String saml = baos.toString();
            baos.close();

            bais = new ByteArrayInputStream(signSamlejb.execute(did, saml).getBytes("UTF-8"));
            doc = dbf.newDocumentBuilder().parse(bais);
            bais.close();
            
            // Set Namespaces - Assertion node
            XPathExpression exprAssertion = xpath.compile("//*[local-name()='Assertion']");
            Element domAssertion = (Element) exprAssertion.evaluate(doc, XPathConstants.NODE);
            
            // Transform to a JAXBElement
            Transformer trf = TransformerFactory.newInstance().newTransformer();
            trf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            JAXBResult result = new JAXBResult(jc);
            trf.transform(new DOMSource(domAssertion), result);
            assertion = (JAXBElement<AssertionType>) result.getResult();

            // Add it to the SAML Response and generate the Response XML
            samlResponse.getAssertion().add(assertion.getValue());
            JAXBElement<ResponseType> response = samlpObjectFactory.createResponse(samlResponse);
            baos = new ByteArrayOutputStream();
            
            m.setProperty("com.sun.xml.bind.namespacePrefixMapper",
                new NamespacePrefixMapper() {
                @Override
                public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                    String prefix = null;
                    switch(namespaceUri) {
                        case "urn:oasis:names:tc:SAML:2.0:protocol": 
                            prefix = "samlp";
                            break;
                        case "http://www.w3.org/2000/09/xmldsig#": 
                            prefix = "ds";
                            break;
                        default: prefix = "";
                    }
                    return prefix;
                }
            });
            
            m.marshal(response, baos);
            String responseString = baos.toString();
            baos.close();
            return responseString;

        } catch (DatatypeConfigurationException | JAXBException | ParserConfigurationException | IOException | SAXException | XPathExpressionException | TransformerException ex) {
            Logger.getLogger(classname).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private XMLGregorianCalendar getCurrentXgc(DatatypeFactory dtfactory, int duration) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeZone(TimeZone.getTimeZone(SKFSCommon.getConfigurationProperty("skfs.cfg.property.saml.timezone")));
        cal.add(Calendar.MINUTE, duration);

        int offset = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (60 * 1000);
        return dtfactory.newXMLGregorianCalendar(
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH) + 1,
                cal.get(Calendar.DATE),
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND),
                cal.get(Calendar.MILLISECOND),
                offset);
    }
}
