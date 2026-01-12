package tn.devoteam.demo.detector.data;

import lombok.RequiredArgsConstructor;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class LoginEventWriteRepository {

    private final CouchbaseTemplate couchbaseTemplate;

    public void save(LoginEventDocument document) {
        couchbaseTemplate.insertById(LoginEventDocument.class)
                .inCollection("login")
                .one(document);
    }
}

