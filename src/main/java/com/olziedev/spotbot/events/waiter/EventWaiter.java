package com.olziedev.spotbot.events.waiter;

import com.olziedev.spotbot.SpotBot;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.SubscribeEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class EventWaiter implements EventListener {

    private final HashMap<Class<?>, Set<OlzieWaitingEvent>> waitingEvents;
    private final ScheduledExecutorService threadpool;

    public EventWaiter() {
        this.waitingEvents = new HashMap<>();
        this.threadpool = Executors.newSingleThreadScheduledExecutor();

        SpotBot.getJDA().addEventListener(this);
    }

    public <E extends Event> CancelWaiter waitEvent(Class<E> classType, Predicate<E> condition, Consumer<E> action) {
        return this.waitEvent(classType, condition, action, -1, null, null);
    }

    public <E extends Event> CancelWaiter waitEvent(Class<E> classType, Predicate<E> condition, Consumer<E> action, long timeout, TimeUnit unit, Runnable runnable) {
        OlzieWaitingEvent<E> olzieWaitingEvent = new OlzieWaitingEvent<>(condition, action);
        Set<OlzieWaitingEvent> set = waitingEvents.computeIfAbsent(classType, c -> new HashSet<>());

        set.add(olzieWaitingEvent);
        if (timeout > 0 && unit != null) {
            threadpool.schedule(() -> {
                if (set.remove(olzieWaitingEvent) && runnable != null) {
                    runnable.run();
                }
            }, timeout, unit);
        }
        return new CancelWaiter(set, olzieWaitingEvent);
    }

    @Override
    @SubscribeEvent
    @SuppressWarnings({"unchecked"})
    public final void onEvent(GenericEvent event) {
        Class c = event.getClass();

        while (c != null) {
            if (waitingEvents.containsKey(c)) {
                Set<OlzieWaitingEvent> set = waitingEvents.get(c);
                OlzieWaitingEvent[] toRemove = set.toArray(new OlzieWaitingEvent[0]);
                set.removeAll(Stream.of(toRemove).filter(i -> i.attempt(event)).collect(Collectors.toSet()));
            }
            if (event instanceof ShutdownEvent) {
                threadpool.shutdown();
            }
            c = c.getSuperclass();
        }
    }

    public static class CancelWaiter {

        private final Set<OlzieWaitingEvent> set;
        private final OlzieWaitingEvent<?> olzieWaitingEvent;

        public CancelWaiter(Set<OlzieWaitingEvent> set, OlzieWaitingEvent<?> olzieWaitingEvent) {
            this.set = set;
            this.olzieWaitingEvent = olzieWaitingEvent;
        }

        public void cancel() {
            set.remove(olzieWaitingEvent);
        }
    }
}
