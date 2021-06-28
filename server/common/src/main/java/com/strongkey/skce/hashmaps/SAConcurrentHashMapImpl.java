/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skce.hashmaps;

import com.strongkey.skce.pojos.FIDOSecretKeyInfo;
import com.strongkey.skce.pojos.FidoKeysInfo;
import com.strongkey.skce.pojos.FidoPolicyMDS;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.utilities.skceCommon;
import com.strongkey.skce.utilities.skceConstants;
import com.strongkey.skce.utilities.skceMaps;
import java.util.Collection;
import java.util.Set;

public class SAConcurrentHashMapImpl implements SAHashmap {

    private static SAConcurrentHashMapImpl mapObj = null;

    protected SAConcurrentHashMapImpl() {
        System.out.println("Initiating SACHMImpl");
    }

    public static SAConcurrentHashMapImpl getInstance() {
        if (mapObj == null) {
            mapObj = new SAConcurrentHashMapImpl();
        }
        return mapObj;
    }

    @Override
    public Object put(Integer type, Object key, Object value) {
        if (type == null || key == null || value == null) {
            throw new NullPointerException("Null input");
        }
        switch (type) {
            case skceConstants.MAP_USER_SESSION_INFO:
                return skceMaps.sessionMap.put((String) key, (UserSessionInfo) value);
            case skceConstants.MAP_FIDO_SECRET_KEY:
                return skceMaps.FSKMap.put((String) key, (FIDOSecretKeyInfo) value);
//            case Constants.MAP_USER_KEY_POINTERS:
//                return Common.userkeysMap.put((String) key, (UserKeyPointers) value);
            case skceConstants.MAP_FIDO_KEYS:
                return skceMaps.FIDOkeysmap.put((String) key, (FidoKeysInfo) value);
            case skceConstants.MAP_FIDO_POLICIES:
                return skceMaps.FPMap.put((String) key, (FidoPolicyMDS) value);
            default:
                throw new IllegalArgumentException("Invalid map type");
        }
    }

    @Override
    public Object get(Integer type, Object key) {
        if (type == null || key == null) {
            throw new NullPointerException("Null input");
        }
        switch (type) {
            case skceConstants.MAP_USER_SESSION_INFO:
                return skceMaps.sessionMap.get((String) key);
            case skceConstants.MAP_FIDO_SECRET_KEY:
                return skceMaps.FSKMap.get((String) key);
//            case Constants.MAP_USER_KEY_POINTERS:
//                return Common.userkeysMap.get((String) key);
            case skceConstants.MAP_FIDO_KEYS:
                return skceMaps.FIDOkeysmap.get((String) key);
            case skceConstants.MAP_FIDO_POLICIES:
                return skceMaps.FPMap.get((String) key);
            default:
                throw new IllegalArgumentException("Invalid map type");
        }
    }

    @Override
    public Boolean containsKey(Integer type, Object key) {
        if (type == null || key == null) {
            throw new NullPointerException("Null input");
        }
        switch (type) {
            case skceConstants.MAP_USER_SESSION_INFO:
                return skceMaps.sessionMap.containsKey((String) key);
            case skceConstants.MAP_FIDO_SECRET_KEY:
                return skceMaps.FSKMap.containsKey((String) key);
//            case Constants.MAP_USER_KEY_POINTERS:
//                return Common.userkeysMap.containsKey((String) key);
            case skceConstants.MAP_FIDO_KEYS:
                return skceMaps.FIDOkeysmap.containsKey((String) key);
            case skceConstants.MAP_FIDO_POLICIES:
                return skceMaps.FPMap.containsKey((String) key);
            default:
                throw new IllegalArgumentException("Invalid map type");
        }
    }

    @Override
    public Object remove(Integer type, Object key) {
        if (type == null || key == null) {
            throw new NullPointerException("Null input");
        }
        switch (type) {
            case skceConstants.MAP_USER_SESSION_INFO:
                return skceMaps.sessionMap.remove((String) key);
            case skceConstants.MAP_FIDO_SECRET_KEY:
                return skceMaps.FSKMap.remove((String) key);
//            case Constants.MAP_USER_KEY_POINTERS:
//                return Common.userkeysMap.remove((String) key);
            case skceConstants.MAP_FIDO_KEYS:
                return skceMaps.FIDOkeysmap.remove((String) key);
            case skceConstants.MAP_FIDO_POLICIES:
                return skceMaps.FPMap.remove((String) key);
            default:
                throw new IllegalArgumentException("Invalid map type");
        }
    }

    @Override
    public Integer size(Integer type) {
        if (type == null) {
            throw new NullPointerException("Null input");
        }
        switch (type) {
            case skceConstants.MAP_USER_SESSION_INFO:
                return skceMaps.sessionMap.size();
            case skceConstants.MAP_FIDO_SECRET_KEY:
                return skceMaps.FSKMap.size();
//            case Constants.MAP_USER_KEY_POINTERS:
//                return Common.userkeysMap.size();
            case skceConstants.MAP_FIDO_KEYS:
                return skceMaps.FIDOkeysmap.size();
            case skceConstants.MAP_FIDO_POLICIES:
                return skceMaps.FPMap.size();
            default:
                throw new IllegalArgumentException("Invalid map type");
        }
    }

    @Override
    public Collection values(Integer type) {
        if (type == null) {
            throw new NullPointerException("Null input");
        }
        switch (type) {
            case skceConstants.MAP_USER_SESSION_INFO:
                return skceMaps.sessionMap.values();
            case skceConstants.MAP_FIDO_SECRET_KEY:
                return skceMaps.FSKMap.values();
//            case Constants.MAP_USER_KEY_POINTERS:
//                return Common.userkeysMap.values();
            case skceConstants.MAP_FIDO_KEYS:
                return skceMaps.FIDOkeysmap.values();
            case skceConstants.MAP_FIDO_POLICIES:
                return skceMaps.FPMap.values();
            default:
                throw new IllegalArgumentException("Invalid map type");
        }
    }

    @Override
    public Set keys(Integer type) {
        if (type == null) {
            throw new NullPointerException("Null input");
        }
        switch (type) {
            case skceConstants.MAP_USER_SESSION_INFO:
                return skceMaps.sessionMap.keySet();
            case skceConstants.MAP_FIDO_SECRET_KEY:
                return skceMaps.FSKMap.keySet();
//            case Constants.MAP_USER_KEY_POINTERS:
//                return Common.userkeysMap.keySet();
            case skceConstants.MAP_FIDO_KEYS:
                return skceMaps.FIDOkeysmap.keySet();
            case skceConstants.MAP_FIDO_POLICIES:
                return skceMaps.FPMap.keySet();
            default:
                throw new IllegalArgumentException("Invalid map type");
        }
    }

    @Override
    public void clear(Integer type) {
        if (type == null) {
            throw new NullPointerException("Null input");
        }
        switch (type) {
            case skceConstants.MAP_USER_SESSION_INFO:
                skceMaps.sessionMap.clear();
                break;
            case skceConstants.MAP_FIDO_SECRET_KEY:
                skceMaps.FSKMap.clear();
                break;
//            case Constants.MAP_USER_KEY_POINTERS:
//                Common.userkeysMap.clear();
//                break;
            case skceConstants.MAP_FIDO_KEYS:
                skceMaps.FIDOkeysmap.clear();
                break;
            case skceConstants.MAP_FIDO_POLICIES:
                skceMaps.FPMap.clear();
                break;
            default:
                throw new IllegalArgumentException("Invalid map type");
        }
    }

    @Override
    public void clean(Integer type) {
        if (type == null) {
            throw new NullPointerException("Null input");
        }
        if (type == skceConstants.MAP_USER_SESSION_INFO) {
            //clean based on time
            Set<String> sessionids = null;
            if (skceMaps.sessionMap != null) {
                sessionids = skceMaps.sessionMap.keySet();
            }

            String agelimit = skceCommon.getConfigurationProperty("skfe.cfg.property.usersession.flush.cutofftime.seconds");

            long sessionagelimit;
            if (agelimit == null || agelimit.trim().isEmpty()) {
                //  if not configured, set it to 30 seconds as default.
                sessionagelimit = 30L;
            } else {
                try {
                    sessionagelimit = Long.parseLong(agelimit);
                    //  if the configured time is < 5 seconds, set it to 5 seconds
                    if (sessionagelimit < 5L) {
                        sessionagelimit = 5L;
                    } //  if the configured time is > 5 minutes, set it to 5 min
                    else if (sessionagelimit > 300L) {
                        sessionagelimit = 300L;
                    }
                } catch (NumberFormatException ex) {
                    //  any exception, default it to 30 seconds.
                    sessionagelimit = 30L;
                }
            }

            if (sessionids != null) {
                for (String k : sessionids) {
                    UserSessionInfo user = skceMaps.sessionMap.get(k);
                    long usersessionage = user.getSessionAge();
                    //  If a session is expired, remove it from the map
                    if (usersessionage >= sessionagelimit) {
                        skceMaps.sessionMap.remove(k);
                    }
                }
            }

        }
//        else if (type == Constants.MAP_USER_KEY_POINTERS) {
//            Set<String> usernames = null;
//            if (Common.userkeysMap != null) {
//                usernames = Common.userkeysMap.keySet();
//            }
//
//            String agelimit = Common.getConfigurationProperty("skce.cfg.property.userkeypointers.flush.cutofftime.seconds");
//            long ukp_entry_agelimit;
//
//            if (agelimit == null || agelimit.trim().isEmpty()) {
//                //  if not configured, set it to 300 seconds (5 minutes) as default.
//                ukp_entry_agelimit = 300L;
//            } else {
//                try {
//                    ukp_entry_agelimit = Long.parseLong(agelimit);
//                    //  if the configured time is < 15 seconds, set it to 15 seconds
//                    if (ukp_entry_agelimit < 15L) {
//                        ukp_entry_agelimit = 15L;
//                    } //  if the configured time is > 15 minutes, set it to 15 min
//                    else if (ukp_entry_agelimit > 900L) {
//                        ukp_entry_agelimit = 900L;
//                    }
//                } catch (NumberFormatException ex) {
//                    //  any exception, default it to 300 seconds (5 min).
//                    ukp_entry_agelimit = 300L;
//                }
//            }
//
//            if (usernames != null) {
//                for (String k : usernames) {
//                    UserKeyPointers ukp = Common.userkeysMap.get(k);
//                    long ukp_age = ukp.getUserKeyPointersAge();
//                    //  If a user key pointer is expired, remove it from the map
//                    if (ukp_age >= ukp_entry_agelimit) {
//                        Common.userkeysMap.remove(k);
//                    }
//                }
//            }
//        }
        else if (type == skceConstants.MAP_FIDO_KEYS) {
            //clean based on time
            Set<String> fkids = null;
            if (skceMaps.FIDOkeysmap != null) {
                fkids = skceMaps.FIDOkeysmap.keySet();
            }
            String agelimit = skceCommon.getConfigurationProperty("skfe.cfg.property.fidokeys.flush.cutofftime.seconds");

            long sessionagelimit;
            if (agelimit == null || agelimit.trim().isEmpty()) {
                //  if not configured, set it to 30 seconds as default.
                sessionagelimit = 30L;
            } else {
                try {
                    sessionagelimit = Long.parseLong(agelimit);
                    //  if the configured time is < 5 seconds, set it to 5 seconds
                    if (sessionagelimit < 5L) {
                        sessionagelimit = 5L;
                    } //  if the configured time is > 5 minutes, set it to 5 min
                    else if (sessionagelimit > 300L) {
                        sessionagelimit = 300L;
                    }
                } catch (NumberFormatException ex) {
                    //  any exception, default it to 30 seconds.
                    sessionagelimit = 30L;
                }
            }

            if (fkids != null) {
                for (String k : fkids) {
                    FidoKeysInfo fkinfo = skceMaps.FIDOkeysmap.get(k);
                    long usersessionage = fkinfo.getFidoKeysInfoAge();
                    //  If a session is expired, remove it from the map
                    if (usersessionage >= sessionagelimit) {
                        skceMaps.FIDOkeysmap.remove(k);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Invalid map type");
        }
    }

}
