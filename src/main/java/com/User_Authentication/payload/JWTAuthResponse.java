package com.User_Authentication.payload;


public class JWTAuthResponse {
//    private String accessToken;
    private String tokenType="Bearer";
    private String token;

    public JWTAuthResponse(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }


//    public String getAccessToken() {
//        return accessToken;
//    }

    public String getTokenType() {
        return tokenType;
    }

//    public void setAccessToken(String accessToken) {
//        this.accessToken = accessToken;
//    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
