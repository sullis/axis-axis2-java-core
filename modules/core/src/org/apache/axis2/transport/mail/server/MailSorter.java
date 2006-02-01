package org.apache.axis2.transport.mail.server;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisEngine;
import org.apache.ws.commons.om.impl.llom.builder.StAXBuilder;
import org.apache.ws.commons.soap.SOAP11Constants;
import org.apache.ws.commons.soap.SOAP12Constants;
import org.apache.ws.commons.soap.SOAPEnvelope;
import org.apache.ws.commons.soap.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

/**
 * This class will be used to sort the messages into normal messages and mails
 * being sent to the Axis engine. If a mail is to be sent to the engine then a
 * new Axis engine is created using the configuration in the MailServer class
 * and the receive method is called.
 */
public class MailSorter {
    protected static Log log = LogFactory.getLog(MailSorter.class.getName());
    Storage st = null;
    private ArrayList sUsers = new ArrayList();

    // Special users. They are hard coded for the time being to axis2-server@localhost and axis2-server@127.0.0.1
    private ConfigurationContext configurationContext = null;
    private boolean actAsMailet = false;

    public MailSorter(Storage st, ConfigurationContext configurationContext) {
        this.st = st;
        sUsers.add("axis2-server@localhost");
        sUsers.add("axis2-server@127.0.0.1");

        if (configurationContext == null) {
            actAsMailet = false;
        } else {
            this.configurationContext = configurationContext;
            actAsMailet = true;
        }
    }

    public void processMail(ConfigurationContext confContext, MimeMessage mimeMessage) {

        // create an Axis server
        AxisEngine engine = new AxisEngine(confContext);
        MessageContext msgContext = null;

        // create and initialize a message context
        try {
            msgContext = new MessageContext();
            msgContext.setConfigurationContext(confContext);
            msgContext.setTransportIn(confContext.getAxisConfiguration()
                    .getTransportIn(new QName(Constants
                    .TRANSPORT_MAIL)));
            msgContext.setTransportOut(confContext.getAxisConfiguration()
                    .getTransportOut(new QName(Constants
                    .TRANSPORT_MAIL)));

            msgContext.setServerSide(true);
            msgContext.setProperty(MailSrvConstants.CONTENT_TYPE, mimeMessage.getContentType());
            msgContext.setWSAAction(getMailHeader(MailSrvConstants.HEADER_SOAP_ACTION,
                    mimeMessage));

            String serviceURL = mimeMessage.getSubject();

            if (serviceURL == null) {
                serviceURL = "";
            }

            String replyTo = ((InternetAddress) mimeMessage.getReplyTo()[0]).getAddress();

            if (replyTo != null) {
                msgContext.setReplyTo(new EndpointReference(replyTo));
            }

            String recepainets = ((InternetAddress) mimeMessage.getAllRecipients()[0]).getAddress();

            if (recepainets != null) {
                msgContext.setTo(new EndpointReference(recepainets + "/" + serviceURL));
            }

            // add the SOAPEnvelope
            String message = mimeMessage.getContent().toString();

            log.info("message[" + message + "]");

            ByteArrayInputStream bais = new ByteArrayInputStream(message.getBytes());
            XMLStreamReader reader =
                    XMLInputFactory.newInstance().createXMLStreamReader(bais);
            String soapNamespaceURI = "";

            if (mimeMessage.getContentType().indexOf(SOAP12Constants.SOAP_12_CONTENT_TYPE) > -1) {
                soapNamespaceURI = SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            } else if (mimeMessage.getContentType().indexOf(SOAP11Constants.SOAP_11_CONTENT_TYPE)
                    > -1) {
                soapNamespaceURI = SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;
            }

            StAXBuilder builder = new StAXSOAPModelBuilder(reader, soapNamespaceURI);
            SOAPEnvelope envelope = (SOAPEnvelope) builder.getDocumentElement();

            msgContext.setEnvelope(envelope);

            if (envelope.getBody().hasFault()) {
                engine.receiveFault(msgContext);
            } else {
                engine.receive(msgContext);
            }
        } catch (Exception e) {
            try {
                if (msgContext != null) {
                    MessageContext faultContext = engine.createFaultMessageContext(msgContext, e);

                    engine.sendFault(faultContext);
                }
            } catch (Exception e1) {
                log.error(e);
            }
        }
    }

    public void sort(String user, MimeMessage msg) {
        if (actAsMailet) {
            if (sUsers.contains(user)) {
                processMail(configurationContext, msg);
            } else {
                st.addMail(user, msg);
            }
        } else {
            st.addMail(user, msg);
        }
    }

    private String getMailHeader(String headerName, MimeMessage mimeMessage) throws AxisFault {
        try {
            String values[] = mimeMessage.getHeader(headerName);

            if (values != null) {
                return values[0];
            } else {
                return null;
            }
        } catch (MessagingException e) {
            throw new AxisFault(e);
        }
    }
}
