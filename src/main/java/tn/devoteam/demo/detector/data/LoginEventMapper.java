package tn.devoteam.demo.detector.data;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.devoteam.demo.detector.model.LoginEvent;

/**
 * Mapper domaine → document Couchbase.
 */
@Mapper(componentModel = "spring")
public interface LoginEventMapper {

    @Mapping(target = "id", expression = "java(buildId(event))")
    @Mapping(target = "type", constant = "login_event")
    @Mapping(target = "tsEpoch", expression = "java(event.occurredAt().getEpochSecond())")
    LoginEventDocument toDocument(LoginEvent event);

    default String buildId(LoginEvent event) {
        return "login::" + event.userId() + "::" + event.occurredAt();
    }
}

