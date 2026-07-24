package ch.admin.bit.jeap.jme.processarchive.resource.domain;

import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class DecreeRepository {

    private Map<String, Decree> decrees = new ConcurrentHashMap<>();

    public void saveDecree(Decree decree) {
        decrees.put(decree.getId(), decree);
    }

    public Decree getDecree(String id) {
        return decrees.get(id);
    }

}
