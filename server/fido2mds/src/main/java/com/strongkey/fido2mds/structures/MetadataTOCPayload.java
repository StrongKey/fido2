/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import java.math.BigInteger;
import java.util.List;

public class MetadataTOCPayload {
    private String legalHeader;
    private BigInteger no;
    private String nextUpdate;
    private List<MetadataTOCPayloadEntry> entries;

    public String getLegalHeader() {
        return legalHeader;
    }

    public void setLegalHeader(String legalHeader) {
        this.legalHeader = legalHeader;
    }

    public BigInteger getNo() {
        return no;
    }

    public void setNo(BigInteger no) {
        this.no = no;
    }

    public String getNextUpdate() {
        return nextUpdate;
    }

    public void setNextUpdate(String nextUpdate) {
        this.nextUpdate = nextUpdate;
    }

    public List<MetadataTOCPayloadEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<MetadataTOCPayloadEntry> entries) {
        this.entries = entries;
    }
    
    
}
