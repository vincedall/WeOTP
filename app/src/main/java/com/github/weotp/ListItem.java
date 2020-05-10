package com.github.weotp;

public class ListItem {
    private int seconds;
    private String appName;
    private String otp;

    public ListItem(int seconds, String appName){
        this.seconds = seconds;
        this.appName = appName;
    }

    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
