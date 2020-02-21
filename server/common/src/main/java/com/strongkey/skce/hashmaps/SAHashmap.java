/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skce.hashmaps;

import java.util.Collection;
import java.util.Set;

public interface SAHashmap {

    public Object put(Integer type, Object key, Object value);

    public Object get(Integer type, Object key);

    public Boolean containsKey(Integer type, Object key);

    public Object remove(Integer type, Object key);

    public Integer size(Integer type);

    public Collection values(Integer type);

    public Set keys(Integer type);

    public void clear(Integer type);

    public void clean(Integer type);
}
