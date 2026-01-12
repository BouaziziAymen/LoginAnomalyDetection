package tn.devoteam.demo.detector.data;

import com.couchbase.client.java.json.JsonObject;
import com.couchbase.client.java.query.QueryOptions;
import lombok.RequiredArgsConstructor;
import org.springframework.data.couchbase.core.CouchbaseTemplate;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LoginEventStatsDao {

    private final CouchbaseTemplate template;

    public Optional<Long> findLastLoginEpoch(String userId) {

        // If your docs sometimes don't have "type", DON'T filter by it.
        String n1ql = """
            SELECT RAW MAX(l.tsEpoch)
            FROM `security`.`_default`.`login` l
            WHERE l.userId = $userId
        """;

        return template.getCouchbaseClientFactory()
                .getCluster()
                .query(n1ql, QueryOptions.queryOptions()
                        .parameters(JsonObject.create().put("userId", userId)))
                .rowsAs(Long.class)
                .stream()
                .filter(Objects::nonNull)
                .findFirst();
    }
}
