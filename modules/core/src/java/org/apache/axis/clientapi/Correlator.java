/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package org.apache.axis.clientapi;

import java.util.HashMap;

public class Correlator {
    private static Correlator instance;

    private static HashMap correlationHash;

    private Correlator() {
        // Exists only to defeat instantiation.

        correlationHash = new HashMap(10);
    }

    public static Correlator getInstance() {
        if (instance == null) {
            instance = new Correlator();
        }
        return instance;
    }

    public void addCorrelationInfo(String msgid, Callback callbackObj) {
        correlationHash.put(msgid, callbackObj);
    }

    public Callback getCorrelationInfo(String MessageID) {
        return (Callback) correlationHash.get(MessageID);
    }

}