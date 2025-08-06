package pl.eurokawa.exception;

import java.util.Objects;

public class ArgumentNullChecker{
    public static <T> T check(T object, String objectName){
        return Objects.requireNonNull(object,objectName + " cannot be null");
    }
    public static <T> T check(T object){
        return Objects.requireNonNull(object);
    }
}