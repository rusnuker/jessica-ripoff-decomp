/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 */
package net.minecraft.util.text.event;

import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.util.text.ITextComponent;

public class HoverEvent {
    private final Action action;
    private final ITextComponent value;

    public HoverEvent(Action actionIn, ITextComponent valueIn) {
        this.action = actionIn;
        this.value = valueIn;
    }

    public Action getAction() {
        return this.action;
    }

    public ITextComponent getValue() {
        return this.value;
    }

    public boolean equals(Object p_equals_1_) {
        if (this == p_equals_1_) {
            return true;
        }
        if (p_equals_1_ != null && this.getClass() == p_equals_1_.getClass()) {
            HoverEvent hoverevent = (HoverEvent)p_equals_1_;
            if (this.action != hoverevent.action) {
                return false;
            }
            return !(this.value != null ? !this.value.equals(hoverevent.value) : hoverevent.value != null);
        }
        return false;
    }

    public String toString() {
        return "HoverEvent{action=" + (Object)((Object)this.action) + ", value='" + this.value + '\'' + '}';
    }

    public int hashCode() {
        int i = this.action.hashCode();
        i = 31 * i + (this.value != null ? this.value.hashCode() : 0);
        return i;
    }

    public static enum Action {
        SHOW_TEXT("show_text", true),
        SHOW_ITEM("show_item", true),
        SHOW_ENTITY("show_entity", true);

        private static final Map<String, Action> NAME_MAPPING;
        private final boolean allowedInChat;
        private final String canonicalName;

        static {
            NAME_MAPPING = Maps.newHashMap();
            Action[] actionArray = Action.values();
            int n = actionArray.length;
            int n2 = 0;
            while (n2 < n) {
                Action hoverevent$action = actionArray[n2];
                NAME_MAPPING.put(hoverevent$action.getCanonicalName(), hoverevent$action);
                ++n2;
            }
        }

        private Action(String canonicalNameIn, boolean allowedInChatIn) {
            this.canonicalName = canonicalNameIn;
            this.allowedInChat = allowedInChatIn;
        }

        public boolean shouldAllowInChat() {
            return this.allowedInChat;
        }

        public String getCanonicalName() {
            return this.canonicalName;
        }

        public static Action getValueByCanonicalName(String canonicalNameIn) {
            return NAME_MAPPING.get(canonicalNameIn);
        }
    }
}

