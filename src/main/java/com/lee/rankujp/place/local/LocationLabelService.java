package com.lee.rankujp.place.local;

import com.lee.rankujp.place.infra.QRestaurant;
import com.lee.rankujp.place.infra.QTestRestaurant;
import com.lee.rankujp.place.infra.TestRestaurant;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationLabelService {

    private final JPAQueryFactory jpaQueryFactory;
    private final QTestRestaurant qTestRestaurant = QTestRestaurant.testRestaurant;
    private final QRestaurant qRestaurant = QRestaurant.restaurant;

    @Transactional
    public void addersSeparate(){

        List<TestRestaurant> restaurants = jpaQueryFactory
                .selectFrom(qTestRestaurant)
                .where(qTestRestaurant.formattedAddress.like("%osaka%"))
                .fetch();

        for (TestRestaurant tr: restaurants) {
            tr.setSeparateAdders1("오사카");
        }
    }
}
