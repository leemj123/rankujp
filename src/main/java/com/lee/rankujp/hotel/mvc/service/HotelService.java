package com.lee.rankujp.hotel.mvc.service;

import com.lee.rankujp.hotel.infra.Hotel;
import com.lee.rankujp.hotel.infra.QHotel;
import com.lee.rankujp.hotel.mvc.dto.HotelDetailResponse;
import com.lee.rankujp.hotel.mvc.dto.HotelPriceResponse;
import com.lee.rankujp.hotel.mvc.dto.HotelReviewResponse;
import com.lee.rankujp.hotel.repo.HotelRepo;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class HotelService {
    private final JPAQueryFactory jpaQueryFactory;
    private final HotelRepo hotelRepo;
    private final QHotel qHotel = QHotel.hotel;
    //list==================================


    //detail================================

    public HotelDetailResponse HotelDetail(Long id) {
        Hotel hotel = hotelRepo.findById(id).orElseThrow();

        double max = hotel.getAverageBusinessScore();
        int maxLabel = 1;

        if (hotel.getAverageCoupleScore() > max) {
            max = hotel.getAverageCoupleScore();
            maxLabel = 2;
        }
        if (hotel.getAverageSoloScore() > max) {
            max = hotel.getAverageSoloScore();
            maxLabel = 3;
        }
        if (hotel.getAverageFamilyScore() > max) {
            maxLabel = 4;
        }

        return HotelDetailResponse.builder()
                .id(hotel.getId())
                .updateDate(hotel.getUpdateDateTime().toLocalDate())
                .title(hotel.getTitle())
                .description(hotel.getDescription())
                .koName(hotel.getKoName())
                .address(hotel.getAddress())
                .zipcode(hotel.getZipcode())
                .starRating(hotel.getStarRating())
                .longitude(hotel.getLongitude())
                .latitude(hotel.getLatitude())
                .thumbnailImg(hotel.getThumbnailImg())
                .photo2(hotel.getPhoto2())
                .photo3(hotel.getPhoto3())
                .photo4(hotel.getPhoto4())
                .photo5(hotel.getPhoto5())
                .bestCrossedOutRate((int)hotel.getBestCrossedOutRate())
                .bestDailyRate((int)hotel.getBestDailyRate())
                .bestSailPrecent((int)hotel.getBestSailPrecent())
                .priceList(
                        hotel.getPriceList()
                                .stream()
                                .map(HotelPriceResponse::new)
                                .sorted(
                                        Comparator
                                                .comparing(HotelPriceResponse::getSailPercent)
                                                .reversed()
                                                .thenComparing(HotelPriceResponse::getDailyRate)
                                )
                                .toList()
                )
                .preferenceValue(maxLabel)
                .averageAllScore((int)(hotel.getAverageAllScore() *10))
                .averageBusinessScore((int)(hotel.getAverageBusinessScore()*10))
                .averageCoupleScore((int)(hotel.getAverageCoupleScore()*10))
                .averageSoloScore((int)(hotel.getAverageSoloScore()*10))
                .averageFamilyScore((int)(hotel.getAverageFamilyScore()*10))
                .brandReviewList(hotel.getHotelReviewList().stream().map(HotelReviewResponse::new).toList())
                .build();
    }

    //other=================================
//    @Transactional
//    public void updater() {
//
//        List<Hotel> hotels = jpaQueryFactory
//                .selectFrom(qHotel)
//                .fetch();
//
//        //title Point Updater
//        hotels.forEach( (hotel) -> {
//            this.setTitle(hotel);
//            this.setPoint(hotel);
//        });
//
//    }

//    private void setTitle(Hotel h) {
//        String value = "";
//        h.titleUpdater();
//    }
//    private void setPoint(Hotel h) {
//
//        PointLocation nearest = null;
//        float minDistance = (float) Double.MAX_VALUE;
//
//        for (PointLocation p : PointLocation.values()) {
//            if (distance < minDistance) {
//                minDistance = distance;
//                nearest = p;
//            }
//        }
//
//        h.(nearest);
//
//    }
}
