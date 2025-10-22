package com.lee.rankujp.hotel.mvc.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lee.rankujp.hotel.mvc.dto.CookieForm;
import com.lee.rankujp.hotel.mvc.dto.HotelDetailResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public final class CookieControl {
    private CookieControl() {}

    private static final String VISIT_COOKIE = "ranku_history";
    private static final int MAX_ITEMS = 10;
    private static final int MAX_COOKIE_BYTES = 3800;


    public static void upsertCookie(String url, HotelDetailResponse h, HttpServletRequest req, HttpServletResponse res){
        try {
            var mapper = new ObjectMapper();

            // 1) 기존 쿠키 읽기
            List<CookieForm> list = new ArrayList<>();
            var cookies = req.getCookies();
            if (cookies != null) {
                for (var c : cookies) {
                    if (VISIT_COOKIE.equals(c.getName())) {
                        String json = java.net.URLDecoder.decode(c.getValue(), java.nio.charset.StandardCharsets.UTF_8);
                        if (!json.isBlank()) {
                            list = mapper.readValue(json, new TypeReference<>() {
                            });
                        }
                        break;
                    }
                }
            }

            CookieForm cookieForm = new CookieForm(url, h.getKoName(), h.getThumbnailImg());

            // 2) 중복 제거(같은 URL 삭제) 후 맨 앞에 삽입
            list.removeIf(v -> v.url().equals(cookieForm.url()));
            list.add(0, cookieForm);

            // 3) 최대 개수 유지
            if (list.size() > MAX_ITEMS) {
                list = new java.util.ArrayList<>(list.subList(0, MAX_ITEMS));
            }

            // 4) 직렬화 & 쿠키 용량 가드
            String json = mapper.writeValueAsString(list);

            // 용량 초과 시 뒤에서부터 제거
            while (json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_COOKIE_BYTES && !list.isEmpty()) {
                list.remove(list.size() - 1);
                json = mapper.writeValueAsString(list);
            }

            String encoded = java.net.URLEncoder.encode(json, java.nio.charset.StandardCharsets.UTF_8);
            Cookie cookie = new Cookie(VISIT_COOKIE, encoded);
            cookie.setPath("/");
            cookie.setMaxAge(60 * 60 * 6); // 6시간
            cookie.setHttpOnly(false);
            res.addCookie(cookie);

        } catch (Exception e) {

            log.warn("visit cookie failed", e);
        }
    }
}
