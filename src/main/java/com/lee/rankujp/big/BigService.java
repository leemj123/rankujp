package com.lee.rankujp.big;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BigService {

    private final AgodaKoRepo agodaKoRepo;
    private final AgodaCityRepo agodaCityRepo;
    private final JPAQueryFactory jpaQueryFactory;
    private final QAgodaKo qAgodaKo = QAgodaKo.agodaKo;
    private final QAgodaCity qAgodaCity = QAgodaCity.agodaCity;


    @Transactional
    public void citySeparate() {
        List<AgodaCity> entityList = jpaQueryFactory
                .selectFrom(qAgodaCity)
                .fetch();

        for (AgodaCity e : entityList) {
            Long count = jpaQueryFactory
                    .select(qAgodaKo.hotel_id.count())
                    .from(qAgodaKo)
                    .where(qAgodaKo.city_id.eq(e.getCityId()))
                    .fetchOne();

            e.setCount(count != null ? count : 0L);

        }
    }

    public void OnQueue() {

    }
}
