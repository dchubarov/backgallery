package org.twowls.backgallery.utils;

/**
 * Represents a one-argument function that allowed to throw an exception.
 *
 * @param <T> the type of the function operand.
 * @param <R> the type of the return value.
 * @param <X> the type of exception.
 *
 * @author Dmitry Chubarov
 */
@FunctionalInterface
public interface ThrowingFunction<T, R, X extends Throwable> {

    /**
     * Applies this function to a given {@code operand}.
     *
     * @param operand the operand.
     * @return the function result.
     * @throws X the type of exception.
     */
    R apply(T operand) throws X;
}
