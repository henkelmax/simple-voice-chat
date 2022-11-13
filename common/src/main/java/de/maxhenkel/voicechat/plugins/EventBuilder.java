package de.maxhenkel.voicechat.plugins;

import de.maxhenkel.voicechat.api.events.Event;
import net.minecraft.util.Tuple;

import java.util.*;
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
            result.put(entry.getKey(), entry.getValue().stream().sorted((o1, o2) -> Integer.compare(o2.getFirst(), o1.getFirst())).map(Tuple::getSecond).collect(Collectors.toList()));
        }
        return result;
    }

    public static EventBuilder create() {
        return new EventBuilder();
    }

}
