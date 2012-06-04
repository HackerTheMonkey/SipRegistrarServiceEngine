/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.orange.uklab.mockse.main;

import javax.jbi.component.ComponentContext;
import javax.jbi.messaging.MessageExchange;

/**
 *
 * @author hasanein
 */
public class NmrListener implements Runnable
{
    private ComponentContext componentContext;

    public NmrListener(ComponentContext componentContext)
    {
        this.componentContext = componentContext;
    }

    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                /**
                 * Output some logging information...
                 */
                componentContext.getLogger(this.getClass().getName(), null).info("REITERATING...");
                componentContext.getLogger(this.getClass().getName(), null).info("Starting Thread with ID of..." + Thread.currentThread().getId() + " and Name " + Thread.currentThread().getName());
                /**
                 * Start listening to the NMR
                 */
                componentContext.getLogger(this.getClass().getName(), null).info("Start listening to the NMR...");
                MessageExchange messageExchange = componentContext.getDeliveryChannel().accept();
                componentContext.getLogger(this.getClass().getName(), null).info("MessageExchange has been received by Thread with ID of..." + Thread.currentThread().getId() + " and Name " + Thread.currentThread().getName());
                /**
                 * Extracting the NormalizedMessage, create a new MessageExchange and send it back to the
                 * B2BBC
                 */
//                componentContext.getLogger(this.getClass().getName(), null).info("MessageExchange ID: " + messageExchange.getExchangeId());
//                QName serviceName = new QName("http://www.orange.com/uklab/b2bbc/provider", "B2BBCSipProvideService");
//                InOnly receivedInOnlyME = (InOnly) messageExchange;
//                NormalizedMessage receivedNormalizedMessage = receivedInOnlyME.getInMessage();
//                componentContext.getLogger(this.getClass().getName(), null).info("MESSAGE EXCHANGE RECEIVED::::::::::::::::::");
//                /**
//                 * Check the Sip Message Payload (SDP)
//                 */
//                Object sipMessagePayload = receivedNormalizedMessage.getProperty("com.orange.uklab.b2bbc.SIP_MESSAGE_PAYLOAD");
//                if(sipMessagePayload == null)
//                {
//                    componentContext.getLogger(this.getClass().getName(), null).info("The SipMessagePayload is null");
//                }
//                else
//                {
//                    componentContext.getLogger(this.getClass().getName(), null).info("The SIP Message Payload has been obtained...");
//                }
//
//                /**
//                 * Get two copies of the request message contents as the basis
//                 * for the various responses to be sent.
//                 */
//                DOMSource ringingResponseSource = (DOMSource) receivedNormalizedMessage.getContent();
//                DOMSource okResponseSource = (DOMSource) receivedNormalizedMessage.getContent();
//                /**
//                 * Change the ringing source to include the ringing response
//                 */
//                NormalizedMessageContentReader normalizedMessageContentReader_ringing = new NormalizedMessageContentReader(ringingResponseSource);
//                normalizedMessageContentReader_ringing.replaceHeader("SipRequestLineElement", "SipStatusLineElement", "SIP/2.0 180 RINGING");
//                /**
//                 * Change the OK response to include the OK response
//                 */
//                NormalizedMessageContentReader normalizedMessageContentReader_ok = new NormalizedMessageContentReader(okResponseSource);
//                normalizedMessageContentReader_ok.replaceHeader("SipRequestLineElement", "SipStatusLineElement", "SIP/2.0 200 OK");
//                /**
//                 * Get the Delivery Channel
//                 */
//                DeliveryChannel deliveryChannel = componentContext.getDeliveryChannel();
//                /**
//                 * Create the message exchanges
//                 */
//                InOnly newInOnlyME_ringing = deliveryChannel.createExchangeFactoryForService(serviceName).createInOnlyExchange();
//                InOnly newInOnlyME_ok = deliveryChannel.createExchangeFactoryForService(serviceName).createInOnlyExchange();
//                /**
//                 * Create the normalized messages and assign them into the their respective
//                 * message exchanges.
//                 */
//                NormalizedMessage ringingNormalizedMessage = newInOnlyME_ringing.createMessage();
//                ringingNormalizedMessage.setContent(ringingResponseSource);
//                newInOnlyME_ringing.setInMessage(ringingNormalizedMessage);
//
//                NormalizedMessage okNormalizedMessage = newInOnlyME_ok.createMessage();
//                okNormalizedMessage.setContent(okResponseSource);
//                newInOnlyME_ok.setInMessage(okNormalizedMessage);
//                /**
//                 * Copy the SDP payload into the OK response message
//                 */
//                okNormalizedMessage.setProperty("com.orange.uklab.b2bbc.SIP_MESSAGE_PAYLOAD", receivedNormalizedMessage.getProperty("com.orange.uklab.b2bbc.SIP_MESSAGE_PAYLOAD"));
//                /**
//                 * Send the ringing response
//                 */
//                componentContext.getLogger(this.getClass().getName(), null).info("Sending ME " + newInOnlyME_ringing.getInMessage().toString());
//                deliveryChannel.send(newInOnlyME_ringing);
//                System.out.println("The ringing response has been sent..." + newInOnlyME_ringing.toString());

                /**
                 * Sleep for 10 seconds then answer the call.
                 */
//                try
//                {
//                    Thread.sleep(1000);
//                }
//                catch(Exception ex)
//                {
//                    ex.printStackTrace();
//                }

//                /**
//                 * Construct 200 OK response
//                 */
//                normalizedMessageContentReader.setHeaderFieldTextContent("SipStatusLineElement", "SIP/2.0 200 OK");
//                newInOnlyME_ok.setInMessage(receivedNormalizedMessage);
//                System.out.println("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
//                deliveryChannel.send(newInOnlyME_ok);
//                System.out.println("QQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQQ");
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
