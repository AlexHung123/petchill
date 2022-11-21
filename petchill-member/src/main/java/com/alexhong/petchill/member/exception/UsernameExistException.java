package com.alexhong.petchill.member.exception;

/**
 * @author alexhung
 */
public class UsernameExistException extends RuntimeException{

    public UsernameExistException() {
        super("Username has already existed");
    }
}
