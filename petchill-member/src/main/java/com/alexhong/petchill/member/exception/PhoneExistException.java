package com.alexhong.petchill.member.exception;

/**
 * @author alexhung
 */
public class PhoneExistException extends RuntimeException {

    public PhoneExistException() {
        super("Phone number has already existed");
    }
}
