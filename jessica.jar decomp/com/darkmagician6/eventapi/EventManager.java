/*
 * Decompiled with CFR 0.152.
 */
package com.darkmagician6.eventapi;

import com.darkmagician6.eventapi.EventTarget;
import com.darkmagician6.eventapi.events.Event;
import com.darkmagician6.eventapi.events.EventStoppable;
import com.darkmagician6.eventapi.types.Priority;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public final class EventManager {
    private static final Map<Class<? extends Event>, List<MethodData>> REGISTRY_MAP = new HashMap<Class<? extends Event>, List<MethodData>>();

    private EventManager() {
    }

    public static void register(Object object) {
        Method[] methodArray = object.getClass().getDeclaredMethods();
        int n = methodArray.length;
        int n2 = 0;
        while (n2 < n) {
            Method method = methodArray[n2];
            if (!EventManager.isMethodBad(method)) {
                EventManager.register(method, object);
            }
            ++n2;
        }
    }

    public static void register(Object object, Class<? extends Event> eventClass) {
        Method[] methodArray = object.getClass().getDeclaredMethods();
        int n = methodArray.length;
        int n2 = 0;
        while (n2 < n) {
            Method method = methodArray[n2];
            if (!EventManager.isMethodBad(method, eventClass)) {
                EventManager.register(method, object);
            }
            ++n2;
        }
    }

    public static void unregister(Object object) {
        for (List<MethodData> dataList : REGISTRY_MAP.values()) {
            for (MethodData data : dataList) {
                if (!data.getSource().equals(object)) continue;
                dataList.remove(data);
            }
        }
        EventManager.cleanMap(true);
    }

    public static void unregister(Object object, Class<? extends Event> eventClass) {
        if (REGISTRY_MAP.containsKey(eventClass)) {
            for (MethodData data : REGISTRY_MAP.get(eventClass)) {
                if (!data.getSource().equals(object)) continue;
                REGISTRY_MAP.get(eventClass).remove(data);
            }
            EventManager.cleanMap(true);
        }
    }

    private static void register(Method method, Object object) {
        Class<?> indexClass = method.getParameterTypes()[0];
        MethodData data = new MethodData(object, method, method.getAnnotation(EventTarget.class).value());
        if (!data.getTarget().isAccessible()) {
            data.getTarget().setAccessible(true);
        }
        if (REGISTRY_MAP.containsKey(indexClass)) {
            if (!REGISTRY_MAP.get(indexClass).contains(data)) {
                REGISTRY_MAP.get(indexClass).add(data);
                EventManager.sortListValue(indexClass);
            }
        } else {
            REGISTRY_MAP.put(indexClass, (List<MethodData>)new CopyOnWriteArrayList<MethodData>(data){
                private static final long serialVersionUID = 666L;
                {
                    this.add(methodData);
                }
            });
        }
    }

    public static void removeEntry(Class<? extends Event> indexClass) {
        Iterator<Map.Entry<Class<? extends Event>, List<MethodData>>> mapIterator = REGISTRY_MAP.entrySet().iterator();
        while (mapIterator.hasNext()) {
            if (!mapIterator.next().getKey().equals(indexClass)) continue;
            mapIterator.remove();
            break;
        }
    }

    public static void cleanMap(boolean onlyEmptyEntries) {
        Iterator<Map.Entry<Class<? extends Event>, List<MethodData>>> mapIterator = REGISTRY_MAP.entrySet().iterator();
        while (mapIterator.hasNext()) {
            if (onlyEmptyEntries && !mapIterator.next().getValue().isEmpty()) continue;
            mapIterator.remove();
        }
    }

    private static void sortListValue(Class<? extends Event> indexClass) {
        CopyOnWriteArrayList<MethodData> sortedList = new CopyOnWriteArrayList<MethodData>();
        byte[] byArray = Priority.VALUE_ARRAY;
        int n = Priority.VALUE_ARRAY.length;
        int n2 = 0;
        while (n2 < n) {
            byte priority = byArray[n2];
            for (MethodData data : REGISTRY_MAP.get(indexClass)) {
                if (data.getPriority() != priority) continue;
                sortedList.add(data);
            }
            ++n2;
        }
        REGISTRY_MAP.put(indexClass, sortedList);
    }

    private static boolean isMethodBad(Method method) {
        return method.getParameterTypes().length != 1 || !method.isAnnotationPresent(EventTarget.class);
    }

    private static boolean isMethodBad(Method method, Class<? extends Event> eventClass) {
        return EventManager.isMethodBad(method) || !method.getParameterTypes()[0].equals(eventClass);
    }

    public static final Event call(Event event) {
        block5: {
            List<MethodData> dataList = REGISTRY_MAP.get(event.getClass());
            if (dataList == null) break block5;
            if (event instanceof EventStoppable) {
                EventStoppable stoppable = (EventStoppable)event;
                for (MethodData data : dataList) {
                    EventManager.invoke(data, event);
                    if (!stoppable.isStopped()) {
                        continue;
                    }
                    break;
                }
            } else {
                for (MethodData data : dataList) {
                    EventManager.invoke(data, event);
                }
            }
        }
        return event;
    }

    private static void invoke(MethodData data, Event argument) {
        try {
            data.getTarget().invoke(data.getSource(), argument);
        }
        catch (IllegalAccessException illegalAccessException) {
        }
        catch (IllegalArgumentException illegalArgumentException) {
        }
        catch (InvocationTargetException invocationTargetException) {
            // empty catch block
        }
    }

    private static final class MethodData {
        private final Object source;
        private final Method target;
        private final byte priority;

        public MethodData(Object source, Method target, byte priority) {
            this.source = source;
            this.target = target;
            this.priority = priority;
        }

        public Object getSource() {
            return this.source;
        }

        public Method getTarget() {
            return this.target;
        }

        public byte getPriority() {
            return this.priority;
        }
    }
}

