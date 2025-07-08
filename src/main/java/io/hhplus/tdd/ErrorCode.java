package io.hhplus.tdd;

public enum ErrorCode {
    INSUFFICIENT_POINT(400, "충분하지 않은 포인트가 있습니다.");

    private final int status;
    private final String message;

    ErrorCode(int status, String message) {
        this.status = status;
        this.message = message;

    }

    public String getMessage() {
        return message;
    }

    public int getStatus(){
        return status;
    }
}
