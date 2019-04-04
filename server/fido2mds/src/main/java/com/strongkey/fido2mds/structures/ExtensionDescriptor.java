/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

public class ExtensionDescriptor {
    private String id;
    private Short tag;
    private String data;
    private Boolean fail_if_unknown;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Short getTag() {
        return tag;
    }

    public void setTag(Short tag) {
        this.tag = tag;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getFail_if_unknown() {
        return fail_if_unknown;
    }

    public void setFail_if_unknown(Boolean fail_if_unknown) {
        this.fail_if_unknown = fail_if_unknown;
    }
}
