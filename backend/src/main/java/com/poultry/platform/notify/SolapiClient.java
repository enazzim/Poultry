package com.poultry.platform.notify;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Solapi Messages API client — shared by SMS and Alimtalk adapters.
 * Replaceable later with company-owned gateway without changing NotifyPort.
 */
@Component
public class SolapiClient {

    private static final Logger log = LoggerFactory.getLogger(SolapiClient.class);
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private final NotifyProperties properties;
    private final RestClient restClient = RestClient.create();

    public SolapiClient(NotifyProperties properties) {
        this.properties = properties;
    }

    public String sendSms(String to, String text) {
        if (!properties.getSolapi().isReady()) {
            log.info("[NOTIFY-DRY-RUN] SMS to={} text={}", mask(to), text);
            return "dry-run-sms";
        }
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("to", normalizePhone(to));
        message.put("from", properties.getSolapi().getSender());
        message.put("text", text);
        return post(Map.of("message", message));
    }

    public String sendAlimtalk(String to, String text, Map<String, String> variables) {
        if (!properties.getSolapi().isReady() || !properties.getKakao().isReady()) {
            log.info("[NOTIFY-DRY-RUN] ALIMTALK to={} text={} vars={}", mask(to), text, variables);
            return "dry-run-alimtalk";
        }
        Map<String, Object> kakaoOptions = new LinkedHashMap<>();
        kakaoOptions.put("pfId", properties.getKakao().getPfId());
        kakaoOptions.put("templateId", properties.getKakao().getTemplateId());
        if (variables != null && !variables.isEmpty()) {
            kakaoOptions.put("variables", variables);
        }

        Map<String, Object> message = new LinkedHashMap<>();
        message.put("to", normalizePhone(to));
        message.put("from", properties.getSolapi().getSender());
        message.put("text", text);
        message.put("kakaoOptions", kakaoOptions);
        return post(Map.of("message", message));
    }

    @SuppressWarnings("unchecked")
    private String post(Map<String, Object> body) {
        String date = ZonedDateTime.now(ZoneOffset.UTC).format(DATE_FMT);
        String salt = UUID.randomUUID().toString().replace("-", "");
        String signature = hmacSha256(date + salt, properties.getSolapi().getApiSecret());
        String auth = "HMAC-SHA256 apiKey=" + properties.getSolapi().getApiKey()
                + ", date=" + date
                + ", salt=" + salt
                + ", signature=" + signature;

        Map<String, Object> response = restClient.post()
                .uri(properties.getSolapi().getApiUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", auth)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null) {
            throw new IllegalStateException("Empty Solapi response");
        }
        Object groupId = response.get("groupId");
        return groupId != null ? String.valueOf(groupId) : "solapi-ok";
    }

    private static String hmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("HMAC failed", e);
        }
    }

    private static String normalizePhone(String phone) {
        return phone == null ? "" : phone.replaceAll("[^0-9]", "");
    }

    private static String mask(String phone) {
        String n = normalizePhone(phone);
        if (n.length() < 4) {
            return "****";
        }
        return n.substring(0, 3) + "****" + n.substring(n.length() - 2);
    }
}
