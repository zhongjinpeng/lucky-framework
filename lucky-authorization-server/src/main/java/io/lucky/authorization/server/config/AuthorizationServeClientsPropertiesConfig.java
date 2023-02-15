package io.lucky.authorization.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConfigurationProperties(prefix = "authorization-server")
public class AuthorizationServeClientsPropertiesConfig {

    private List<AuthorizationServeClientConfig> clients;

    public List<AuthorizationServeClientConfig> getClients() {
        return clients;
    }

    public void setClients(List<AuthorizationServeClientConfig> clients) {
        this.clients = clients;
    }

    @Override
    public String toString() {
        return "AuthorizationServeClientsPropertiesConfig{" +
                "clients=" + clients +
                '}';
    }

    public static class AuthorizationServeClientConfig {

        /**
         * 客户端id
         */
        private String clientId;

        /**
         * 客户端能够访问的资源
         */
        private String[] resourceIds;

        /**
         * 客户端是否需要密码验证
         */
        private Boolean isSecretRequired = true;

        /**
         * 客户端密码
         */
        private String clientSecret;

        /**
         * 客户端访问是否被限制在某个范围
         */
        private Boolean isScoped = true;

        /**
         * 客户端访问范围
         */
        private String scope;

        /**
         * 客户端支持的授权类型
         */
        private String[] authorizedGrantTypes;

        /**
         * 预定义的重定向url
         */
        private String[] registeredRedirectUris;

        /**
         * access token 有效时间
         */
        private Integer accessTokenValiditySeconds;

        /**
         * refresh token 有效时间
         */
        private Integer refreshTokenValiditySeconds;

        /**
         * 客户端是否需要特定范围的用户批准
         */
        private Boolean isAutoApprove = false;

        /**
         * 客户端附加信息
         */
        private Map<String, Object> additionalInformation = new HashMap<>(16);

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String[] getResourceIds() {
            return resourceIds;
        }

        public void setResourceIds(String[] resourceIds) {
            this.resourceIds = resourceIds;
        }

        public Boolean getSecretRequired() {
            return isSecretRequired;
        }

        public void setSecretRequired(Boolean secretRequired) {
            isSecretRequired = secretRequired;
        }

        public String getClientSecret() {
            return clientSecret;
        }

        public void setClientSecret(String clientSecret) {
            this.clientSecret = clientSecret;
        }

        public Boolean getScoped() {
            return isScoped;
        }

        public void setScoped(Boolean scoped) {
            isScoped = scoped;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String[] getAuthorizedGrantTypes() {
            return authorizedGrantTypes;
        }

        public void setAuthorizedGrantTypes(String[] authorizedGrantTypes) {
            this.authorizedGrantTypes = authorizedGrantTypes;
        }

        public String[] getRegisteredRedirectUris() {
            if(null == this.registeredRedirectUris){
                return new String[]{};
            }
            return registeredRedirectUris;
        }

        public void setRegisteredRedirectUris(String[] registeredRedirectUris) {
            this.registeredRedirectUris = registeredRedirectUris;
        }

        public Integer getAccessTokenValiditySeconds() {
            return accessTokenValiditySeconds;
        }

        public void setAccessTokenValiditySeconds(Integer accessTokenValiditySeconds) {
            this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        }

        public Integer getRefreshTokenValiditySeconds() {
            return refreshTokenValiditySeconds;
        }

        public void setRefreshTokenValiditySeconds(Integer refreshTokenValiditySeconds) {
            this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
        }

        public Boolean getAutoApprove() {
            return isAutoApprove;
        }

        public void setAutoApprove(Boolean autoApprove) {
            isAutoApprove = autoApprove;
        }

        public Map<String, Object> getAdditionalInformation() {
            return additionalInformation;
        }

        public void setAdditionalInformation(Map<String, Object> additionalInformation) {
            this.additionalInformation = additionalInformation;
        }

        @Override
        public String toString() {
            return "AuthorizationServeClientConfig{" +
                    "clientId='" + clientId + '\'' +
                    ", resourceIds=" + Arrays.toString(resourceIds) +
                    ", isSecretRequired=" + isSecretRequired +
                    ", clientSecret='" + clientSecret + '\'' +
                    ", isScoped=" + isScoped +
                    ", scope='" + scope + '\'' +
                    ", authorizedGrantTypes=" + Arrays.toString(authorizedGrantTypes) +
                    ", registeredRedirectUris=" + Arrays.toString(registeredRedirectUris) +
                    ", accessTokenValiditySeconds=" + accessTokenValiditySeconds +
                    ", refreshTokenValiditySeconds=" + refreshTokenValiditySeconds +
                    ", isAutoApprove=" + isAutoApprove +
                    ", additionalInformation=" + additionalInformation +
                    '}';
        }
    }
}
