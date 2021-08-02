package com.matchpointgps.fcm.xmpp.server;

import com.matchpointgps.fcm.xmpp.service.PayloadProcessor;
import com.matchpointgps.fcm.xmpp.service.impl.EchoProcessor;
import com.matchpointgps.fcm.xmpp.service.impl.MessageProcessor;
import com.matchpointgps.fcm.xmpp.service.impl.RegisterProcessor;
import com.matchpointgps.fcm.xmpp.util.Util;

/**
 * Manages the creation of different payload processors based on the desired
 * action
 */

public class ProcessorFactory {

	public static PayloadProcessor getProcessor(String action) {
		if (action == null) {
			throw new IllegalStateException("ProcessorFactory: Action must not be null");
		}
		if (action.equals(Util.BACKEND_ACTION_REGISTER)) {
			return new RegisterProcessor();
		} else if (action.equals(Util.BACKEND_ACTION_ECHO)) {
			return new EchoProcessor();
		} else if (action.equals(Util.BACKEND_ACTION_MESSAGE)) {
			return new MessageProcessor();
		}
		throw new IllegalStateException("ProcessorFactory: Action " + action + " is unknown");
	}
}
