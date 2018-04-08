package org.twowls.backgallery.utils;

/**
 * <p>TODO add documentation...</p>
 *
 * @author Dmitry Chubarov
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, X extends Exception> {

    R apply(T operand) throws X;
}
