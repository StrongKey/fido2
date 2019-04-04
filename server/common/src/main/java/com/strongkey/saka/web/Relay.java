
package com.strongkey.saka.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for relay complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="relay">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="did" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="relayurl" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="relayprotocol" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="relayencoding" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="relaycontent" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "relay", propOrder = {
    "did",
    "username",
    "password",
    "relayurl",
    "relayprotocol",
    "relayencoding",
    "relaycontent"
})
public class Relay {

    protected Long did;
    protected String username;
    protected String password;
    protected String relayurl;
    protected String relayprotocol;
    protected String relayencoding;
    protected String relaycontent;

    /**
     * Gets the value of the did property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getDid() {
        return did;
    }

    /**
     * Sets the value of the did property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setDid(Long value) {
        this.did = value;
    }

    /**
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

    /**
     * Gets the value of the relayurl property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelayurl() {
        return relayurl;
    }

    /**
     * Sets the value of the relayurl property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelayurl(String value) {
        this.relayurl = value;
    }

    /**
     * Gets the value of the relayprotocol property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelayprotocol() {
        return relayprotocol;
    }

    /**
     * Sets the value of the relayprotocol property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelayprotocol(String value) {
        this.relayprotocol = value;
    }

    /**
     * Gets the value of the relayencoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelayencoding() {
        return relayencoding;
    }

    /**
     * Sets the value of the relayencoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelayencoding(String value) {
        this.relayencoding = value;
    }

    /**
     * Gets the value of the relaycontent property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelaycontent() {
        return relaycontent;
    }

    /**
     * Sets the value of the relaycontent property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelaycontent(String value) {
        this.relaycontent = value;
    }

}
