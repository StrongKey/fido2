/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.utilities.SKFSCommon;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

@Stateless
public class originVerfierBean implements originVerfierBeanLocal {

    @Override
    public boolean execute(String appid, String origin) {
        try {
            URL originUrl = new URL(origin);
            String originFQDN = originUrl.getHost();
            URL appidUrl = new URL(appid);
            String appidFQDN = appidUrl.getHost();

            //section 3.1.2.1 - appid not https URL
            if (appid.startsWith("http://")) {
                if (originFQDN.equalsIgnoreCase(appidFQDN)) {
                    return true;
                }
            } else {
                //section 3.1.2.3 - facet/origin and appid is same
                if (originFQDN.equalsIgnoreCase(appidFQDN)) {
                    return true;
                }

                //section 3.1.2.4 - fetch trusted facets
                String domain = appidFQDN.startsWith("www.") ? appidFQDN.substring(4) : appidFQDN;
                String allowedtld = domain;

                allowedtld = SKFSCommon.getTLdplusone(domain);

                JsonArray resJsonObj = null;
                JsonReader rdr = null;
                List<String> allowedfacets = new ArrayList<>();
                try {
                    InputStream is = appidUrl.openStream();
                    rdr = Json.createReader(is);

                    JsonObject obj = rdr.readObject();
                    JsonArray results = obj.getJsonArray("trustedFacets");
                    for (JsonObject result : results.getValuesAs(JsonObject.class)) {
                        resJsonObj = result.getJsonArray("ids");
                    }
                } catch (Exception e) {
                    InputStream is = appidUrl.openStream();
                    rdr = Json.createReader(is);
                    JsonArray results = rdr.readArray();
                    resJsonObj = results;
                }

                //parsing facets ids and discarding invalid ones
                for (int i = 0; i < resJsonObj.size(); i++) {
                    String facet = resJsonObj.getString(i);
                    if (facet.startsWith("https")) {
                        URL u = new URL(facet);
                        if (u.getHost().endsWith(allowedtld)) {
                            allowedfacets.add(facet);
                        }
                    }

                }
                if (allowedfacets.contains(origin)) {
                    return true;
                }
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
