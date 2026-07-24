package ch.admin.bit.jeap.jme.processarchive.service.provider;

import ch.admin.bit.jeap.processarchive.plugin.api.archivetype.ArchiveTypeProvider;
import ch.admin.bit.jeap.processarchive.test.decreedocument.v1.DecreeDocument;
import ch.admin.bit.jeap.processarchive.test.decreesummary.v1.DecreeSummary;
import ch.admin.bit.jeap.processarchive.test.diagram.v1.Diagram;
import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JmeArchiveTypeProvider implements ArchiveTypeProvider {


    @Override
    public List<Class<? extends SpecificRecordBase>> getArchiveTypeVersions() {
        return List.of(
                ch.admin.bit.jeap.processarchive.test.decree.v1.Decree.class,
                ch.admin.bit.jeap.processarchive.test.decree.v2.Decree.class,
                ch.admin.bit.jeap.processarchive.test.decree.v3.Decree.class,
                DecreeDocument.class,
                DecreeSummary.class,
                Diagram.class);
    }
}
