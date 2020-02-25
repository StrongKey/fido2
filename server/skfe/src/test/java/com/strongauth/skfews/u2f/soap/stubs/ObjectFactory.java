/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
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
 */

package com.strongauth.skfews.u2f.soap.stubs;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each
 * Java content interface and Java element interface
 * generated in the com.strongauth.skfews.u2f.soap.stubs package.
 * <p>An ObjectFactory allows you to programatically
 * construct new instances of the Java representation
 * for XML content. The Java representation of XML
 * content can consist of schema derived interfaces
 * and classes representing the binding of schema
 * type definitions, element declarations and model
 * groups.  Factory methods for each of these are
 * provided in this class.
 *
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _PreregisterResponse_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "preregisterResponse");
    private final static QName _Register_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "register");
    private final static QName _Authorize_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "authorize");
    private final static QName _AuthorizeResponse_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "authorizeResponse");
    private final static QName _Preauthorize_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "preauthorize");
    private final static QName _Activate_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "activate");
    private final static QName _Preregister_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "preregister");
    private final static QName _DeregisterResponse_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "deregisterResponse");
    private final static QName _GetkeysinfoResponse_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "getkeysinfoResponse");
    private final static QName _RegisterResponse_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "registerResponse");
    private final static QName _PreauthorizeResponse_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "preauthorizeResponse");
    private final static QName _PreauthenticateResponse_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "preauthenticateResponse");
    private final static QName _GetserverinfoResponse_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "getserverinfoResponse");
    private final static QName _Preauthenticate_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "preauthenticate");
    private final static QName _Deactivate_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "deactivate");
    private final static QName _Getkeysinfo_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "getkeysinfo");
    private final static QName _ActivateResponse_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "activateResponse");
    private final static QName _DeactivateResponse_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "deactivateResponse");
    private final static QName _Deregister_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "deregister");
    private final static QName _AuthenticateResponse_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "authenticateResponse");
    private final static QName _Getserverinfo_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "getserverinfo");
    private final static QName _Authenticate_QNAME = new QName("http://soap.u2f.skfews.strongauth.com/", "authenticate");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.strongauth.skfews.u2f.soap.stubs
     *
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Authenticate }
     *
     */
    public Authenticate createAuthenticate() {
        return new Authenticate();
    }

    /**
     * Create an instance of {@link Getserverinfo }
     *
     */
    public Getserverinfo createGetserverinfo() {
        return new Getserverinfo();
    }

    /**
     * Create an instance of {@link AuthenticateResponse }
     *
     */
    public AuthenticateResponse createAuthenticateResponse() {
        return new AuthenticateResponse();
    }

    /**
     * Create an instance of {@link Deregister }
     *
     */
    public Deregister createDeregister() {
        return new Deregister();
    }

    /**
     * Create an instance of {@link ActivateResponse }
     *
     */
    public ActivateResponse createActivateResponse() {
        return new ActivateResponse();
    }

    /**
     * Create an instance of {@link DeactivateResponse }
     *
     */
    public DeactivateResponse createDeactivateResponse() {
        return new DeactivateResponse();
    }

    /**
     * Create an instance of {@link Getkeysinfo }
     *
     */
    public Getkeysinfo createGetkeysinfo() {
        return new Getkeysinfo();
    }

    /**
     * Create an instance of {@link Deactivate }
     *
     */
    public Deactivate createDeactivate() {
        return new Deactivate();
    }

    /**
     * Create an instance of {@link GetserverinfoResponse }
     *
     */
    public GetserverinfoResponse createGetserverinfoResponse() {
        return new GetserverinfoResponse();
    }

    /**
     * Create an instance of {@link Preauthenticate }
     *
     */
    public Preauthenticate createPreauthenticate() {
        return new Preauthenticate();
    }

    /**
     * Create an instance of {@link PreauthenticateResponse }
     *
     */
    public PreauthenticateResponse createPreauthenticateResponse() {
        return new PreauthenticateResponse();
    }

    /**
     * Create an instance of {@link PreauthorizeResponse }
     *
     */
    public PreauthorizeResponse createPreauthorizeResponse() {
        return new PreauthorizeResponse();
    }

    /**
     * Create an instance of {@link DeregisterResponse }
     *
     */
    public DeregisterResponse createDeregisterResponse() {
        return new DeregisterResponse();
    }

    /**
     * Create an instance of {@link GetkeysinfoResponse }
     *
     */
    public GetkeysinfoResponse createGetkeysinfoResponse() {
        return new GetkeysinfoResponse();
    }

    /**
     * Create an instance of {@link RegisterResponse }
     *
     */
    public RegisterResponse createRegisterResponse() {
        return new RegisterResponse();
    }

    /**
     * Create an instance of {@link Activate }
     *
     */
    public Activate createActivate() {
        return new Activate();
    }

    /**
     * Create an instance of {@link Preregister }
     *
     */
    public Preregister createPreregister() {
        return new Preregister();
    }

    /**
     * Create an instance of {@link Preauthorize }
     *
     */
    public Preauthorize createPreauthorize() {
        return new Preauthorize();
    }

    /**
     * Create an instance of {@link Authorize }
     *
     */
    public Authorize createAuthorize() {
        return new Authorize();
    }

    /**
     * Create an instance of {@link AuthorizeResponse }
     *
     */
    public AuthorizeResponse createAuthorizeResponse() {
        return new AuthorizeResponse();
    }

    /**
     * Create an instance of {@link PreregisterResponse }
     *
     */
    public PreregisterResponse createPreregisterResponse() {
        return new PreregisterResponse();
    }

    /**
     * Create an instance of {@link Register }
     *
     */
    public Register createRegister() {
        return new Register();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PreregisterResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "preregisterResponse")
    public JAXBElement<PreregisterResponse> createPreregisterResponse(PreregisterResponse value) {
        return new JAXBElement<PreregisterResponse>(_PreregisterResponse_QNAME, PreregisterResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Register }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "register")
    public JAXBElement<Register> createRegister(Register value) {
        return new JAXBElement<Register>(_Register_QNAME, Register.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Authorize }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "authorize")
    public JAXBElement<Authorize> createAuthorize(Authorize value) {
        return new JAXBElement<Authorize>(_Authorize_QNAME, Authorize.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuthorizeResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "authorizeResponse")
    public JAXBElement<AuthorizeResponse> createAuthorizeResponse(AuthorizeResponse value) {
        return new JAXBElement<AuthorizeResponse>(_AuthorizeResponse_QNAME, AuthorizeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Preauthorize }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "preauthorize")
    public JAXBElement<Preauthorize> createPreauthorize(Preauthorize value) {
        return new JAXBElement<Preauthorize>(_Preauthorize_QNAME, Preauthorize.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Activate }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "activate")
    public JAXBElement<Activate> createActivate(Activate value) {
        return new JAXBElement<Activate>(_Activate_QNAME, Activate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Preregister }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "preregister")
    public JAXBElement<Preregister> createPreregister(Preregister value) {
        return new JAXBElement<Preregister>(_Preregister_QNAME, Preregister.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeregisterResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "deregisterResponse")
    public JAXBElement<DeregisterResponse> createDeregisterResponse(DeregisterResponse value) {
        return new JAXBElement<DeregisterResponse>(_DeregisterResponse_QNAME, DeregisterResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetkeysinfoResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "getkeysinfoResponse")
    public JAXBElement<GetkeysinfoResponse> createGetkeysinfoResponse(GetkeysinfoResponse value) {
        return new JAXBElement<GetkeysinfoResponse>(_GetkeysinfoResponse_QNAME, GetkeysinfoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RegisterResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "registerResponse")
    public JAXBElement<RegisterResponse> createRegisterResponse(RegisterResponse value) {
        return new JAXBElement<RegisterResponse>(_RegisterResponse_QNAME, RegisterResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PreauthorizeResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "preauthorizeResponse")
    public JAXBElement<PreauthorizeResponse> createPreauthorizeResponse(PreauthorizeResponse value) {
        return new JAXBElement<PreauthorizeResponse>(_PreauthorizeResponse_QNAME, PreauthorizeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PreauthenticateResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "preauthenticateResponse")
    public JAXBElement<PreauthenticateResponse> createPreauthenticateResponse(PreauthenticateResponse value) {
        return new JAXBElement<PreauthenticateResponse>(_PreauthenticateResponse_QNAME, PreauthenticateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetserverinfoResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "getserverinfoResponse")
    public JAXBElement<GetserverinfoResponse> createGetserverinfoResponse(GetserverinfoResponse value) {
        return new JAXBElement<GetserverinfoResponse>(_GetserverinfoResponse_QNAME, GetserverinfoResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Preauthenticate }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "preauthenticate")
    public JAXBElement<Preauthenticate> createPreauthenticate(Preauthenticate value) {
        return new JAXBElement<Preauthenticate>(_Preauthenticate_QNAME, Preauthenticate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Deactivate }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "deactivate")
    public JAXBElement<Deactivate> createDeactivate(Deactivate value) {
        return new JAXBElement<Deactivate>(_Deactivate_QNAME, Deactivate.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Getkeysinfo }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "getkeysinfo")
    public JAXBElement<Getkeysinfo> createGetkeysinfo(Getkeysinfo value) {
        return new JAXBElement<Getkeysinfo>(_Getkeysinfo_QNAME, Getkeysinfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ActivateResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "activateResponse")
    public JAXBElement<ActivateResponse> createActivateResponse(ActivateResponse value) {
        return new JAXBElement<ActivateResponse>(_ActivateResponse_QNAME, ActivateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeactivateResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "deactivateResponse")
    public JAXBElement<DeactivateResponse> createDeactivateResponse(DeactivateResponse value) {
        return new JAXBElement<DeactivateResponse>(_DeactivateResponse_QNAME, DeactivateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Deregister }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "deregister")
    public JAXBElement<Deregister> createDeregister(Deregister value) {
        return new JAXBElement<Deregister>(_Deregister_QNAME, Deregister.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AuthenticateResponse }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "authenticateResponse")
    public JAXBElement<AuthenticateResponse> createAuthenticateResponse(AuthenticateResponse value) {
        return new JAXBElement<AuthenticateResponse>(_AuthenticateResponse_QNAME, AuthenticateResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Getserverinfo }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "getserverinfo")
    public JAXBElement<Getserverinfo> createGetserverinfo(Getserverinfo value) {
        return new JAXBElement<Getserverinfo>(_Getserverinfo_QNAME, Getserverinfo.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Authenticate }{@code >}}
     *
     */
    @XmlElementDecl(namespace = "http://soap.u2f.skfews.strongauth.com/", name = "authenticate")
    public JAXBElement<Authenticate> createAuthenticate(Authenticate value) {
        return new JAXBElement<Authenticate>(_Authenticate_QNAME, Authenticate.class, null, value);
    }

}
