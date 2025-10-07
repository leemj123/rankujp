package com.lee.rankujp.big;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BigService {

    private final JPAQueryFactory jpaQueryFactory;
    private final QAgodaJa qAgodaJa = QAgodaJa.agodaJa;
    public List<JaNameDto> hello(List<Long> ids) {
        return jpaQueryFactory
                .select(Projections.constructor(
                        JaNameDto.class,
                        qAgodaJa.C1,   // long id
                        qAgodaJa.C8    // String name
                ))
                .from(qAgodaJa)
                .where(qAgodaJa.C1.in(ids))
                .fetch();
    }
}
