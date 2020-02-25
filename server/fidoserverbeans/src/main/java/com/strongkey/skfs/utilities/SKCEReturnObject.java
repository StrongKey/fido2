/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.utilities;

//import java.io.File;
import java.io.Serializable;
import javax.activation.DataHandler;
import javax.xml.bind.annotation.XmlMimeType;

public class SKCEReturnObject implements Serializable {

    // Server ID
    private Long sid;

    // Request ID
    private Long rid;

    // Holds value of property errorkey.
    private String errorkey;

    // Holds value of property errormsg.
    private String errormsg;

    // Holds value of property exceptionmsg
    private String exceptionmsg;

    // Holds value of property returnval.
    private Object returnval;

    // Holds value of property strid.
    private String strid;

    // Holds value of property messagekey.
    private String messagekey;

    // The actual content being returned.  Client applicatin must cast
    // the object to the specified type and then process it
    private String hash;

    // The datahandler object of the output file
    private DataHandler outDataHandler;

    //long variable to store the response time for the transaction
    private long txtime;

    // Required empty constructor for JAXB
    public SKCEReturnObject() {
    }

    public SKCEReturnObject(Long sid, Long rid) {
        this.sid = sid;
        this.rid = rid;
    }

    /*
     * Get and Set methods for member variables.
     */
    public @XmlMimeType("application/octet-stream")
    DataHandler getOutDataHandler() {
        return outDataHandler;
    }

    public void setOutDataHandler(DataHandler dh) {
        this.outDataHandler = dh;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getResponse() {
        return (String) returnval;
    }

    public void setResponse(String response) {
        this.returnval = response;
    }

    public Long getSid() {
        return sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public Long getRid() {
        return rid;
    }

    public void setRid(Long rid) {
        this.rid = rid;
    }

    public String getErrorkey() {
        return errorkey;
    }

    public void setErrorkey(String errorkey) {
        this.errorkey = errorkey;
    }

    public String getErrormsg() {
        return errormsg;
    }

    public void setErrormsg(String errormsg) {
        this.errormsg = errormsg;
    }

    public String getExceptionmsg() {
        return exceptionmsg;
    }

    public void setExceptionmsg(String exceptionmsg) {
        this.exceptionmsg = exceptionmsg;
    }

    public String getMessagekey() {
        return messagekey;
    }

    public void setMessagekey(String messagekey) {
        this.messagekey = messagekey;
    }

    public Object getReturnval() {
        return returnval;
    }

    public void setReturnval(Object returnval) {
        this.returnval = returnval;
    }

    public String getStrId() {
        return strid;
    }

    public void setStrId(String strid) {
        this.strid = strid;
    }

    public long getTxtime() {
        return txtime;
    }

    public void setTxtime(long txtime) {
        this.txtime = txtime;
    }

    @Override
    public String toString() {
        return "\n sid = " + this.sid
                + "\n rid = " + this.rid
                + "\n errorkey = " + this.errorkey
                + "\n errormsg = " + this.errormsg
                + "\n messagekey = " + this.messagekey
                + "\n returnval = " + this.returnval
                + "\n strid = " + this.strid
                + "\n outDataHandler = " + this.outDataHandler
                + "\n TX Time = " + this.txtime
                + "\n hash = " + this.hash;
    }

    public String toSKEEString() {
        return "\n rid = " + this.rid
                + "\n hash = " + this.hash;
    }

    public String toSKSEString() {
        return "\n rid = " + this.rid
                + "\n response = " + this.returnval;
    }

    public String toLDAPEString() {
        return "\n rid = " + this.rid
                + "\n errorkey = " + this.errorkey
                + "\n errormsg = " + this.errormsg
                + "\n errorkey = " + this.errorkey
                + "\n errormsg = " + this.errormsg
                + "\n returnval = " + this.returnval;
    }

    /**
     * Clean-up method to make sure RV's are correct
     */
    public void cleanUp() {
        this.sid = null;
        this.rid = null;
        this.errorkey = null;
        this.errormsg = null;
        this.messagekey = null;
        this.returnval = null;
        this.strid = null;
        this.hash = null;
        this.outDataHandler = null;
        this.hash = null;
        this.txtime = 0;
    }
}
