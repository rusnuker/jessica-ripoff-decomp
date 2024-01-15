/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix;

import com.mysql.fabric.Wrapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FriendManager {
    public static List<String> friends = new ArrayList<String>();

    public static boolean isFriend(String nickname) {
        return friends.contains(nickname);
    }

    public static void addFriend(String nickname) {
        if (FriendManager.isFriend(nickname)) {
            Wrapper.msg("&c\u0414\u0430\u043d\u043d\u044b\u0439 \u0438\u0433\u0440\u043e\u043a \u0443\u0436\u0435 \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 \u0441\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439.", true);
            return;
        }
        friends.add(nickname);
        Wrapper.msg("&a\u0418\u0433\u0440\u043e\u043a \u0441 \u043d\u0438\u043a\u043e\u043c \"" + nickname + "\" \u0434\u043e\u0431\u0430\u0432\u043b\u0435\u043d \u0432 \u0441\u043f\u0438\u0441\u043e\u043a \u0434\u0440\u0443\u0437\u0435\u0439.", true);
    }

    public static void delFriend(String nickname) {
        if (!FriendManager.isFriend(nickname)) {
            Wrapper.msg("&c\u0414\u0430\u043d\u043d\u044b\u0439 \u0438\u0433\u0440\u043e\u043a \u043d\u0435 \u044f\u0432\u043b\u044f\u0435\u0442\u0441\u044f \u0432\u0430\u0448\u0438\u043c \u0434\u0440\u0443\u0433\u043e\u043c!", true);
            return;
        }
        friends.remove(nickname);
        Wrapper.msg("&c\u0418\u0433\u0440\u043e\u043a \u0441 \u043d\u0438\u043a\u043e\u043c \"" + nickname + "\" \u0443\u0434\u0430\u043b\u0451\u043d \u0438\u0437 \u0441\u043f\u0438\u0441\u043a\u0430 \u0434\u0440\u0443\u0437\u0435\u0439.", true);
    }

    public static Collection<String> getFriends() {
        if (friends == null) {
            friends = new ArrayList<String>();
        }
        return friends;
    }

    public static void setFriends(Collection<String> friends) {
        FriendManager.friends = (List)friends;
    }
}

