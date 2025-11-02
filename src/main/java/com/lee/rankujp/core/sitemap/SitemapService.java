package com.lee.rankujp.core.sitemap;

import com.lee.rankujp.hotel.infra.QHotel;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SitemapService {
    private final JPAQueryFactory jpaQueryFactory;
    private final QHotel qHotel = QHotel.hotel;

    private List<SiteMap> siteMapQueryFactory() {
        List<SiteMap> data = new ArrayList<>();
        data.addAll(
                jpaQueryFactory
                        .select(Projections.constructor(SiteMap.class,
                                qHotel.id,
                                qHotel.updateDateTime
                        ))
                        .from(qHotel)
                        .fetch()
        );

        return data;
    }
//    private List<RssMap> RssQueryFactory() {
//        List<RssMap> data = new ArrayList<>();
//        data.addAll(
//                jpaQueryFactory
//                        .select(Projections.constructor(RssMap.class,
//                                qPost.title,
//                                qPost.description,
//                                qPost.id,
//                                qPost.modifiedDate,
//                                Expressions.constant("detail/")
//                        ))
//                        .from(qPost)
//                        .limit(20)
//                        .orderBy(qPost.modifiedDate.desc())
//                        .fetch()
//        );
//        data.addAll(
//                jpaQueryFactory
//                        .select(Projections.constructor(RssMap.class,
//                                qSnsContent.title,
//                                qSnsContent.description,
//                                qSnsContent.id,
//                                qSnsContent.publishTime,
//                                Expressions.constant("detail/sns-content/")
//                        ))
//                        .from(qSnsContent)
//                        .limit(20)
//                        .orderBy(qSnsContent.publishTime.desc())
//                        .fetch()
//        );
//        // 날짜 기준 내림차순 정렬
//        // 상위 20개만 선택
//        return data.stream()
//                .sorted(Comparator.comparing(RssMap::getModifiedTime).reversed()) // 날짜 기준 내림차순 정렬
//                .limit(20) // 상위 20개만 선택
//                .toList();
//    }
public String makeSiteMap() {
    List<SiteMap> list = siteMapQueryFactory();
    String today = LocalDate.now().toString(); // ✅ 오늘 날짜 (yyyy-MM-dd)

    StringBuilder xml = new StringBuilder();
    xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    xml.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

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

//    public String makeRss() {
//        List<RssMap> latestPosts = this.RssQueryFactory();
//        StringBuilder rss = new StringBuilder();
//        rss.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
//        rss.append("<rss version=\"2.0\">\n");
//        rss.append("<channel>\n");
//        rss.append("""
//
//                <title>하우버 - 일본 생활의 모든 정보를 한눈에</title>
//                <link>https://rankujp.com</link>
//                <description>일본 생활 정보, 워킹홀리데이, 유학등 모든 콘텐츠를 알려드려요!</description>
//                <lastBuildDate>%s</lastBuildDate>
//                <language>ko</language>
//        """.formatted(ZonedDateTime.now().format(DateTimeFormatter.RFC_1123_DATE_TIME)));
//
//        for (RssMap r : latestPosts) {
//            rss.append("""
//                <item>
//                    <title>%s</title>
//                    <link>https://rankujp.com/%s</link>
//                    <description>%s</description>
//                    <pubDate>%s</pubDate>
//                </item>
//            """.formatted(
//                    r.getTitle(),
//                    r.getLink(),
//                    r.getDescription(),
//                    formatToRFC1123(r.getModifiedTime()) // RFC-1123 포맷 적용
//            ));
//        }
//
//        rss.append("</channel>\n");
//        rss.append("</rss>");
//
//        return rss.toString().trim();
//    }
//    private String formatToRFC1123(LocalDateTime localDateTime) {
//        return ZonedDateTime.of(localDateTime, ZoneId.systemDefault()) // 시스템 타임존 적용
//                .format(DateTimeFormatter.RFC_1123_DATE_TIME);
//    }
}
