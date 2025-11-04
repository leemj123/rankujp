package com.lee.rankujp.detected;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CountryHeaderResolver {

    // 1) CDN/프록시가 추가하는 국가 헤더들
    private static final List<String> COUNTRY_HEADER_KEYS = List.of(
            "CF-IPCountry",           // Cloudflare
            "X-AppEngine-Country",    // App Engine/GAE
            "Fastly-Geo-Country",     // Fastly
            "X-Akamai-Edgestat-Country", // Akamai (환경별 상이)
            "X-Geo-Country",          // 일반화
            "X-Forwarded-Country"     // 커스텀
    );

    // 3) 언어 코드만 있을 때 기본 매핑(필요시 확장)
    private static final Map<String, String> LANG_TO_CC = Map.ofEntries(
            Map.entry("ko","KR"), Map.entry("ja","JP"), Map.entry("en","US"),
            Map.entry("zh","CN"), Map.entry("zh-tw","TW"), Map.entry("fr","FR"),
            Map.entry("de","DE"), Map.entry("es","ES"), Map.entry("pt","BR"),
            Map.entry("ru","RU"), Map.entry("it","IT"), Map.entry("nl","NL")
    );

    public String resolveCountry(HttpServletRequest req) {
        // 1) CDN/프록시 국가 헤더
        for (String k : COUNTRY_HEADER_KEYS) {
            String v = req.getHeader(k);
            if (v != null && !v.isBlank()) {
                String cc = v.trim().toUpperCase();
                // 일부 헤더는 "ZZ"나 "XX"처럼 unknown을 줄 수도 있음
                if (cc.length() == 2) return cc;
            }
        }

        // 2) Accept-Language 에서 지역코드 우선 추출
        String al = req.getHeader("Accept-Language");
        String fromAL = parseAcceptLanguageToCC(al);
        if (fromAL != null) return fromAL;

        // 4) 모르면 ZZ
        return "ZZ";
    }

    private String parseAcceptLanguageToCC(String header) {
        if (header == null || header.isBlank()) return null;
        // 예: "ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7"
        String[] tokens = header.split(",");
        for (String t : tokens) {
            String langTag = t.trim().split(";")[0]; // "ko-KR"
            // ko-KR → KR, en-US → US
            String[] parts = langTag.split("[-_]");
            if (parts.length >= 2 && parts[1].length() == 2) {
                return parts[1].toUpperCase();
            }
            // 지역 없음 → 언어 기본 매핑 시도 (ko → KR, ja → JP 등)
            String base = parts[0].toLowerCase();
            String cc = LANG_TO_CC.get(base);
            if (cc != null) return cc;
        }
        return null;
    }
}
