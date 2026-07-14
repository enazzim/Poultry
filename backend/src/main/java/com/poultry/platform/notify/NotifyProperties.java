package com.poultry.platform.notify;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.notify")
public class NotifyProperties {

    private final Solapi solapi = new Solapi();
    private final Kakao kakao = new Kakao();
    /** When true, /api/notify/v1/** can be called with JWT (seed for company API). */
    private boolean apiEnabled = true;

    public Solapi getSolapi() { return solapi; }
    public Kakao getKakao() { return kakao; }
    public boolean isApiEnabled() { return apiEnabled; }
    public void setApiEnabled(boolean apiEnabled) { this.apiEnabled = apiEnabled; }

    public static class Solapi {
        private boolean enabled = false;
        private String apiKey = "";
        private String apiSecret = "";
        private String sender = "";
        private String apiUrl = "https://api.solapi.com/messages/v4/send";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getApiSecret() { return apiSecret; }
        public void setApiSecret(String apiSecret) { this.apiSecret = apiSecret; }
        public String getSender() { return sender; }
        public void setSender(String sender) { this.sender = sender; }
        public String getApiUrl() { return apiUrl; }
        public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

        public boolean isReady() {
            return enabled
                    && notBlank(apiKey)
                    && notBlank(apiSecret)
                    && notBlank(sender);
        }

        private static boolean notBlank(String v) {
            return v != null && !v.isBlank();
        }
    }

    public static class Kakao {
        private boolean enabled = false;
        private String pfId = "";
        private String templateId = "";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getPfId() { return pfId; }
        public void setPfId(String pfId) { this.pfId = pfId; }
        public String getTemplateId() { return templateId; }
        public void setTemplateId(String templateId) { this.templateId = templateId; }

        public boolean isReady() {
            return enabled && pfId != null && !pfId.isBlank() && templateId != null && !templateId.isBlank();
        }
    }
}
