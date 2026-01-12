package tn.devoteam.demo.detector.data;

import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.data.couchbase.repository.Query;

import java.util.List;

public interface LoginEventReadRepository extends CouchbaseRepository<LoginEventDocument, String> {

    @Query("""
        SELECT META(l).id AS __id,
               l._class, l.type, l.userId, l.ip, l.country, l.deviceId, l.tsEpoch
        FROM `security`.`_default`.`login` l
        WHERE l.userId = $1
          AND l.tsEpoch >= $2
        ORDER BY l.tsEpoch ASC
    """)
    List<LoginEventDocument> findSince(String userId, long sinceEpoch);
}
