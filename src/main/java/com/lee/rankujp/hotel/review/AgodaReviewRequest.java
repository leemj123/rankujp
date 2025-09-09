package com.lee.rankujp.hotel.review;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class AgodaReviewRequest {
    private final long hotelId;
    private final int hotelProviderId;
    private final int demographicId;
    private final int pageNo;
    private final int pageSize;
    private final int sorting;
    private final List<Integer> reviewProviderIds = new ArrayList<>();
    private final boolean isReviewPage;
    private final boolean isCrawlablePage;
    private final int paginationSize;

    public AgodaReviewRequest(long targetId) {
        this.hotelId = targetId;
        this.hotelProviderId = 332;
        this.demographicId = 0;
        this.pageNo = 1;
        this.pageSize = 1;
        this.sorting = 7;
        this.reviewProviderIds.addAll(0, Arrays.asList(332, 3038));
        this.isReviewPage = false;
        this.isCrawlablePage = true;
        this.paginationSize = 5;
    }
}
