package com.backend.allreva.concert.exception.search;

import com.backend.allreva.common.exception.CustomException;

public class EventConsumingException extends CustomException {
    public EventConsumingException() {
        super(SearchErrorCode.EVENT_CONSUMING_FAIL);
    }
}
