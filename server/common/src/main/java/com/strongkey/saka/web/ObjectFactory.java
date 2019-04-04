
package com.strongkey.saka.web;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.strongauth.saka.web package. 
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

    private final static QName _Delete_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "delete");
    private final static QName _BatchDelete_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "batchDelete");
    private final static QName _GpkDecrypt_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "gpkDecrypt");
    private final static QName _BatchDeleteResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "batchDeleteResponse");
    private final static QName _GpkEncryptResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "gpkEncryptResponse");
    private final static QName _BatchEncrypt_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "batchEncrypt");
    private final static QName _Ping_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "ping");
    private final static QName _RelayResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "relayResponse");
    private final static QName _StrongKeyLiteException_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "StrongKeyLiteException");
    private final static QName _BatchSearch_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "batchSearch");
    private final static QName _EntropyResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "entropyResponse");
    private final static QName _Entropy_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "entropy");
    private final static QName _Search_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "search");
    private final static QName _DeleteResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "deleteResponse");
    private final static QName _Encrypt_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "encrypt");
    private final static QName _BatchSearchResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "batchSearchResponse");
    private final static QName _GpkEncrypt_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "gpkEncrypt");
    private final static QName _DecryptResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "decryptResponse");
    private final static QName _Relay_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "relay");
    private final static QName _BatchEncryptResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "batchEncryptResponse");
    private final static QName _BatchDecrypt_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "batchDecrypt");
    private final static QName _BatchDecryptResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "batchDecryptResponse");
    private final static QName _PingResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "pingResponse");
    private final static QName _Decrypt_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "decrypt");
    private final static QName _EncryptResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "encryptResponse");
    private final static QName _GpkDecryptResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "gpkDecryptResponse");
    private final static QName _SearchResponse_QNAME = new QName("http://web.strongkeylite.strongauth.com/", "searchResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.strongauth.saka.web
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link BatchDeleteResponse }
     * 
     */
    public BatchDeleteResponse createBatchDeleteResponse() {
        return new BatchDeleteResponse();
    }

    /**
     * Create an instance of {@link GpkEncryptResponse }
     * 
     */
    public GpkEncryptResponse createGpkEncryptResponse() {
        return new GpkEncryptResponse();
    }

    /**
     * Create an instance of {@link BatchEncrypt }
     * 
     */
    public BatchEncrypt createBatchEncrypt() {
        return new BatchEncrypt();
    }

    /**
     * Create an instance of {@link Ping }
     * 
     */
    public Ping createPing() {
        return new Ping();
    }

    /**
     * Create an instance of {@link Delete }
     * 
     */
    public Delete createDelete() {
        return new Delete();
    }

    /**
     * Create an instance of {@link BatchDelete }
     * 
     */
    public BatchDelete createBatchDelete() {
        return new BatchDelete();
    }

    /**
     * Create an instance of {@link GpkDecrypt }
     * 
     */
    public GpkDecrypt createGpkDecrypt() {
        return new GpkDecrypt();
    }

    /**
     * Create an instance of {@link Search }
     * 
     */
    public Search createSearch() {
        return new Search();
    }

    /**
     * Create an instance of {@link Entropy }
     * 
     */
    public Entropy createEntropy() {
        return new Entropy();
    }

    /**
     * Create an instance of {@link DeleteResponse }
     * 
     */
    public DeleteResponse createDeleteResponse() {
        return new DeleteResponse();
    }

    /**
     * Create an instance of {@link Encrypt }
     * 
     */
    public Encrypt createEncrypt() {
        return new Encrypt();
    }

    /**
     * Create an instance of {@link RelayResponse }
     * 
     */
    public RelayResponse createRelayResponse() {
        return new RelayResponse();
    }

    /**
     * Create an instance of {@link BatchSearch }
     * 
     */
    public BatchSearch createBatchSearch() {
        return new BatchSearch();
    }

    /**
     * Create an instance of {@link StrongKeyLiteException }
     * 
     */
    public StrongKeyLiteException createStrongKeyLiteException() {
        return new StrongKeyLiteException();
    }

    /**
     * Create an instance of {@link EntropyResponse }
     * 
     */
    public EntropyResponse createEntropyResponse() {
        return new EntropyResponse();
    }

    /**
     * Create an instance of {@link DecryptResponse }
     * 
     */
    public DecryptResponse createDecryptResponse() {
        return new DecryptResponse();
    }

    /**
     * Create an instance of {@link GpkEncrypt }
     * 
     */
    public GpkEncrypt createGpkEncrypt() {
        return new GpkEncrypt();
    }

    /**
     * Create an instance of {@link Relay }
     * 
     */
    public Relay createRelay() {
        return new Relay();
    }

    /**
     * Create an instance of {@link BatchSearchResponse }
     * 
     */
    public BatchSearchResponse createBatchSearchResponse() {
        return new BatchSearchResponse();
    }

    /**
     * Create an instance of {@link EncryptResponse }
     * 
     */
    public EncryptResponse createEncryptResponse() {
        return new EncryptResponse();
    }

    /**
     * Create an instance of {@link GpkDecryptResponse }
     * 
     */
    public GpkDecryptResponse createGpkDecryptResponse() {
        return new GpkDecryptResponse();
    }

    /**
     * Create an instance of {@link SearchResponse }
     * 
     */
    public SearchResponse createSearchResponse() {
        return new SearchResponse();
    }

    /**
     * Create an instance of {@link BatchDecrypt }
     * 
     */
    public BatchDecrypt createBatchDecrypt() {
        return new BatchDecrypt();
    }

    /**
     * Create an instance of {@link BatchEncryptResponse }
     * 
     */
    public BatchEncryptResponse createBatchEncryptResponse() {
        return new BatchEncryptResponse();
    }

    /**
     * Create an instance of {@link BatchDecryptResponse }
     * 
     */
    public BatchDecryptResponse createBatchDecryptResponse() {
        return new BatchDecryptResponse();
    }

    /**
     * Create an instance of {@link Decrypt }
     * 
     */
    public Decrypt createDecrypt() {
        return new Decrypt();
    }

    /**
     * Create an instance of {@link PingResponse }
     * 
     */
    public PingResponse createPingResponse() {
        return new PingResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Delete }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "delete")
    public JAXBElement<Delete> createDelete(Delete value) {
        return new JAXBElement<Delete>(_Delete_QNAME, Delete.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BatchDelete }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "batchDelete")
    public JAXBElement<BatchDelete> createBatchDelete(BatchDelete value) {
        return new JAXBElement<BatchDelete>(_BatchDelete_QNAME, BatchDelete.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GpkDecrypt }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "gpkDecrypt")
    public JAXBElement<GpkDecrypt> createGpkDecrypt(GpkDecrypt value) {
        return new JAXBElement<GpkDecrypt>(_GpkDecrypt_QNAME, GpkDecrypt.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BatchDeleteResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "batchDeleteResponse")
    public JAXBElement<BatchDeleteResponse> createBatchDeleteResponse(BatchDeleteResponse value) {
        return new JAXBElement<BatchDeleteResponse>(_BatchDeleteResponse_QNAME, BatchDeleteResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GpkEncryptResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "gpkEncryptResponse")
    public JAXBElement<GpkEncryptResponse> createGpkEncryptResponse(GpkEncryptResponse value) {
        return new JAXBElement<GpkEncryptResponse>(_GpkEncryptResponse_QNAME, GpkEncryptResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BatchEncrypt }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "batchEncrypt")
    public JAXBElement<BatchEncrypt> createBatchEncrypt(BatchEncrypt value) {
        return new JAXBElement<BatchEncrypt>(_BatchEncrypt_QNAME, BatchEncrypt.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Ping }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "ping")
    public JAXBElement<Ping> createPing(Ping value) {
        return new JAXBElement<Ping>(_Ping_QNAME, Ping.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link RelayResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "relayResponse")
    public JAXBElement<RelayResponse> createRelayResponse(RelayResponse value) {
        return new JAXBElement<RelayResponse>(_RelayResponse_QNAME, RelayResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StrongKeyLiteException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "StrongKeyLiteException")
    public JAXBElement<StrongKeyLiteException> createStrongKeyLiteException(StrongKeyLiteException value) {
        return new JAXBElement<StrongKeyLiteException>(_StrongKeyLiteException_QNAME, StrongKeyLiteException.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BatchSearch }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "batchSearch")
    public JAXBElement<BatchSearch> createBatchSearch(BatchSearch value) {
        return new JAXBElement<BatchSearch>(_BatchSearch_QNAME, BatchSearch.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EntropyResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "entropyResponse")
    public JAXBElement<EntropyResponse> createEntropyResponse(EntropyResponse value) {
        return new JAXBElement<EntropyResponse>(_EntropyResponse_QNAME, EntropyResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Entropy }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "entropy")
    public JAXBElement<Entropy> createEntropy(Entropy value) {
        return new JAXBElement<Entropy>(_Entropy_QNAME, Entropy.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Search }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "search")
    public JAXBElement<Search> createSearch(Search value) {
        return new JAXBElement<Search>(_Search_QNAME, Search.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DeleteResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "deleteResponse")
    public JAXBElement<DeleteResponse> createDeleteResponse(DeleteResponse value) {
        return new JAXBElement<DeleteResponse>(_DeleteResponse_QNAME, DeleteResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Encrypt }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "encrypt")
    public JAXBElement<Encrypt> createEncrypt(Encrypt value) {
        return new JAXBElement<Encrypt>(_Encrypt_QNAME, Encrypt.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BatchSearchResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "batchSearchResponse")
    public JAXBElement<BatchSearchResponse> createBatchSearchResponse(BatchSearchResponse value) {
        return new JAXBElement<BatchSearchResponse>(_BatchSearchResponse_QNAME, BatchSearchResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GpkEncrypt }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "gpkEncrypt")
    public JAXBElement<GpkEncrypt> createGpkEncrypt(GpkEncrypt value) {
        return new JAXBElement<GpkEncrypt>(_GpkEncrypt_QNAME, GpkEncrypt.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DecryptResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "decryptResponse")
    public JAXBElement<DecryptResponse> createDecryptResponse(DecryptResponse value) {
        return new JAXBElement<DecryptResponse>(_DecryptResponse_QNAME, DecryptResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Relay }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "relay")
    public JAXBElement<Relay> createRelay(Relay value) {
        return new JAXBElement<Relay>(_Relay_QNAME, Relay.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BatchEncryptResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "batchEncryptResponse")
    public JAXBElement<BatchEncryptResponse> createBatchEncryptResponse(BatchEncryptResponse value) {
        return new JAXBElement<BatchEncryptResponse>(_BatchEncryptResponse_QNAME, BatchEncryptResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BatchDecrypt }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "batchDecrypt")
    public JAXBElement<BatchDecrypt> createBatchDecrypt(BatchDecrypt value) {
        return new JAXBElement<BatchDecrypt>(_BatchDecrypt_QNAME, BatchDecrypt.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BatchDecryptResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "batchDecryptResponse")
    public JAXBElement<BatchDecryptResponse> createBatchDecryptResponse(BatchDecryptResponse value) {
        return new JAXBElement<BatchDecryptResponse>(_BatchDecryptResponse_QNAME, BatchDecryptResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link PingResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "pingResponse")
    public JAXBElement<PingResponse> createPingResponse(PingResponse value) {
        return new JAXBElement<PingResponse>(_PingResponse_QNAME, PingResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Decrypt }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "decrypt")
    public JAXBElement<Decrypt> createDecrypt(Decrypt value) {
        return new JAXBElement<Decrypt>(_Decrypt_QNAME, Decrypt.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link EncryptResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "encryptResponse")
    public JAXBElement<EncryptResponse> createEncryptResponse(EncryptResponse value) {
        return new JAXBElement<EncryptResponse>(_EncryptResponse_QNAME, EncryptResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GpkDecryptResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "gpkDecryptResponse")
    public JAXBElement<GpkDecryptResponse> createGpkDecryptResponse(GpkDecryptResponse value) {
        return new JAXBElement<GpkDecryptResponse>(_GpkDecryptResponse_QNAME, GpkDecryptResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SearchResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://web.strongkeylite.strongauth.com/", name = "searchResponse")
    public JAXBElement<SearchResponse> createSearchResponse(SearchResponse value) {
        return new JAXBElement<SearchResponse>(_SearchResponse_QNAME, SearchResponse.class, null, value);
    }

}
