package com.backend.allreva.survey.query.application.response;

import com.backend.allreva.survey.infra.elasticsearch.SurveyDocument;

import java.time.LocalDate;

public record SurveyThumbnail(
        Long id,
        String title,
        String region,
        Integer participantNum,
        LocalDate edDate
) {
        public static SurveyThumbnail from(final SurveyDocument surveyDocument) {
            return new SurveyThumbnail(
                    Long.parseLong(surveyDocument.getId()),
                    surveyDocument.getTitle(),
                    surveyDocument.getRegion(),
                    surveyDocument.getParticipationCount(),
                    surveyDocument.getEdDate()
            );
        }
}
