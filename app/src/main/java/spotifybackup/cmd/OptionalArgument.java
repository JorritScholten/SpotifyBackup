package spotifybackup.cmd;

import java.util.function.Consumer;

public interface OptionalArgument<T> {
    boolean isPresent();

    T getValue();

    /**
     * If argument is present, performs the given action with the value, otherwise does nothing.
     * @param action the action to be performed, if argument is present
     */
    default void ifPresent(Consumer<T> action) {
        if (isPresent()) action.accept(getValue());
    }

    /**
     * If argument is not present, performs the given action, otherwise does nothing.
     * @param action the action to be performed, if argument is not present
     */
    default void ifNotPresent(Runnable action) {
        if (!isPresent()) action.run();
    }

    /**
     * If an argument is present, performs the given action with the value, otherwise performs the given empty-based
     * action.
     * @param action      the action to be performed, if a value is present
     * @param emptyAction the empty-based action to be performed, if no value is present
     */
    default void ifPresentOrElse(Consumer<T> action, Runnable emptyAction) {
        if (isPresent()) action.accept(getValue());
        else emptyAction.run();
    }
}
