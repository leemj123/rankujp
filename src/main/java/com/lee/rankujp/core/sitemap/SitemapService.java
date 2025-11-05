package com.lee.rankujp.core.sitemap;

import com.lee.rankujp.hotel.infra.QHotel;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SitemapService {
    private final JPAQueryFactory jpaQueryFactory;
    private final QHotel qHotel = QHotel.hotel;

    private final int PER_PAGE = 10000;

    public String siteMapList() {

        // 1️⃣ 전체 데이터 개수
        long dataCount = Optional.ofNullable(
                jpaQueryFactory
                        .select(Wildcard.count)
                        .from(qHotel)
                        .fetchOne()
        ).orElse(0L);

        // 2️⃣ 오늘 날짜
        String today = LocalDate.now().toString();

        long pageCount = (dataCount + PER_PAGE - 1) / PER_PAGE;

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        for (int i = 1; i <= pageCount; i++) {
            xml.append(String.format("""
            <sitemap>
                <loc>https://rankujp.com/sitemap%d.xml</loc>
                <lastmod>%s</lastmod>
            </sitemap>
        """, i, today));
        }

        // 6️⃣ 마무리
        xml.append("</sitemapindex>");
        return xml.toString().trim();
    }

    private List<SiteMap> siteMapQueryFactory(int page) {

        int offset = (page - 1) * PER_PAGE;

        List<SiteMap> data = new ArrayList<>();
        data.addAll(
                jpaQueryFactory
                        .select(Projections.constructor(SiteMap.class,
                                qHotel.id,
                                qHotel.updateDateTime
                        ))
                        .orderBy(qHotel.id.asc())
                        .offset(offset)
                        .limit(PER_PAGE)
                        .from(qHotel)
                        .fetch()
        );

        return data;
    }

    public String makeSiteMap(int page) {
        List<SiteMap> list = siteMapQueryFactory(page);
        String today = LocalDate.now().toString(); // ✅ 오늘 날짜 (yyyy-MM-dd)

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        if (page == 1) {
            // ✅ today 변수를 삽입한 정적 URL 목록
            xml.append(String.format("""
            <url>
                <loc>https://rankujp.com/</loc>
                <lastmod>%s</lastmod>
                <priority>1.0</priority>
            </url>
            <url>
                <loc>https://rankujp.com/?page=2</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/?page=3</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/?page=4</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/?page=5</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/score</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/score?page=2</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/score?page=3</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/score?page=4</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/score?page=5</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/premium</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/premium?page=2</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/premium?page=3</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/premium?page=4</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/premium?page=5</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/restaurant</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/restaurant?page=2</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/restaurant?page=3</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/restaurant?page=4</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/restaurant?page=5</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu</loc>
                <lastmod>%s</lastmod>
                <priority>1.0</priority>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu?page=2</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu?page=3</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu?page=4</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu?page=5</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu/score</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu/score?page=2</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu/score?page=3</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu/score?page=4</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu/score?page=5</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu/restaurant</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu/restaurant?page=2</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu/restaurant?page=3</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu/restaurant?page=4</loc>
                <lastmod>%s</lastmod>
            </url>
            <url>
                <loc>https://rankujp.com/kyushu/restaurant?page=5</loc>
                <lastmod>%s</lastmod>
            </url>
        """,
                    today, today, today, today, today,
                    today, today, today, today, today,
                    today, today, today, today, today,
                    "2025-10-21", "2025-10-21", "2025-10-21", "2025-10-21", "2025-10-21",
                    today, today, today, today, today,
                    today, today, today, today, today,
                    "2025-11-02", "2025-11-02", "2025-11-02", "2025-11-02", "2025-11-02"
            ));
        }

        // ✅ 동적 hotel 리스트 부분 (DB 데이터 기준)
        list.stream()
                .map(s -> """
                <url>
                    <loc>https://rankujp.com/hotel/%s</loc>
                    <lastmod>%s</lastmod>
                </url>
            """.formatted(s.getId(),
                        s.getModifiedTime() != null ? s.getModifiedTime() : today))
                .forEach(xml::append);

        xml.append("</urlset>");
        return xml.toString().trim();
    }

}
