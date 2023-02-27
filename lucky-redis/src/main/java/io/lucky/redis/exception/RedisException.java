package io.lucky.redis.exception;

public class RedisException extends RuntimeException{

    public RedisException(String msg) {
        super(msg);
    }
}
