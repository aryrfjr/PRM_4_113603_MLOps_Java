package org.doi.prmv4p113603.mlops.messaging;

import lombok.AllArgsConstructor;
import org.doi.prmv4p113603.mlops.data.dto.messaging.MessageDto;
import org.doi.prmv4p113603.mlops.domain.RunStatus;
import org.doi.prmv4p113603.mlops.exception.NominalCompositionNotFoundException;
import org.doi.prmv4p113603.mlops.exception.RunNotFoundException;
import org.doi.prmv4p113603.mlops.model.NominalComposition;
import org.doi.prmv4p113603.mlops.model.Run;
import org.doi.prmv4p113603.mlops.repository.NominalCompositionRepository;
import org.doi.prmv4p113603.mlops.repository.RunRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
public class RunSubmittedMessageHandler implements MessageHandler {

    private final RunRepository runRepository;
    private final NominalCompositionRepository nominalCompositionRepository;

    @Override
    public boolean canHandle(MessageDto message) {
        return message.getType().isRunSubmitted();
    }

    @Transactional
    public void handle(MessageDto message) {

        NominalComposition nominalComposition = nominalCompositionRepository.findByName(message.getNominalComposition())
                .orElseThrow(() -> new NominalCompositionNotFoundException(message.getNominalComposition()));

        Run run = runRepository.findByNominalCompositionAndRunNumber(nominalComposition, message.getRunNumber())
                .orElseThrow(() -> new RunNotFoundException(String.valueOf(message.getRunNumber())));

        run.setStatus(RunStatus.EXPLORATION_RUNNING);

    }

}
