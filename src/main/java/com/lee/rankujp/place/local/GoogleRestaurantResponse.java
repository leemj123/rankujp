//package com.lee.rankujp.place.local;
//
//import lombok.Data;
//import lombok.Getter;
//import lombok.Setter;
//
//import java.util.List;
//
//@Getter
//@Setter
//public class GoogleRestaurantResponse {
//
//    private String id;
//    private String internationalPhoneNumber;
//    private String formattedAddress;
//    private Location location;
//    private double rating;
//    private String googleMapsUri;
//    private String websiteUri;
//    private long userRatingCount;
//    private DisplayName displayName;
//
//    private PrimaryTypeDisplayName primaryTypeDisplayName;
//    private String primaryType;
//    private List<Photo> photos;
//
//    private PriceRange priceRange;
//    private ReviewSummary reviewSummary;
//
//
//    @Data
//    public static class Location {
//        private double latitude;
//        private double longitude;
//    }
//    @Data
//    public static class DisplayName {
//        private String text;
//        private String languageCode;
//    }
//    @Data
//    public static class PrimaryTypeDisplayName {
//        private String text;
//        private String languageCode;
//    }
//    @Data
//    public static class Photo {
//        private String name;
//        private int widthPx;
//        private int heightPx;
//        private List<AuthorAttribution> authorAttributions;
//        private String googleMapsUri;
//        @Data
//        public static class AuthorAttribution {
//            private String displayName;
//        }
//    }
//
//    @Data
//    public static class PriceRange {
//        private StartPrice startPrice;
//        private EndPrice endPrice;
//
//        @Data
//        public static class StartPrice {
//            private String currencyCode;
//            private String units;
//        }
//        @Data
//        public static class EndPrice {
//            private String currencyCode;
//            private String units;
//        }
//    }
//    @Data
//    public static class ReviewSummary {
//        private GeminiDes text;
//
//        @Data
//        public static class GeminiDes {
//            private String text;
//            private String languageCode;
//        }
//    }
//}
