/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.appliance.utilities;

import com.strongkey.appliance.entitybeans.Domains;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;

public class DomainObject
{
    private Domains domain = null;
    private SecureRandom secrand = null;
    private Boolean useprng = Boolean.FALSE;
    private static SortedMap<String, String> config = null;
    private int counter = 0;
    private long lastused;

    DomainObject(Domains domain) {
        this.domain = domain;
        config = new ConcurrentSkipListMap<>();
        lastused = System.currentTimeMillis();
    }

    protected void setDomain(Domains domain) {
        this.domain = domain;
        lastused = System.currentTimeMillis();
    }

    protected Domains getDomain() {
        return domain;
    }

    protected void setSecureRandom(String algorithm, byte[] seed) throws NoSuchAlgorithmException {
        secrand = SecureRandom.getInstance(algorithm);
        secrand.setSeed(seed);
        useprng = Boolean.TRUE;
    }

    protected SecureRandom getSecureRandom() {
        return secrand;
    }

    protected String getIv(int size) {
        byte[] randbytes = new byte[size];
        secrand.nextBytes(randbytes);
        String iv = DatatypeConverter.printBase64Binary(randbytes);
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, "DomainObject", "getIv", "APPL-MSG-1047", iv + " [DID=" + domain.getDid().toString() + ", COUNTER=" + counter + "]");
        counter++;
        return iv;
    }

    protected String setConfiguration(String key, String value) {
        config.put(key, value);
        return this.getConfiguration(key);
    }

    protected String getConfiguration(String key) {
        if (config.containsKey(key))
            return config.get(key);
        else
            return null;
    }

    protected void setUsePrng(Boolean useprng) {
        this.useprng = useprng;
    }

    protected Boolean getUsePrng() {
        return useprng;

    }

    protected void setLastUsed(long timeinms) {
        this.lastused = timeinms;
    }

    public long getLastUsed() {
        return lastused;
    }
}
