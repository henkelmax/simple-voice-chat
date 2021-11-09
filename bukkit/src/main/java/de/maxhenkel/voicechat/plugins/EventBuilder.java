package de.maxhenkel.voicechat.plugins;

import de.maxhenkel.voicechat.api.events.Event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class EventBuilder {

    private final Map<Class<? extends Event>, List<Tuple<Integer, Consumer<? extends Event>>>> events;

    private EventBuilder() {
        events = new HashMap<>();
    }

    public <T extends Event> EventBuilder addEvent(Class<T> eventClass, Consumer<T> event, int priority) {
        List<Tuple<Integer, Consumer<? extends Event>>> eventList = this.events.getOrDefault(eventClass, new ArrayList<>());
        eventList.add(new Tuple<>(priority, event));
        this.events.put(eventClass, eventList);
        return this;
    }

    public Map<Class<? extends Event>, List<Consumer<? extends Event>>> build() {
        Map<Class<? extends Event>, List<Consumer<? extends Event>>> result = new HashMap<>();
        for (Map.Entry<Class<? extends Event>, List<Tuple<Integer, Consumer<? extends Event>>>> entry : events.entrySet()) {
            result.put(entry.getKey(), entry.getValue().stream().sorted((o1, o2) -> Integer.compare(o2.getA(), o1.getA())).map(Tuple::getB).collect(Collectors.toList()));
        }
        return result;
    }

    public static EventBuilder create() {
        return new EventBuilder();
    }

    private static class Tuple<A, B> {
        private final A a;
        private final B b;

        public Tuple(A a, B b) {
            this.a = a;
            this.b = b;
        }

        public A getA() {
            return a;
        }

        public B getB() {
            return b;
        }
    }

}
