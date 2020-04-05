package com.dev.eatit.model;

public class Token {
    private String token;
    private boolean isServerToken;
    //이 토큰이 서버쪽에서 보낸 것인지 클라이언트 쪽에서 보낸 것인지 알려줄 변수. 각 토큰은 firebase에 key로써 저장된다.
    //결국 한 사람 당 토큰 하나와 번호 하나를 가지게 되는 셈.

    public Token() {
    }

    public Token(String token, boolean isServerToken) {
        this.token = token;
        this.isServerToken = isServerToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isServerToken() {
        return isServerToken;
    }

    public void setServerToken(boolean serverToken) {
        isServerToken = serverToken;
    }
}
