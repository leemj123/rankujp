package com.lee.rankujp.big;

import com.querydsl.core.types.Projections;
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

//    public List<JaNameDto> hello(List<Long> ids) {
//        return jpaQueryFactory
//                .select(Projections.constructor(
//                        JaNameDto.class,
//                        qAgodaKo.C1,   // long id
//                        qAgodaKo.C8    // String name
//                ))
//                .from(qAgodaJa)
//                .where(qAgodaJa.C1.in(ids))
//                .fetch();
//    }

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
}
