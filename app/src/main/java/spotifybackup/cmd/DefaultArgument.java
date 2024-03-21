package spotifybackup.cmd;

import java.util.function.Consumer;

public interface DefaultArgument<T> {
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
     * If argument is not present, performs the given action with the default value, otherwise does nothing.
     * @param defaultAction the action to be performed using the default value, if argument is not present
     */
    default void ifNotPresent(Consumer<T> defaultAction) {
        if (!isPresent()) defaultAction.accept(getValue());
    }

    /**
     * If an argument is present, performs the given action with the value, otherwise performs the given empty-based
     * action.
     * @param presentAction the action to be performed, if argument is present
     * @param defaultAction the action to be performed using the default value, if argument is not present
     */
    default void ifPresentOrElse(Consumer<T> presentAction, Consumer<T> defaultAction) {
        if (isPresent()) presentAction.accept(getValue());
        else defaultAction.accept(getValue());
    }
}
