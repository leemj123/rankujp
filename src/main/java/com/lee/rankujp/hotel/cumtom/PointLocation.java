package com.lee.rankujp.hotel.cumtom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
public enum PointLocation {
    ALL("전체",0,0,0,0),

    OSAKA("오사카시",0,0,0,0),
    KYOTO("교토",0,0,0,0),
    KOBE("고배",0,0,0,0),
    NARA("나라",0,0,0,0),

    USJ("유니버셜스튜디오",34.63536955507671,34.68168066884229,
            135.38468230641618, 135.4824604901906),

    UMEDA("우메다",34.688629502690766,34.70327366978438,
            135.48569677541784, 135.50953022910093),

    SHINSAIBASHI("신사이바시",34.66902251186601,34.68116369999818,
            135.49306658805833, 135.51037910501117),

    NAMBA("난바",34.656290744098364,34.66890491328834,
            135.49621375348892, 135.51042706580265),

    TENOJI("텐노지",34.63856479412372,34.65503213253988,
            135.49856126680922, 135.5241026033304);

    private final String title;
    private final double minLat;
    private final double maxLat;
    private final double minLon;
    private final double maxLon;

    private static final double EPS = 1e-9; // 경계 포함 안정성용

    public boolean contains(double lat, double lon) {
        return (lat >= minLat - EPS && lat <= maxLat + EPS) &&
                (lon >= minLon - EPS && lon <= maxLon + EPS);
    }

    /** 주어진 좌표가 들어가는 포인트를 반환 (겹치지 않으므로 최대 1개) */
    public static Optional<PointLocation> from(double lat, double lon) {
        for (PointLocation p : values()) {
            if (p.contains(lat, lon)) return Optional.of(p);
        }
        return Optional.empty();
    }
}
