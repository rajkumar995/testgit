package com.medidropbox.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * MSG91 Widget API v5 client for MediDropBox.
 * Uses Widget API: sendOtp, verifyOtp, verifyAccessToken, retryOtp.
 */
@Component
public class Msg91OtpClient {

    private static final Logger log = LoggerFactory.getLogger(Msg91OtpClient.class);

    /** Default base URL for MSG91 Widget API v5. Override with msg91.base-url if needed. */
    private static final String DEFAULT_BASE_URL = "https://api.msg91.com/api/v5/widget";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String authKey;
    private final String widgetId;
    private final String baseUrl;

    public Msg91OtpClient(@Value("${msg91.auth-key:}") String authKey,
                          @Value("${msg91.widget-id:}") String widgetId,
                          @Value("${msg91.base-url:}") String baseUrl) {
        this.authKey = authKey;
        this.widgetId = widgetId;
        this.baseUrl = (baseUrl != null && !baseUrl.isBlank()) ? baseUrl.replaceAll("/$", "") : DEFAULT_BASE_URL;
    }

    private String url(String path) {
        return baseUrl + (path.startsWith("/") ? path : "/" + path);
    }

    /**
     * Send OTP via Widget API v5.
     * @param identifier Phone number (e.g. 917274920470)
     * @return Response with reqId (request ID) in message field
     */
    public WidgetSendOtpResponse sendOtp(String identifier) {
        if (authKey == null || authKey.isBlank()) {
            throw new IllegalStateException("msg91.auth-key is not configured");
        }
        if (widgetId == null || widgetId.isBlank()) {
            throw new IllegalStateException("msg91.widget-id is not configured");
        }
        log.info("[MSG91] Calling sendOtp API | identifier={}", maskMobile(identifier));
        Map<String, String> body = Map.of(
                "widgetId", widgetId,
                "identifier", identifier
        );
        WidgetSendOtpResponse response = callWidgetApi(url("sendOtp"), body, WidgetSendOtpResponse.class, "send OTP");
        log.info("[MSG91] sendOtp success | type={}, reqId={}", response.getType(), response.getMessage());
        return response;
    }

    /**
     * Verify OTP via Widget API v5.
     * @param reqId Request ID from sendOtp response
     * @param otp OTP entered by user
     * @return Response with access token (JWT) in message field
     */
    public WidgetVerifyOtpResponse verifyOtp(String reqId, String otp) {
        if (authKey == null || authKey.isBlank()) {
            throw new IllegalStateException("msg91.auth-key is not configured");
        }
        if (widgetId == null || widgetId.isBlank()) {
            throw new IllegalStateException("msg91.widget-id is not configured");
        }
        log.info("[MSG91] Calling verifyOtp API | reqId={}", reqId);
        Map<String, String> body = Map.of(
                "widgetId", widgetId,
                "reqId", reqId,
                "otp", otp
        );
        WidgetVerifyOtpResponse response = callWidgetApi(url("verifyOtp"), body, WidgetVerifyOtpResponse.class, "verify OTP");
        log.info("[MSG91] verifyOtp success | type={}, accessToken present={}", response.getType(), response.getMessage() != null);
        return response;
    }

    /**
     * Verify access token (JWT from verifyOtp) via Widget API v5.
     * @param accessToken JWT token from verifyOtp response
     * @return Response with reqId in message field if verified
     */
    public WidgetVerifyAccessTokenResponse verifyAccessToken(String accessToken) {
        if (authKey == null || authKey.isBlank()) {
            throw new IllegalStateException("msg91.auth-key is not configured");
        }
        log.info("[MSG91] Calling verifyAccessToken API");
        Map<String, String> body = Map.of("access-token", accessToken);
        WidgetVerifyAccessTokenResponse response = callWidgetApi(url("verifyAccessToken"), body, WidgetVerifyAccessTokenResponse.class, "verify access token");
        log.info("[MSG91] verifyAccessToken success | type={}, reqId={}", response.getType(), response.getMessage());
        return response;
    }

    /**
     * Retry OTP via Widget API v5.
     * @param reqId Request ID from sendOtp response
     * @param retryChannel Optional channel ID (e.g. "sms", "voice")
     * @return Response with new reqId in message field
     */
    public WidgetRetryOtpResponse retryOtp(String reqId, String retryChannel) {
        if (authKey == null || authKey.isBlank()) {
            throw new IllegalStateException("msg91.auth-key is not configured");
        }
        if (widgetId == null || widgetId.isBlank()) {
            throw new IllegalStateException("msg91.widget-id is not configured");
        }
        log.info("[MSG91] Calling retryOtp API | reqId={}, channel={}", reqId, retryChannel != null ? retryChannel : "default");
        Map<String, String> body = retryChannel != null && !retryChannel.isBlank()
                ? Map.of("widgetId", widgetId, "reqId", reqId, "retryChannel", retryChannel)
                : Map.of("widgetId", widgetId, "reqId", reqId);
        WidgetRetryOtpResponse response = callWidgetApi(url("retryOtp"), body, WidgetRetryOtpResponse.class, "retry OTP");
        log.info("[MSG91] retryOtp success | type={}, newReqId={}", response.getType(), response.getMessage());
        return response;
    }

    private <T> T callWidgetApi(String url, Map<String, String> body, Class<T> responseType, String operation) {
        log.info("[MSG91] HIT {} | URL={} | body={}", operation, url, body);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authkey", authKey);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
        String raw = null;
        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            raw = response.getBody();
            log.info("[MSG91] RESPONSE {} | status={} | body={}", operation, response.getStatusCode(), raw);
            if (raw == null || raw.isBlank()) {
                throw new RuntimeException("MSG91 Widget " + operation + ": empty response");
            }
            raw = raw.trim();
            if (raw.startsWith("<") || raw.toLowerCase().startsWith("<!doctype")) {
                throw new RuntimeException("MSG91 Widget " + operation + " failed (HTML response). Check auth key, widget ID, and MSG91 dashboard. Response: " + raw.substring(0, Math.min(200, raw.length())) + "...");
            }
            return objectMapper.readValue(raw, responseType);
        } catch (org.springframework.web.client.HttpClientErrorException | org.springframework.web.client.HttpServerErrorException e) {
            raw = e.getResponseBodyAsString();
            log.warn("[MSG91] HTTP_ERROR {} | status={} | body={}", operation, e.getStatusCode(), raw);
            throw new RuntimeException("MSG91 Widget " + operation + " failed (HTTP " + e.getStatusCode() + "): " + (raw != null && !raw.isBlank() ? raw.substring(0, Math.min(300, raw.length())) : e.getMessage()), e);
        } catch (org.springframework.web.client.RestClientException e) {
            log.warn("[MSG91] REST_ERROR {} | message={}", operation, e.getMessage(), e);
            throw new RuntimeException("MSG91 Widget " + operation + " failed: " + e.getMessage(), e);
        } catch (Exception e) {
            String snippet = (raw != null && !raw.isBlank()) ? raw.substring(0, Math.min(500, raw.length())) : "";
            log.warn("[MSG91] PARSE_ERROR {} | rawBody={} | exception={}", operation, snippet, e.getMessage(), e);
            throw new RuntimeException("MSG91 Widget " + operation + ": invalid response. " + (snippet.isEmpty() ? e.getMessage() : "Response: " + snippet), e);
        }
    }

    private static String maskMobile(String mobile) {
        if (mobile == null || mobile.length() < 4) return "****";
        return mobile.substring(0, 3) + "****" + mobile.substring(mobile.length() - 3);
    }

    public boolean isConfigured() {
        return authKey != null && !authKey.isBlank() && widgetId != null && !widgetId.isBlank();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WidgetSendOtpResponse {
        private String type;   // "success" or "error"
        private String message; // reqId (success) or error message (e.g. AuthenticationFailure)
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WidgetVerifyOtpResponse {
        private String type;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WidgetVerifyAccessTokenResponse {
        private String type;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WidgetRetryOtpResponse {
        private String type;
        private String message;
    }
}
