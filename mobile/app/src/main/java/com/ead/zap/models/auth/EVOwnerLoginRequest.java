package com.ead.zap.models.auth;

/**
 * Login request model for EV Owners
 */
public class EVOwnerLoginRequest {
    private String nic;
    private String password;

    public EVOwnerLoginRequest() {}

    public EVOwnerLoginRequest(String nic, String password) {
        this.nic = nic;
        this.password = password;
    }

    public String getNic() {
        return nic;
    }

    public void setNic(String nic) {
        this.nic = nic;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}