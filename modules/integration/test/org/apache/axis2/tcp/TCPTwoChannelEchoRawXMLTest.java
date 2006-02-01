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

package org.apache.axis2.tcp;


import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.Echo;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilsTCPServer;
import org.apache.ws.commons.om.OMAbstractFactory;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMFactory;
import org.apache.ws.commons.om.OMNamespace;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

public class TCPTwoChannelEchoRawXMLTest extends TestCase {
    private EndpointReference targetEPR =
            new EndpointReference("tcp://127.0.0.1:"
                    + (UtilServer.TESTING_PORT)
                    + "/axis/services/EchoXMLService/echoOMElement");
    private QName serviceName = new QName("EchoXMLService");
    private QName operationName = new QName("echoOMElement");
    private AxisService service;
    private ConfigurationContext configContext;

    private Log log = LogFactory.getLog(getClass());

    private boolean finish = false;

    public TCPTwoChannelEchoRawXMLTest() {
        super(TCPTwoChannelEchoRawXMLTest.class.getName());
    }

    public TCPTwoChannelEchoRawXMLTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        UtilsTCPServer.start();

        //create and deploy the service
        service =
                Utils.createSimpleService(serviceName,
                        Echo.class.getName(),
                        operationName);
        UtilsTCPServer.deployService(service);

        configContext = UtilServer.createClientConfigurationContext();
    }

    protected void tearDown() throws Exception {
        UtilsTCPServer.stop();
    }

    public void testEchoXMLCompleteASync() throws Exception {
        OMFactory fac = OMAbstractFactory.getOMFactory();

        OMNamespace omNs = fac.createOMNamespace("http://localhost/my", "my");
        OMElement method = fac.createOMElement("echoOMElement", omNs);
        OMElement value = fac.createOMElement("myValue", omNs);
        value.setText("Isaac Asimov, The Foundation Trilogy");
        method.addChild(value);

        ServiceClient sender = null;

        try {
            Options options = new Options();
            options.setTo(targetEPR);
            options.setTransportInProtocol(Constants.TRANSPORT_TCP);
            options.setUseSeparateListener(true);
            options.setAction(operationName.getLocalPart());
            Callback callback = new Callback() {
                public void onComplete(AsyncResult result) {
                    try {
                        result.getResponseEnvelope().serializeAndConsume(XMLOutputFactory.newInstance()
                                .createXMLStreamWriter(System.out));
                    } catch (XMLStreamException e) {
                        onError(e);
                    } finally {
                        finish = true;
                    }
                }

                public void onError(Exception e) {
                    log.info(e.getMessage());
                    finish = true;
                }
            };

            AxisService serviceClient =
                    Utils.createSimpleServiceforClient(serviceName,
                            org.apache.axis2.engine.Echo.class.getName(),
                            operationName);

            sender = new ServiceClient(configContext, serviceClient);
            sender.setOptions(options);

            sender.sendReceiveNonBlocking(operationName, method, callback);

            int index = 0;
            while (!finish) {
                Thread.sleep(1000);
                index++;
                if (index > 10) {
                    throw new AxisFault(
                            "Server was shutdown as the async response take too long to complete");
                }
            }
        } finally {
            if(finish) {
            }
        }

    }
}
