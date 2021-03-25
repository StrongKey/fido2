/*
 *  Copyright 2006-2018 WebPKI.org (http://webpki.org).
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.erdtman.jcs;

import java.io.IOException;

/**
 * Number serialization support.
 */
public class NumberToJSON {

    private NumberToJSON() {}

    /**
     * Formats a number according to ES6.<p>
     * This code is emulating 7.1.12.1 of the EcmaScript V6 specification.</p>
     * @param value Value to be formatted
     * @return String representation
     * @throws IOException &nbsp;
     */
    public static String serializeNumber(double value) throws IOException {
        // 1. Check for JSON compatibility.
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IOException("NaN/Infinity are not permitted in JSON");
        }

        // 2.Deal with zero separately.  Note that this test takes "-0.0" as well
        if (value == 0.0) {
            return "0";
        }

        // 3. Call the DtoA algorithm crunchers
        // V8 FastDtoa can't convert all numbers, so try it first but
        // fall back to old DToA in case it fails
        String result = NumberFastDtoa.numberToString(value);
        if (result != null) {
            return result;
        }
        StringBuilder buffer = new StringBuilder();
        NumberDToA.JS_dtostr(buffer, NumberDToA.DTOSTR_STANDARD, 0, value);
        return buffer.toString();
    }
}
