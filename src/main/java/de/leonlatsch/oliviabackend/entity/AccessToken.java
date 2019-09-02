package de.leonlatsch.oliviabackend.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "access_token")
public class AccessToken {

    @Id
    @Column(name = "token", length = 11)
    private String token;

    @Column(name = "uid")
    private int uid;

    @Column(name = "valid")
    private boolean valid;

    public AccessToken() {}

    public AccessToken(String token, int uid, boolean valid) {
        this.token = token;
        this.uid = uid;
        this.valid = valid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
