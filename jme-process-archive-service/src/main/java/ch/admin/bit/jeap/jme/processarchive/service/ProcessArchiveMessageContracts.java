package ch.admin.bit.jeap.jme.processarchive.service;

import ch.admin.bit.jeap.event.shared.processarchive.archivedartifactversioncreated.SharedArchivedArtifactVersionCreatedEvent;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageConsumerContract;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageConsumerContractsByTemplates;
import ch.admin.bit.jeap.messaging.annotations.JeapMessageProducerContract;
import ch.admin.bit.jeap.processarchive.command.CreateArtifactCommand;

@JeapMessageProducerContract(value = SharedArchivedArtifactVersionCreatedEvent.TypeRef.class, topic = "jme-process-archive-artifactversioncreated")
@JeapMessageProducerContract(value = CreateArtifactCommand.TypeRef.class, topic = "jme-process-archive-createartifact")
@JeapMessageConsumerContract(value = CreateArtifactCommand.TypeRef.class, topic = "jme-process-archive-createartifact")
@JeapMessageConsumerContractsByTemplates
interface ProcessArchiveMessageContracts {
}
