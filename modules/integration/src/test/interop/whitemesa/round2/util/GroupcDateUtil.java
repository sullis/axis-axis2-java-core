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

package test.interop.whitemesa.round2.util;

import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.SOAPFactory;
import org.apache.ws.commons.soap.SOAPHeader;
import org.apache.ws.commons.soap.SOAPHeaderBlock;
import test.interop.whitemesa.SunClientUtil;

public class GroupcDateUtil implements SunClientUtil {

    public SOAPEnvelope getEchoSoapEnvelope() {

        SOAPFactory omfactory = OMAbstractFactory.getSOAP11Factory();
        SOAPEnvelope reqEnv = omfactory.getDefaultEnvelope();
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/envelope/", "soapenv");
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema", "xsd");
        reqEnv.declareNamespace("http://schemas.xmlsoap.org/soap/encoding/", "SOAP-ENC"); //xmlns:SOAP-ENC="http://schemas.xmlsoap.org/soap/encoding/"
        reqEnv.declareNamespace("http://www.w3.org/2001/XMLSchema-instance", "xsi");
        reqEnv.declareNamespace("http://soapinterop.org/xsd", "s");
        reqEnv.declareNamespace("http://soapinterop.org/", "m");

        SOAPHeader header = omfactory.createSOAPHeader(reqEnv);
        OMNamespace hns = reqEnv.declareNamespace("http://soapinterop.org/echoheader/", "hns"); //xmlns:m0="http://soapinterop.org/echoheader/
        SOAPHeaderBlock block1 = header.addHeaderBlock("echoMeStringRequest", hns);
        block1.addAttribute("xsi:type", "xsd:string", null);
        block1.addChild(omfactory.createText("string"));
        header.addChild(block1);

        SOAPHeaderBlock block2 = header.addHeaderBlock("echoMeStructRequest", hns);
        block2.addAttribute("xsi:type", "s:SOAPStruct", null);

        OMElement h2Val1 = omfactory.createOMElement("varString", null);
        h2Val1.addAttribute("xsi:type", "xsd:string", null);
        h2Val1.addChild(omfactory.createText("string"));

        OMElement h2Val2 = omfactory.createOMElement("varInt", null);
        h2Val2.addAttribute("xsi:type", "xsd:int", null);
        h2Val2.addChild(omfactory.createText("150"));

        OMElement h2Val3 = omfactory.createOMElement("varFloat", null);
        h2Val3.addAttribute("xsi:type", "xsd:float", null);
        h2Val3.addChild(omfactory.createText("456.321"));

        block2.addChild(h2Val1);
        block2.addChild(h2Val2);
        block2.addChild(h2Val3);

        OMElement operation = omfactory.createOMElement("echoDate", "http://soapinterop.org/", null);
        reqEnv.getBody().addChild(operation);
        operation.addAttribute("soapenv:encodingStyle", "http://schemas.xmlsoap.org/soap/encoding/", null);

        OMElement part = omfactory.createOMElement("inputDate", null);
        part.addAttribute("xsi:type", "xsd:dateTime", null);
        part.addChild(omfactory.createText("2002-07-18T19:40:30.387-06:00"));

        operation.addChild(part);

        return reqEnv;

    }
}
