package io.lucky.redis.domain;

import java.io.Serializable;

public class LockParam implements Serializable {

    /**
     * 锁名称
     */
    private String key;

    /**
     * 锁失效时间,单位:秒,默认:60秒
     */
    private Integer expirationTime = 60;

    /**
     * 是否可重入,默认:可重入
     */
    private Boolean isReentrant = true;

    /**
     * 是否自旋,默认:自旋
     */
    private Boolean isSpin = true;

    /**
     * 自旋次数,超出次数,放弃自旋避免死循环,默认:3次
     */
    private Integer spinNumber = 3;

    /**
     * 自旋等待时间,单位:秒,默认:5秒
     */
    private Integer spinAwaitTime = 5;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Integer getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(Integer expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Boolean getReentrant() {
        return isReentrant;
    }

    public void setReentrant(Boolean reentrant) {
        isReentrant = reentrant;
    }

    public Boolean getSpin() {
        return isSpin;
    }

    public void setSpin(Boolean spin) {
        isSpin = spin;
    }

    public Integer getSpinNumber() {
        return spinNumber;
    }

    public void setSpinNumber(Integer spinNumber) {
        this.spinNumber = spinNumber;
    }

    public Integer getSpinAwaitTime() {
        return spinAwaitTime;
    }

    public void setSpinAwaitTime(Integer spinAwaitTime) {
        this.spinAwaitTime = spinAwaitTime;
    }

    @Override
    public String toString() {
        return "LockParam{" +
                "key='" + key + '\'' +
                ", expirationTime=" + expirationTime +
                ", isReentrant=" + isReentrant +
                ", isSpin=" + isSpin +
                ", spinNumber=" + spinNumber +
                ", spinAwaitTime=" + spinAwaitTime +
                '}';
    }

    public static Builder builder(String key){
        return new Builder(key);
    }

    public static class Builder {

        private String key;

        private Integer expirationTime = 5;

        private Boolean isReentrant = true;

        private Boolean isSpin = true;

        private Integer spinNumber = 3;

        private Integer spinAwaitTime = 5;

        public Builder(String key) {
            this.key = key;
        }

        public Builder expirationTime(Integer expirationTime) {
            this.expirationTime = expirationTime;
            return this;
        }

        public Builder reentrant(Boolean reentrant) {
            isReentrant = reentrant;
            return this;
        }

        public Builder spin(Boolean spin) {
            isSpin = spin;
            return this;
        }

        public Builder spinNumber(Integer spinNumber) {
            this.spinNumber = spinNumber;
            return this;
        }

        public Builder spinAwaitTime(Integer spinAwaitTime) {
            this.spinAwaitTime = spinAwaitTime;
            return this;
        }

        public LockParam build() {
            LockParam lockParam = new LockParam();
            lockParam.setKey(key);
            lockParam.setExpirationTime(expirationTime);
            lockParam.setReentrant(isReentrant);
            lockParam.setSpin(isSpin);
            lockParam.setSpinNumber(spinNumber);
            lockParam.setSpinAwaitTime(spinAwaitTime);
            return lockParam;
        }
    }
}
