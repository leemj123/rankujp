//package com.lee.rankujp.place.local;
//
//import com.amazonaws.services.s3.AmazonS3;
//import com.amazonaws.services.s3.model.ObjectMetadata;
//import com.amazonaws.services.s3.model.PutObjectRequest;
//import com.lee.rankujp.hotel.infra.QHotel;
//import com.lee.rankujp.place.local.GooglePlaceRequest;
//import com.lee.rankujp.place.local.GoogleRestaurantResponse;
//import com.lee.rankujp.place.local.GoogleRestaurantWrapper;
//import com.lee.rankujp.place.local.HotelLocationPoint;
//import com.lee.rankujp.place.infra.*;
//import com.querydsl.core.types.Projections;
//import com.querydsl.jpa.impl.JPAQueryFactory;
//import io.netty.handler.timeout.TimeoutException;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatusCode;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import org.springframework.web.reactive.function.client.WebClientRequestException;
//import org.springframework.web.reactive.function.client.WebClientResponseException;
//import reactor.core.publisher.Mono;
//
//import java.net.URI;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//import java.net.http.HttpResponse;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.time.Duration;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class LegacyRestaurantService {

//    private final WebClient googleWebClient;
//    private final RestaurantRepo restaurantRepo;
//    private final PlaceImgRepo placeImgRepo;
//
//    private final JPAQueryFactory jpaQueryFactory;
//    private final QHotel qHotel = QHotel.hotel;
//    private final QRestaurant qRestaurant = QRestaurant.restaurant;
//    private final QPlaceImg qPlaceImg = QPlaceImg.placeImg;
//
//    @Value("${google.place.key}")
//    String googlePlaceKey;
//    @Value("${aws.s3.bucket-name}")
//    String bucketName;
//
//    private final AmazonS3 amazonS3;
//
//
//
//    public List<Long> addRestaurant(long lastId) {
//
//        List<HotelLocationPoint> pointList = jpaQueryFactory
//                .select(Projections.constructor(
//                        HotelLocationPoint.class,
//                        qHotel.id,
//                        qHotel.latitude,
//                        qHotel.longitude
//                ))
//                .from(qHotel)
//                .limit(500)
//                .where(qHotel.id.gt(lastId))
//                .fetch();
//
////        List<HotelLocationPoint> pointList = jpaQueryFactory
////                .select(Projections.constructor(
////                        HotelLocationPoint.class,
////                        qHotel.id,
////                        qHotel.latitude,
////                        qHotel.longitude
////                ))
////                .from(qHotel)
////                .where(qHotel.id.in())
////                .fetch();
//
//        List<Long> failIdList = new ArrayList<>();
//        for (HotelLocationPoint hlp : pointList) {
//            try {
//                GooglePlaceRequest googlePlaceRequest = new GooglePlaceRequest(hlp.getLatitude(), hlp.getLongitude());
//                GoogleRestaurantWrapper googleRestaurantWrapper = this.googlePlaceFlux(googlePlaceRequest).block();
//                if (googleRestaurantWrapper == null) {log.info("googlePlacesWrapper is null"); continue;}
//
//
//                List<GoogleRestaurantResponse> allPlaces = googleRestaurantWrapper.getPlaces();
//                if (allPlaces == null || allPlaces.isEmpty()) {log.info("googlePlacesWrapper.getPlaces() is empty"); continue;}
//
//
//                // 1️⃣ 모든 Google ID 추출
//                List<String> googleIdCheckList = allPlaces.stream()
//                        .map(GoogleRestaurantResponse::getId)
//                        .toList();
//
//                // 2️⃣ DB에 이미 존재하는 Google ID 목록 조회
//                List<String> existGoogleIdList = jpaQueryFactory
//                        .select(qRestaurant.googlePlaceId)
//                        .from(qRestaurant)
//                        .where(qRestaurant.googlePlaceId.in(googleIdCheckList))
//                        .fetch();
//
//                // 3️⃣ DB에 없는 것만 필터링
//                Set<String> existIdSet = new HashSet<>(existGoogleIdList);
//
//                List<GoogleRestaurantResponse> filteredPlaces = allPlaces.stream()
//                        .filter(place -> !existIdSet.contains(place.getId()))
//                        .toList();
//
//                for (GoogleRestaurantResponse grr : filteredPlaces) {
//                    Restaurant restaurant = new Restaurant(grr);
//
//                    var photos = grr.getPhotos();
//                    if (photos != null && !photos.isEmpty()) {
//
//                        for (GoogleRestaurantResponse.Photo p : photos) {
//                            restaurant.addImage(new PlaceImg(p));
//                        }
//                    }
//
//                    restaurantRepo.save(restaurant);
//                }
//            } catch (Exception e) {
//                log.warn("FILL : {}",hlp.getId());
//                failIdList.add(hlp.getId());
//            } finally {
//                log.info("id : {}",hlp.getId());
//            }
//        }
//        return failIdList;
//    }
//
//    private Mono<GoogleRestaurantWrapper> googlePlaceFlux(GooglePlaceRequest gpr) {
//
//
//        return googleWebClient.post()
//                .accept(MediaType.APPLICATION_JSON)
//                .header("User-Agent", "Mozilla/5.0 (compatible; RankuBot/1.0)")
//                .bodyValue(gpr)
//                .retrieve()
//
//                // ✅ 403 Forbidden → 크레딧 만료 로그 찍고 전체 종료
//                .onStatus(HttpStatusCode::is4xxClientError, response ->
//                        response.bodyToMono(String.class)
//                                .flatMap(body -> {
//                                    if (response.statusCode().value() == 403) {
//                                        log.error("🚫 Google Places API 403 Forbidden - 크레딧 종료로 판단됨. body={}", body);
//                                        // 전체 로직을 종료시키기 위해 에러를 throw
//                                        return Mono.error(new RuntimeException("Google API credit expired"));
//                                    }
//                                    return Mono.error(new RuntimeException("Google API client error: " + body));
//                                })
//                )
//
//                // ✅ 응답이 정상일 때 바디를 파싱
//                .bodyToMono(GoogleRestaurantWrapper.class)
//
//                // ✅ 응답이 아무것도 없을 때 → null 반환
//                .switchIfEmpty(Mono.justOrEmpty(null))
//
//                .onErrorResume(ex -> {
//                    log.error("❌ Google Places 요청 실패 [{}]: {}",
//                            ex.getClass().getSimpleName(), ex.getMessage(), ex);
//
//                    if (ex instanceof WebClientResponseException responseEx) {
//                        log.error("🌐 WebClientResponseException");
//                        log.error("  ↳ Status : {}", responseEx.getStatusCode());
//                        log.error("  ↳ Headers: {}", responseEx.getHeaders());
//                        log.error("  ↳ Body   : {}", responseEx.getResponseBodyAsString());
//                    }
//                    else if (ex instanceof WebClientRequestException requestEx) {
//                        log.error("🌐 WebClientRequestException");
//                        log.error("  ↳ URL     : {}", requestEx.getUri());
//                        log.error("  ↳ Cause   : {}", requestEx.getCause() != null ? requestEx.getCause().getMessage() : "none");
//                    }
//                    else if (ex instanceof TimeoutException) {
//                        log.error("⏰ TimeoutException: 요청 제한 시간 초과");
//                    }
//                    else {
//                        log.error("⚠️ 기타 예외 발생: {}", ex.toString());
//                    }
//
//                    // null 대신 빈 Mono 반환
//                    return Mono.empty();
//                })
//
//                // ✅ 타임아웃
//                .timeout(Duration.ofSeconds(10));
//    }
//
//    public List<Long> fetchAndUpload() {
//        List<Restaurant> restaurantList = jpaQueryFactory
//                .selectFrom(qRestaurant)
//                .where(qRestaurant.id.gt(14))
//                .fetch();
//        List<Long> failId = new ArrayList<>();
//
//        for( Restaurant restaurant : restaurantList ) {
//            List<PlaceImg> placeImgList = jpaQueryFactory
//                    .selectFrom(qPlaceImg)
//                    .where(qPlaceImg.restaurant.id.eq(restaurant.getId()))
//                    .fetch();
//
//            if (placeImgList == null || placeImgList.isEmpty()) {continue;}
//
//            PlaceImg placeImg;
//
//            if (placeImgList.size() < 2) {
//                placeImg = placeImgList.get(0);
//            } else {
//                placeImg = placeImgList.get(1);
//            }
//
//            String url = "https://places.googleapis.com/v1/"
//                    +placeImg.getName()
//                    +"/media?"
//                    +"&maxHeightPx="+placeImg.getHeightPx()
//                    +"&maxWidthPx="+placeImg.getWidthPx()
//                    +"&key="+googlePlaceKey;
//
//
//            Path temp = null;
//            try {
//                // 1) HTTP 요청
//                HttpClient http = HttpClient.newBuilder()
//                        .connectTimeout(Duration.ofSeconds(10))
//                        .followRedirects(HttpClient.Redirect.NORMAL)
//                        .build();
//
//                HttpRequest request = HttpRequest.newBuilder(URI.create(url))
//                        .header("User-Agent", "RankuJP/1.0 (+https://rankujp.com)")
//                        .timeout(Duration.ofSeconds(30))
//                        .GET()
//                        .build();
//
//                // 2) 임시파일로 스트리밍 다운로드
//                temp = Files.createTempFile("img-", ".bin");
//                HttpResponse<Path> resp = http.send(request, HttpResponse.BodyHandlers.ofFile(temp));
//
//                if (resp.statusCode() / 100 != 2) {
//                    log.warn("non 200: {}", restaurant.getId());
//                    failId.add(restaurant.getId());
//                    Files.deleteIfExists(temp);
//                    continue;
//                }
//
//                // 3) Content-Type 추출 (없으면 octet-stream)
//                String contentType = resp.headers()
//                        .firstValue("content-type")
//                        .map(v -> v.split(";")[0].trim())
//                        .orElse("application/octet-stream");
//
//                // 3-1) 화이트리스트(선택): 이미지 외 차단
//                if (!isAllowedContentType(contentType)) {
//                    log.warn("Unsupported content-type: {}, {}", contentType, restaurant.getId());
//                    failId.add(restaurant.getId());
//                    Files.deleteIfExists(temp);
//                    continue;
//                }
//
//                // 3-2) 파일 크기 확인
//                long size = Files.size(temp);
//                if (size <= 0L) {
//                    log.warn("Empty body, {}", restaurant.getId());
//                    failId.add(restaurant.getId());
//                    Files.deleteIfExists(temp);
//                    continue;
//                }
//
//                // 3-3) 확장자 결정 (URL 우선, 없으면 content-type 기반)
//                String ext = guessExtensionFromUrl(url);
//                if (ext == null) ext = guessExtensionFromContentType(contentType);
//                if (ext == null) ext = ".bin";
//
//                // 4) S3 키 생성
//                String fileName = UUID.randomUUID() + restaurant.getTitle() + "." + ext;
//                String nameKey = "restaurants/osaka/" + fileName;
//
//                // 5) 메타데이터 설정 후 업로드 (스트리밍)
//                try (var in = Files.newInputStream(temp)) {
//                    ObjectMetadata meta = new ObjectMetadata();
//                    meta.setContentLength(size);
//                    meta.setContentType(contentType);
//                    meta.setCacheControl("public, max-age=31536000, immutable");
//
//                    PutObjectRequest req = new PutObjectRequest(bucketName, nameKey, in, meta);
//
//                    amazonS3.putObject(req);
//                }
//
//                // 6) 업로드된 S3 URL 반환
//
//                placeImg.thumbnailUpdate(amazonS3.getUrl(bucketName, nameKey).toString());
//                log.info("success: {}", restaurant.getId());
//                placeImgRepo.save(placeImg);
//
//            } catch (InterruptedException ie) {
//                log.warn("Interrupted while fetching image, {}", restaurant.getId());
//                failId.add(restaurant.getId());
//
//            } catch (Exception e) {
//                log.warn("Failed to proxy & upload image, {}", restaurant.getId());
//                failId.add(restaurant.getId());
//            } finally {
//                // 7) 임시파일 정리
//                if (temp != null) {
//                    try { Files.deleteIfExists(temp); } catch (Exception ignore) {}
//                }
//            }
//        }
//        return failId;
//
//    }
//
//    private static boolean isAllowedContentType(String ct) {
//        if (ct == null) return false;
//        return ct.startsWith("image/");
//    }
//
//    private static String guessExtensionFromUrl(String url) {
//        try {
//            String p = URI.create(url).getPath();
//            int dot = p.lastIndexOf('.');
//            if (dot >= 0 && dot > p.lastIndexOf('/')) {
//                String ext = p.substring(dot).toLowerCase();
//                // 쿼리스트링 제거
//                int q = ext.indexOf('?');
//                if (q >= 0) ext = ext.substring(0, q);
//                // 이미지 확장자만 허용
//                if (ext.matches("\\.(jpg|jpeg|png|webp|gif|bmp|svg|avif)")) return ext;
//            }
//        } catch (Exception ignore) {}
//        return null;
//    }
//
//    private static String guessExtensionFromContentType(String ct) {
//        Map<String, String> map = Map.of(
//                "image/jpeg", ".jpg",
//                "image/png",  ".png",
//                "image/webp", ".webp",
//                "image/gif",  ".gif",
//                "image/bmp",  ".bmp",
//                "image/svg+xml", ".svg",
//                "image/avif", ".avif"
//        );
//        return map.get(ct);
//    }
//}
