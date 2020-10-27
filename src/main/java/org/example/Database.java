package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Database {
    private final List<User> users;
    private final Map<Integer, User> mappedUsers;

    public Database() {
        this.users = new ArrayList<>();
        this.mappedUsers = new HashMap<>();
        users.add(new User("tomek", "tomek1"));
        users.add(new User("pawel", "pawel1"));
        users.add(new User("marcin", "marcin1"));
    }

    public Map<Integer, User> getSessionUserMap() {
        return mappedUsers;
    }

    public User getUserBySessionId(int sessionId) {
        return mappedUsers.get(sessionId);
    }

    public User getUserByProvidedName(String providedName) {
        return users.stream().filter(u -> u.getName().equals(providedName)).findFirst().orElse(null);
    }
}
