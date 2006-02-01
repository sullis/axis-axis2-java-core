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

package org.apache.ws.commons.soap;

import org.apache.ws.commons.om.OMTestCase;
import org.apache.ws.commons.soap.impl.llom.builder.StAXSOAPModelBuilder;

public class SOAPMessageTest extends OMTestCase {

    public SOAPMessageTest(String testName) {
        super(testName);
    }

    public void testSOAPMessageCreation(){
        try {
            StAXSOAPModelBuilder soapBuilder = getOMBuilder("");
            SOAPMessage soapMessage = soapBuilder.getSoapMessage();
            assertNotNull(soapMessage);
            assertNotNull(soapMessage.getSOAPEnvelope());
        } catch (Exception e) {
            fail("Exception thrown "+ e);
        }
    }
}
