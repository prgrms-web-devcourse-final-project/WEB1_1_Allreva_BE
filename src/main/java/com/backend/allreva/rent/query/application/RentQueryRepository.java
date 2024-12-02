package com.backend.allreva.rent.query.application;

import com.backend.allreva.rent.command.domain.value.Region;
import com.backend.allreva.rent.query.application.dto.DepositAccountResponse;
import com.backend.allreva.rent.query.application.dto.RentDetailResponse;
import com.backend.allreva.rent.query.application.dto.RentSummaryResponse;
import com.backend.allreva.survey.query.application.dto.SortType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RentQueryRepository {
    Optional<RentDetailResponse> findRentDetailById(Long rentId);
    Optional<DepositAccountResponse> findDepositAccountById(Long rentId);
    List<RentSummaryResponse> findRentSummaries(Region region, SortType sortType, LocalDate lastEndDate, Long lastId,  int pageSize);
}
