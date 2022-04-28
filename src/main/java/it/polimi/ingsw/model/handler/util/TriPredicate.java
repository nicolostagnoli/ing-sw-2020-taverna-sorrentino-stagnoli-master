package it.polimi.ingsw.model.handler.util;

import java.util.Objects;

/**
 * Represents a predicate (boolean-valued function) of three arguments.
 * This is the three-arity specialization of {@link java.util.function.Predicate}.
 * @param <T> the type of the first argument to the predicate
 * @param <U> the type of the second argument to the predicate
 * @param <R> the type of the third argument to the predicate
 */
@FunctionalInterface
public interface TriPredicate<T, U, R> {

    /**
     * Evaluates this predicate on the given arguments.
     * @param t the first input argument
     * @param u the second input argument
     * @param r the third input argument
     * @return true if the input arguments match the predicate, otherwise false
     */
    boolean test(T t, U u, R r);

    default TriPredicate<T, U, R> negate() {
        return (t, u, r) -> !test(t, u, r);
    }

    default TriPredicate<T, U, R> and(TriPredicate<? super T, ? super U, ? super R> other) {
        Objects.requireNonNull(other);
        return (t, u, r) -> test(t, u, r) && other.test(t, u, r);
    }

    default TriPredicate<T, U, R> or(TriPredicate<? super T, ? super U, ? super R> other) {
        Objects.requireNonNull(other);
        return (t, u, r) -> test(t, u, r) || other.test(t, u, r);
    }
}
