//package com.lee.rankujp.hotel.ryokan;
//
//import lombok.Data;
//
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Map;
//
//@Data
//public class RyokanRequest {
//
//    private String operationName = "propertyDetailsSearch";
//    private Map<String, Object> variables;
//    private String query = """
//        query propertyDetailsSearch($PropertyDetailsRequest: PropertyDetailsRequest!) {
//          propertyDetailsSearch(PropertyDetailsRequest: $PropertyDetailsRequest) {
//            propertyDetails {
//              propertyId
//              contentDetail {
//                contentFeatures {
//                  featureGroups {
//                    id
//                    name
//                    symbol
//                    features {
//                      id
//                      featureName
//                      symbol
//                      available
//                    }
//                  }
//                }
//              }
//            }
//          }
//        }
//        """;
//
//    public RyokanRequest(Long propertyIds, LocalDateTime now, LocalDate strDate, LocalDate finDate) {
//
//        this.variables = Map.of(
//                "PropertyDetailsRequest", Map.of("propertyIds",  Collections.singleton(propertyIds)),
//                "ContentImagesRequest", Map.of(
//                        "isApo", false,
//                        "isUseNewImageCaption", false,
//                        "enableUGCImages", false,
//                        "enableUGCMosaic", false
//                ),
//                "ContentTopicsRequest", Map.of(),
//                "GrowthProgramInfoRequest", Map.of(
//                        "bookingDate", now,
//                        "checkInDate", strDate.atStartOfDay(),
//                        "checkOutDate", finDate.atStartOfDay(),
//                        "lengthOfStay", 2,
//                        "rooms", 1,
//                        "adults", 2,
//                        "children", 0
//                ),
//                "PriceStreamMetaLabRequest", Map.of("attributesId", List.of(8))
//        );
//    }
//}
