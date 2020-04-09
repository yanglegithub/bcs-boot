package com.phy.bcs.common.rest;

import com.phy.bcs.common.util.PublicUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ResultBuilder {
    public static <T> ResponseEntity<CustomMessage<T>> build(CustomMessage customMessage) {
        return new ResponseEntity(customMessage, customMessage.getHttpStatus() == null ? HttpStatus.OK : customMessage.getHttpStatus());
    }

    public static <T> ResponseEntity<CustomMessage<T>> buildOk(String... messages) {
        CustomMessage customMessage = CustomMessage.createSuccess(messages);
        customMessage.setHttpStatus(HttpStatus.OK);
        return new ResponseEntity(CustomMessage.createSuccess(messages), HttpStatus.OK);
    }

    public static <T> ResponseEntity<CustomMessage<T>> buildOk(T data, String... messages) {
        CustomMessage customMessage = CustomMessage.createSuccessData(data, messages);
        customMessage.setHttpStatus(HttpStatus.OK);
        return new ResponseEntity(customMessage, HttpStatus.OK);
    }

    public static <T> ResponseEntity<CustomMessage<T>> buildFailed(String... messages) {
        return buildFailed(null, messages);
    }

    public static <T> ResponseEntity<CustomMessage<T>> buildFailed(T data, HttpStatus httpStatus, String... messages) {
        if (PublicUtils.isEmpty(messages)) {
            messages = new String[]{"failed"};
        }
        CustomMessage warn = CustomMessage.createWarn(data, messages);
        warn.setHttpStatus(httpStatus);

        return new ResponseEntity(warn, httpStatus != null ? httpStatus : HttpStatus.OK);

    }

    public static <T> ResponseEntity<CustomMessage<T>> buildError(T data, String... messages) {
        return buildError(data, null, messages);
    }

    public static <T> ResponseEntity<CustomMessage<T>> buildError(HttpStatus httpStatus, String... messages) {
        return buildError(null, httpStatus, messages);
    }

    public static <T> ResponseEntity<CustomMessage<T>> buildError(T data, HttpStatus httpStatus, String... messages) {
        if (PublicUtils.isEmpty(messages)) {
            messages = new String[]{"error"};
        }
        CustomMessage error = CustomMessage.createError(data, messages);
        error.setHttpStatus(httpStatus);

        return new ResponseEntity(error, httpStatus != null ? httpStatus : HttpStatus.INTERNAL_SERVER_ERROR);

    }

    public static <T> ResponseEntity<CustomMessage<T>> buildFailed(HttpStatus httpStatus, String... messages) {

        return buildFailed(null, httpStatus, messages);
    }

    public static <T> ResponseEntity<CustomMessage<T>> buildFailed(T data, String... messages) {

        return buildFailed(data, HttpStatus.OK, messages);
    }

    public static <T> ResponseEntity<CustomMessage<T>> buildDataOk(T data) {
        String[] msg;
        if (data instanceof BindingResult) {
            List<String> errorsList = new ArrayList();
            BindingResult bindingResult = (BindingResult) data;
            errorsList.addAll(bindingResult.getAllErrors().stream().map(DefaultMessageSourceResolvable::getDefaultMessage).collect(Collectors.toList()));
            data = null;
            msg = new String[errorsList.size()];
            msg = errorsList.toArray(msg);
        } else {
            msg = new String[]{"ok"};
        }
        return buildOk(data, msg);
    }

    public static <T> ResponseEntity<CustomMessage<T>> buildObject(T data) {
        return buildOk(data, new String[]{});
//        return new ResponseEntity(data, HttpStatus.OK);
    }

    public static <T> ResponseEntity<CustomMessage<T>> wrapOrNotFound(Optional<T> maybeResponse) {
        return wrapOrNotFound(maybeResponse, (HttpHeaders) null);
    }

    public static <T> ResponseEntity<CustomMessage<T>> wrapOrNotFound(Optional<T> maybeResponse, HttpHeaders header) {
        return (ResponseEntity) maybeResponse.map((response) -> {
            return ((ResponseEntity.BodyBuilder) ResponseEntity.ok().headers(header)).body(CustomMessage.createSuccessData(response));
        }).orElse(new ResponseEntity(HttpStatus.NOT_FOUND));
    }

    public static <T> ResponseEntity<CustomMessage<T>> buildMessage(boolean success, String... messages) {
        if (success) {
            return buildOk(messages);
        } else {
            return buildFailed(messages);
        }
    }

    public static <T> ResponseEntity<CustomMessage<T>> buildDataMessage(boolean success, T data, String... messages) {
        if (success) {
            return buildOk(data, messages);
        } else {
            return buildFailed(data, messages);
        }
    }

}
