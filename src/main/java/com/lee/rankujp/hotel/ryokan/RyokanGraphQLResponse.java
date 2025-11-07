//package com.lee.rankujp.hotel.ryokan;
//
//import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
//import lombok.Data;
//
//import java.util.List;
//
//@Data
//@JsonIgnoreProperties(ignoreUnknown = true)
//public class RyokanGraphQLResponse {
//    private DataWrapper data;
//
//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class DataWrapper {
//        private PropertyDetailsSearch propertyDetailsSearch;
//    }
//
//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class PropertyDetailsSearch {
//        private List<PropertyDetail> propertyDetails;
//    }
//
//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class PropertyDetail {
//        private long propertyId;
//        private ContentDetail contentDetail;
//    }
//
//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class ContentDetail {
//        private ContentFeatures contentFeatures;
//    }
//
//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class ContentFeatures {
//        private List<FeatureGroup> featureGroups;
//    }
//
//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class FeatureGroup {
//        private long id;          // ex) 35
//        private String name;      // ex) "수영 및 목욕 시설",
//        private String symbol;    // ex) "pool",
//        private List<Feature> features;
//    }
//
//    @Data
//    @JsonIgnoreProperties(ignoreUnknown = true)
//    public static class Feature {
//        private long id;              // ex) 104
//        private String featureName;   // ex) "온천"
//        private String symbol;        // ex) "hot-spring-bath"
//        private boolean available;    // ex) true
//    }
//}
