package com.example.DrawStrategy;

import com.example.entity.Prize;

public class DrawResult {

    private boolean success;
    private String message;
    private Prize prize;
    private String errorCode;
    private String type;

    private DrawResult() {}

    public static DrawResult success(Prize prize) {
        DrawResult result = new DrawResult();
        result.success = true;
        result.prize = prize;
        result.message = "抽奖成功";
        result.type = prize.getPrizeType();
        return result;
    }

    public static DrawResult noStock(String message) {
        DrawResult result = new DrawResult();
        result.success = false;
        result.errorCode = "NO_STOCK";
        result.message = message != null ? message : "活动奖品已售罄";
        return result;
    }

    public static DrawResult forbidden(String message) {
        DrawResult result = new DrawResult();
        result.success = false;
        result.errorCode = "FORBIDDEN";
        result.message = message != null ? message : "已达抽奖上限或不符合抽奖资格";
        return result;
    }

    public static DrawResult error(String message, String errorCode) {
        DrawResult result = new DrawResult();
        result.success = false;
        result.errorCode = errorCode != null ? errorCode : "ERROR";
        result.message = message != null ? message : "抽奖失败";
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Prize getPrize() {
        return prize;
    }

    public void setPrize(Prize prize) {
        this.prize = prize;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "DrawResult{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", prize=" + (prize != null ? prize.getPrizeName() : "null") +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}
