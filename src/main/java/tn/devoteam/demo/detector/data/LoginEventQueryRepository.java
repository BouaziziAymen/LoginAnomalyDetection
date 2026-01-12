package tn.devoteam.demo.detector.data;

import com.couchbase.client.java.json.JsonObject;
import lombok.RequiredArgsConstructor;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.data.couchbase.core.query.StringQuery;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LoginEventQueryRepository {

    private final CouchbaseTemplate template;

    public List<LoginEventDocument> findSince(String userId, Instant since) {

        String statement = """
            SELECT l.id, l.type, l.userId, l.ip, l.country, l.deviceId, l.tsEpoch
            FROM `security`.`_default`.`login` l
            WHERE l.type = 'login_event'
              AND l.userId = $userId
              AND l.tsEpoch >= $since
            ORDER BY l.tsEpoch ASC
        """;

        JsonObject params = JsonObject.create()
                .put("userId", userId)
                .put("since", since.getEpochSecond());

        StringQuery query = new StringQuery(statement);
        query.setNamedParameters(params);

        return template
                .findByQuery(LoginEventDocument.class)
                .matching(query)
                .all();
    }
}
