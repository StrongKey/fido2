
package com.strongkey.saka.web;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for gpkDecrypt complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="gpkDecrypt">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="did" type="{http://www.w3.org/2001/XMLSchema}long" minOccurs="0"/>
 *         &lt;element name="username" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="password" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="gpktoken" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ciphertext" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="encoding" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="algorithm" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="iv" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="aad" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "gpkDecrypt", propOrder = {
    "did",
    "username",
    "password",
    "gpktoken",
    "ciphertext",
    "encoding",
    "algorithm",
    "iv",
    "aad"
})
public class GpkDecrypt {

    protected Long did;
    protected String username;
    protected String password;
    protected String gpktoken;
    protected String ciphertext;
    protected String encoding;
    protected String algorithm;
    protected String iv;
    protected String aad;

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
     * Gets the value of the gpktoken property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGpktoken() {
        return gpktoken;
    }

    /**
     * Sets the value of the gpktoken property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGpktoken(String value) {
        this.gpktoken = value;
    }

    /**
     * Gets the value of the ciphertext property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCiphertext() {
        return ciphertext;
    }

    /**
     * Sets the value of the ciphertext property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCiphertext(String value) {
        this.ciphertext = value;
    }

    /**
     * Gets the value of the encoding property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the value of the encoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEncoding(String value) {
        this.encoding = value;
    }

    /**
     * Gets the value of the algorithm property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the value of the algorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAlgorithm(String value) {
        this.algorithm = value;
    }

    /**
     * Gets the value of the iv property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIv() {
        return iv;
    }

    /**
     * Sets the value of the iv property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIv(String value) {
        this.iv = value;
    }

    /**
     * Gets the value of the aad property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAad() {
        return aad;
    }

    /**
     * Sets the value of the aad property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAad(String value) {
        this.aad = value;
    }

}
