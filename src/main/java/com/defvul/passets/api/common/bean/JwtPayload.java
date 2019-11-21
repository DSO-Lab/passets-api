package com.defvul.passets.api.common.bean;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class JwtPayload {
    private List<String> scope;
    private String iss;
    private Long exp;
    private List<String> authorities;
    private String jti;
    @JsonProperty("client_id")
    private String clientId;
    @JsonProperty("user_name")
    private String username;

    public JwtPayload() {
    }

    public List<String> getScope() {
        return this.scope;
    }

    public void setScope(List<String> scope) {
        this.scope = scope;
    }

    public String getIss() {
        return this.iss;
    }

    public void setIss(String iss) {
        this.iss = iss;
    }

    public Long getExp() {
        return this.exp;
    }

    public void setExp(Long exp) {
        this.exp = exp;
    }

    public List<String> getAuthorities() {
        return this.authorities;
    }

    public void setAuthorities(List<String> authorities) {
        this.authorities = authorities;
    }

    public String getJti() {
        return this.jti;
    }

    public void setJti(String jti) {
        this.jti = jti;
    }

    public String getClientId() {
        return this.clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
