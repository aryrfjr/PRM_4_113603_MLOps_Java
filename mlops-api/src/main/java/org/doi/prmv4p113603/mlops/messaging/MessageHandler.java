package org.doi.prmv4p113603.mlops.messaging;

import org.doi.prmv4p113603.mlops.data.dto.messaging.MessageDto;

public interface MessageHandler {

    boolean canHandle(MessageDto message);

    void handle(MessageDto message);

}
