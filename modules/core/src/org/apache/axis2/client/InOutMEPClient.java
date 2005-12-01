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

package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.async.AsyncResult;
import org.apache.axis2.client.async.Callback;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.description.TransportOutDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisEngine;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMException;
import org.apache.axis2.soap.SOAPBody;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.soap.SOAPFault;
import org.apache.axis2.transport.TransportUtils;
import org.apache.axis2.util.CallbackReceiver;
import org.apache.axis2.util.UUIDGenerator;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

/**
 * This class captures the handling of In-Out type method invocations for both blocking
 * and non-blocking calls. The basic API is based on MessageContext and
 * provides more convenient APIs.
 */
public class InOutMEPClient extends MEPClient {


    /**
     * This is used for the receiving the asynchronous messages.
     */
    protected CallbackReceiver callbackReceiver;

    /**
     * Constructs a InOutMEPClient from a ServiceContext.
     * Ideally this should be generated from a WSDL, we do not have it yet.
     * <p/>
     * Following code works for the time being. <p/>
     * <blockquote><pre>
     * ConfigurationContextFactory efac = new ConfigurationContextFactory();
     * // Replace the null with your client repository if any
     * ConfigurationContext sysContext = efac.buildClientConfigurationContext(null);
     * // above line "null" may be a file name if you know the client repssitory
     * <p/>
     * //create new service
     * QName assumedServiceName = new QName("Your Service");
     * AxisService axisService = new AxisService(assumedServiceName);
     * sysContext.getEngineConfig().addService(axisService);
     * ServiceContext service = sysContext.createServiceContext(assumedServiceName);
     * return service;
     * </pre></blockquote>
     * </code>
     *
     * @param serviceContext
     */

    public InOutMEPClient(ServiceContext serviceContext) {
        super(serviceContext, WSDLConstants.MEP_URI_OUT_IN);
        //service context has the engine context set in to it !
        callbackReceiver = new CallbackReceiver();
    }


    /**
     * This method is used to make blocking calls. This is independent of the transport.
     * For e.g. invocation done with this method might
     * <ol>
     * <li>send request via http and receive the response at the same http connection.</li>
     * <li>send request via http and receive the response at a different http connection.</li>
     * <li>send request via an email smtp and receive the response via an email.</li>
     * </ol>
     */

    public MessageContext invokeBlocking(AxisOperation axisop,
                                         final MessageContext msgctx)
            throws AxisFault {

        // The message ID is sent all the time
        String messageID = String.valueOf("uuid:" + UUIDGenerator.getUUID());
        msgctx.setMessageID(messageID);
        //
        if (clientOptions.isUseSeperateListener()) {

            //This mean doing a Request-Response invocation using two channel. If the
            //transport is two way transport (e.g. http) Only one channel is used (e.g. in http cases
            //202 OK is sent to say no repsone avalible). Axis2 get blocked return when the response is avalible.

            SyncCallBack callback = new SyncCallBack();
            //this method call two channel non blocking method to do the work and wait on the callbck
            invokeNonBlocking(axisop, msgctx, callback);
            long index = clientOptions.getTimeOutInMilliSeconds() / 100;
            while (!callback.isComplete()) {
                //wait till the reponse arrives
                if (index-- >= 0) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new AxisFault(e);
                    }
                } else {
                    throw new AxisFault(Messages.getMessage("responseTimeOut"));
                }
            }
            //process the resule of the invocation
            if (callback.envelope != null) {
                MessageContext resMsgctx =
                        new MessageContext(serviceContext.getConfigurationContext());
                resMsgctx.setEnvelope(callback.envelope);
                return resMsgctx;
            } else {
                if (callback.error instanceof AxisFault) {
                    throw (AxisFault) callback.error;
                } else {
                    throw new AxisFault(callback.error);
                }
            }
        } else {
            msgctx.setServiceContext(serviceContext);
            prepareInvocation(axisop, msgctx);
            //This is the Usual Request-Response Sync implemetation
            ConfigurationContext syscontext = serviceContext.getConfigurationContext();
            msgctx.setConfigurationContext(syscontext);

            checkTransport(msgctx);

            OperationContext operationContext = new OperationContext(axisop, serviceContext);
            axisop.registerOperationContext(msgctx, operationContext);
            operationContext.setProperties(clientOptions.getProperties());

            //Send the SOAP Message and receive a response
            MessageContext response =
                    send(msgctx, clientOptions.getListenerTransport());

            //check for a fault and return the result
            SOAPEnvelope resenvelope = response.getEnvelope();
            if (resenvelope.getBody().hasFault()) {
                SOAPFault soapFault = resenvelope.getBody().getFault();
                Exception ex = soapFault.getException();

                if (clientOptions.isExceptionToBeThrownOnSOAPFault()) {
                    //does the SOAPFault has a detail element for Excpetion
                    if (ex != null) {
                        throw new AxisFault(ex);
                    } else {
                        //if detail element not present create a new Exception from the detail
                        String message = "";
                        message = message + "Code =" + soapFault.getCode() == null ? "" :
                                soapFault.getCode().getValue() == null ? "" : soapFault.getCode().getValue().getText();
                        message = message + "Reason =" + soapFault.getReason() == null ? "" :
                                soapFault.getReason().getSOAPText() == null ? "" : soapFault.getReason().getSOAPText().getText();
                        throw new AxisFault(message);
                    }
                }
            }
            return response;
        }
    }

    /**
     * This method is used to make non-blocking calls and is independent of the transport.
     * For e.g. invocation done with this method might
     * <ol>
     * <li>send request via http and receive the response at the same http connection.</li>
     * <li>send request via http and receive the response at a different http connection.</li>
     * <li>send request via an email smtp and receive the response via an email.</li>
     * </ol>
     */
    public void invokeNonBlocking(final AxisOperation axisop,
                                  final MessageContext msgctx,
                                  final Callback callback)
            throws AxisFault {
        prepareInvocation(axisop, msgctx);

        try {
            final ConfigurationContext syscontext =
                    serviceContext.getConfigurationContext();

            AxisEngine engine = new AxisEngine(syscontext);
            checkTransport(msgctx);
            //Use message id all the time!
            String messageID = String.valueOf("uuid:" + UUIDGenerator.getUUID());
            msgctx.setMessageID(messageID);
            ////
            if (clientOptions.isUseSeperateListener()) {
                //the invocation happen via a separate Channel, so we should set up the
                //information need to correlated the response message and invoke the call back

                axisop.setMessageReceiver(callbackReceiver);
                callbackReceiver.addCallback(messageID, callback);

                //set the replyto such that the response will arrive at the transport listener started
                // Note that this will only change the replyTo Address property in the replyTo EPR
                EndpointReference replyToFromTransport = ListenerManager.replyToEPR(
                        serviceContext.getConfigurationContext(),
                        serviceContext.getAxisService().getName().getLocalPart()
                                + "/"
                                + axisop.getName().getLocalPart(),
                        clientOptions.getListenerTransport().getName().getLocalPart());

                if (msgctx.getReplyTo() == null) {
                    msgctx.setReplyTo(replyToFromTransport);
                } else {
                    msgctx.getReplyTo().setAddress(replyToFromTransport.getAddress());
                }

                //create and set the Operation context
                msgctx.setOperationContext(axisop.findOperationContext(msgctx, serviceContext));
                msgctx.setServiceContext(serviceContext);
                msgctx.getOperationContext().setProperties(clientOptions.getProperties());

                //send the message
                engine.send(msgctx);
            } else {
                // here a bloking invocation happens in a new thread, so the
                // progamming model is non blocking
                OperationContext opcontxt = new OperationContext(axisop, serviceContext);
                msgctx.setOperationContext(opcontxt);
                msgctx.setServiceContext(serviceContext);
                opcontxt.setProperties(clientOptions.getProperties());
                serviceContext.getConfigurationContext().getThreadPool().execute(new NonBlockingInvocationWorker(callback, axisop, msgctx));
            }

        } catch (OMException e) {
            throw new AxisFault(e.getMessage(), e);
        } catch (Exception e) {
            throw new AxisFault(e.getMessage(), e);
        }

    }



    protected void configureTransportInformation() throws AxisFault {
        AxisConfiguration axisConfig = this.serviceContext.getConfigurationContext().getAxisConfiguration();
        String listenerTransportProtocol = clientOptions.getListenerTransportProtocol();
        String senderTrasportProtocol = clientOptions.getSenderTrasportProtocol();
        if (axisConfig != null) {
            if (listenerTransportProtocol != null && !"".equals(listenerTransportProtocol)) {
                TransportInDescription transportIn = axisConfig.getTransportIn(new QName(listenerTransportProtocol));
                if (transportIn == null) {
                    throw new AxisFault(Messages.getMessage("unknownTransport", listenerTransportProtocol));
                }
                clientOptions.setListenerTransport(transportIn);
            }
            if (senderTrasportProtocol != null && "".equals(senderTrasportProtocol)) {
                TransportOutDescription transportOut = axisConfig.getTransportOut(new QName(senderTrasportProtocol));
                if (transportOut == null) {
                    throw new AxisFault(Messages.getMessage("unknownTransport", senderTrasportProtocol));
                }
                clientOptions.setSenderTransport(transportOut);
            }
        }

        if (clientOptions.isUseSeperateListener()) {
            //if separate transport is used, start the required listeners
            if (!serviceContext
                    .getConfigurationContext()
                    .getAxisConfiguration()
                    .isEngaged(new QName(Constants.MODULE_ADDRESSING))) {
                throw new AxisFault(Messages.getMessage("2channelNeedAddressing"));
            }
            ListenerManager.makeSureStarted(clientOptions.getListenerTransportProtocol(),
                    serviceContext.getConfigurationContext());
        }
    }


    /**
     * Checks if the transports are identified correctly.
     *
     * @param msgctx
     * @throws AxisFault
     */
    private void checkTransport(MessageContext msgctx) throws AxisFault {
        if (clientOptions.getSenderTransport() == null) {
            clientOptions.setSenderTransport(inferTransport(msgctx.getTo()));
        }
        if (clientOptions.getListenerTransport() == null) {
            clientOptions.setListenerTransport(serviceContext.getConfigurationContext()
                    .getAxisConfiguration()
                    .getTransportIn(clientOptions.getSenderTransport().getName()));
        }

        if (msgctx.getTransportIn() == null) {
            msgctx.setTransportIn(clientOptions.getListenerTransport());
        }
        if (msgctx.getTransportOut() == null) {
            msgctx.setTransportOut(clientOptions.getSenderTransport());
        }

    }

    /**
     * This class acts as a callback that allows users to wait on the result.
     */
    public class SyncCallBack extends Callback {
        private SOAPEnvelope envelope;
        private Exception error;

        public void onComplete(AsyncResult result) {
            this.envelope = result.getResponseEnvelope();
        }

        public void reportError(Exception e) {
            error = e;
        }
    }

    /**
     * Closes the call initiated to the Transport Listeners. If there are multiple
     * requests sent, the call should be closed only when all are are done.
     */
    public void close() throws AxisFault {
        ListenerManager.stop(serviceContext.getConfigurationContext(), clientOptions.getListenerTransport().getName().getLocalPart());
    }

    /**
     * This class is the workhorse for a non-blocking invocation that uses a
     * two way transport.
     */
    private class NonBlockingInvocationWorker implements Runnable {

        private Callback callback;
        private AxisOperation axisop;
        private MessageContext msgctx;

        public NonBlockingInvocationWorker(Callback callback,
                                           AxisOperation axisop,
                                           MessageContext msgctx) {
            this.callback = callback;
            this.axisop = axisop;
            this.msgctx = msgctx;
        }

        public void run() {
            try {
                //send the request and wait for reponse
                MessageContext response =
                        send(msgctx, clientOptions.getListenerTransport());
                //call the callback                        
                SOAPEnvelope resenvelope = response.getEnvelope();
                SOAPBody body = resenvelope.getBody();
                if (body.hasFault()) {
                    Exception ex = body.getFault().getException();
                    if (ex != null) {
                        callback.reportError(ex);
                    } else {
                        //todo this needs to be fixed
                        callback.reportError(new Exception(body.getFault().getReason().getText()));
                    }
                } else {
                    AsyncResult asyncResult = new AsyncResult(response);
                    callback.onComplete(asyncResult);
                }

                callback.setComplete(true);
            } catch (Exception e) {
                callback.reportError(e);
            }
        }
    }

    /**
     * Sends the message using a two way transport and waits for a response
     *
     * @param msgctx
     * @param transportIn
     * @return
     * @throws AxisFault
     */
    public MessageContext send(MessageContext msgctx,
                               TransportInDescription transportIn) throws AxisFault {

        AxisEngine engine = new AxisEngine(msgctx.getConfigurationContext());
        engine.send(msgctx);

        //create the response
        MessageContext response =
                new MessageContext(msgctx.getConfigurationContext(),
                        msgctx.getSessionContext(),
                        msgctx.getTransportIn(),
                        msgctx.getTransportOut());
        response.setProperty(MessageContext.TRANSPORT_IN,
                msgctx.getProperty(MessageContext.TRANSPORT_IN));
        msgctx.getAxisOperation().registerOperationContext(response, msgctx.getOperationContext());
        response.setServerSide(false);
        response.setServiceContext(msgctx.getServiceContext());
        response.setServiceGroupContext(msgctx.getServiceGroupContext());

        //If request is REST we assume the response is REST, so set the variable
        response.setDoingREST(msgctx.isDoingREST());

        SOAPEnvelope resenvelope = TransportUtils.createSOAPMessage(response, msgctx.getEnvelope().getNamespace().getName());

        if (resenvelope != null) {
            response.setEnvelope(resenvelope);
            engine = new AxisEngine(msgctx.getConfigurationContext());
            engine.receive(response);
        } else {
            throw new AxisFault(Messages.getMessage("blockingInvocationExpectsResponse"));
        }
        return response;
    }
}
