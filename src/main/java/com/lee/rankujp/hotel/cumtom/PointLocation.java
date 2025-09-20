package com.lee.rankujp.hotel.cumtom;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PointLocation {
    ALL(0,0,0,0),

    OSAKA(0,0,0,0),
    KYOTO(0,0,0,0),
    GOBAE(0,0,0,0),
    NARA(0,0,0,0),

    USJ(34.63536955507671,34.68168066884229,
            135.38468230641618, 135.4824604901906),

    UMEDA(34.688629502690766,34.70327366978438,
            135.48569677541784, 135.50953022910093),

    SHINSAIBASHI(34.66902251186601,34.68116369999818,
            135.49306658805833, 135.51037910501117
    ),
    NAMBA(34.656290744098364,34.66890491328834,
            135.49621375348892, 135.51042706580265),
    TENOJI(34.63856479412372,34.65503213253988,
            135.49856126680922, 135.5241026033304);

    private final double minLat;
    private final double maxLat;
    private final double minLon;
    private final double maxLon;
}
