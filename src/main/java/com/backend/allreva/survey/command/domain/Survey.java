package com.backend.allreva.survey.command.domain;

import com.backend.allreva.common.event.Events;
import com.backend.allreva.common.model.BaseEntity;
import com.backend.allreva.survey.command.domain.value.Region;
import com.backend.allreva.survey.exception.SurveyInvalidBoardingDateException;
import com.backend.allreva.survey.exception.SurveyNotWriterException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE survey SET deleted_at = NOW() WHERE id = ?")
@Entity
public class Survey extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @OneToMany(mappedBy = "survey", fetch = FetchType.EAGER)
    private List<SurveyBoardingDate> boardingDates = new ArrayList<>();

    @Column(nullable = false)
    private String artistName;

    @Column(nullable = false)
    private Region region;

    @Column(nullable = false, name = "eddate")
    private LocalDate endDate;

    @Column(nullable = false)
    private int maxPassenger;

    @Column(nullable = true)
    private String information;

    @Column(nullable = false)
    private Long concertId;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private boolean isClosed;

    @Builder
    private Survey(final Long memberId,
                   final Long concertId,
                   final String title,
                   final String artistName,
                   final Region region,
                   final LocalDate endDate,
                   final int maxPassenger,
                   final String information) {
        this.memberId = memberId;
        this.concertId = concertId;
        this.title = title;
        this.artistName = artistName;
        this.region = region;
        this.endDate = endDate;
        this.maxPassenger = maxPassenger;
        this.information = information;
        this.isClosed = false;
    }

    public void update(final String title,
                       final Region region,
                       final LocalDate endDate,
                       final int maxPassenger,
                       final String information) {
        this.title = title;
        this.region = region;
        this.endDate = endDate;
        this.maxPassenger = maxPassenger;
        this.information = information;

        Events.raise(new SurveySavedEvent(this));
    }

    public void isWriter(final Long loginMemberId) {

        if (!this.memberId.equals(loginMemberId))
            throw new SurveyNotWriterException();
    }

    public void containsBoardingDate(final LocalDate boardingDate) {
        boolean contain = this.boardingDates.stream()
                .noneMatch(bd -> bd.getDate().equals(boardingDate));

        if (contain)
            throw new SurveyInvalidBoardingDateException();
    }
}
