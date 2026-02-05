package com.medidropbox.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShareUrlResponse {
    /** Full webpage URL for the shared booking (e.g. https://yourapp.com/shared/FCZU78T9). Null if app.share-view-base-url not set. */
    private String shareUrl;
    /** Short code to put in URL path (e.g. FCZU78T9). Use when shareUrl is null: baseUrl + "/shared/" + shortCode */
    private String shortCode;
}
