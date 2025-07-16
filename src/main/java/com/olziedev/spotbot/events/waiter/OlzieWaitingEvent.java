package com.olziedev.spotbot.events.waiter;

import net.dv8tion.jda.api.events.GenericEvent;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class OlzieWaitingEvent<E extends GenericEvent> {

    final Predicate<E> condition;
    final Consumer<E> action;

    OlzieWaitingEvent(Predicate<E> condition, Consumer<E> action) {
        this.condition = condition;
        this.action = action;
    }

    boolean attempt(E event) {
        if (condition.test(event)) {
            if (action != null) action.accept(event);
            return true;
        }
        return false;
    }
}
