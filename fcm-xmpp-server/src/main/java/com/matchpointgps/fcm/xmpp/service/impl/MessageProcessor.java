package com.matchpointgps.fcm.xmpp.service.impl;

import com.matchpointgps.fcm.xmpp.bean.CcsInMessage;
import com.matchpointgps.fcm.xmpp.bean.CcsOutMessage;
import com.matchpointgps.fcm.xmpp.server.CcsClient;
import com.matchpointgps.fcm.xmpp.server.MessageHelper;
import com.matchpointgps.fcm.xmpp.service.PayloadProcessor;
import com.matchpointgps.fcm.xmpp.util.Util;

/**
 * Handles an upstream message request
 */
public class MessageProcessor implements PayloadProcessor {

	@Override
	public void handleMessage(CcsInMessage inMessage) {
		CcsClient client = CcsClient.getInstance();
		String messageId = Util.getUniqueMessageId();
		String to = inMessage.getDataPayload().get(Util.PAYLOAD_ATTRIBUTE_RECIPIENT);

		// TODO: handle the data payload sent to the client device. Here, I just
		// resend the incoming message.
		CcsOutMessage outMessage = new CcsOutMessage(to, messageId, inMessage.getDataPayload());
		String jsonRequest = MessageHelper.createJsonOutMessage(outMessage);
		client.send(jsonRequest);
	}

}
