package com.lee.rankujp.hotel.review.scrapper.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AgodaEngagementRequest {

    private String operationName = "propertyDetailsSearch";
    private Map<String, Object> variables;

    private String query = """
        query propertyDetailsSearch($PropertyDetailsRequest: PropertyDetailsRequest!) {
          propertyDetailsSearch(PropertyDetailsRequest: $PropertyDetailsRequest) {
            propertyDetails {
              propertyId
              contentDetail {
                contentEngagement {
                  peopleLooking
                  todayBooking
                }
              }
            }
          }
        }
        """;

    public AgodaEngagementRequest(LocalDate stayDate, LocalDate finDate, List<Long> hotelIds) {
        DateTimeFormatter fmt = DateTimeFormatter.ISO_DATE_TIME;
        this.variables = Map.of(
                "PropertyDetailsRequest", Map.of(
                        "propertyIds", hotelIds
                ),
                "ContentImagesRequest", Map.of(
                        "isApo", false,
                        "isUseNewImageCaption", false,
                        "enableUGCImages", false,
                        "enableUGCMosaic", false
                ),
                "ContentTopicsRequest", Map.of(),
                "GrowthProgramInfoRequest", Map.of(
                        "bookingDate", fmt.format(LocalDate.now().atStartOfDay()),
                        "checkInDate", fmt.format(stayDate.atStartOfDay()),
                        "checkOutDate", fmt.format(finDate.atStartOfDay()),
                        "lengthOfStay", (int) stayDate.until(finDate).getDays(),
                        "rooms", 1,
                        "adults", 2,
                        "children", 0
                ),
                "PriceStreamMetaLabRequest", Map.of(
                        "attributesId", List.of(8)
                )
        );
    }
}
