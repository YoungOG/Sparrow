package com.breakmc.sparrow.utils;

import com.breakmc.sparrow.Sparrow;
import com.breakmc.sparrow.server.ServerQueue;
import de.blablubbabc.insigns.InSigns;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class PlayerUtility {

    public static Player[] getOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }

    public static String getGroup(String name) {
        if (!Bukkit.getPluginManager().isPluginEnabled(PermissionsEx.getPlugin())) {
            return "";
        }

        PermissionUser user = PermissionsEx.getUser(name);
        if (user == null)
            return "";

        PermissionGroup[] groups = user.getGroups();

        if (groups.length == 0)
            return "";

        return groups[0].getName();
    }

    public static String getGroupColor(String name) {
        String group = getGroup(name);

        if (group.equalsIgnoreCase("Supreme")) {
            return "&b" + group;
        }

        if (group.equalsIgnoreCase("Enhanced")) {
            return "&e" + group;
        }

        if (group.equalsIgnoreCase("Member")) {
            return "&a" + group;
        }

        if (group.equalsIgnoreCase("Youtube+")) {
            return "&d" + group.replace("+", "&c+").replace("t", "T");
        }

        if (group.equalsIgnoreCase("Youtube")) {
            return "&d" + group.replace("t", "T");
        }

        return "&7" + group;
    }

    public static void connect(Player p, String channel) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(b);

        try {
            out.writeUTF("Connect");
            out.writeUTF(channel);
        } catch (IOException e) {
            e.printStackTrace();
        }

        p.sendPluginMessage(Sparrow.getInstance(), "BungeeCord", b.toByteArray());
    }

    public static int findPosition(UUID id, ServerQueue queue) {
        int pos = 0;

        Queue<UUID> totalQueue = new LinkedList<>();

        for (UUID uuid : queue.getSupremeQueue()) {
            totalQueue.add(uuid);
        }

        for (UUID uuid : queue.getDonatorQueue()) {
            totalQueue.add(uuid);
        }

        for (UUID uuid : queue.getNormalQueue()) {
            totalQueue.add(uuid);
        }

        for (UUID i : totalQueue) {
            if (i != id) {
                pos++;
            } else {
                return pos;
            }
        }

        return pos;
    }

    public static void updateSign(Player p, Location loc) {
        Sign s = (Sign) loc.getWorld().getBlockAt(loc).getState();

        if (s != null) {
            if (p.getLocation().distance(loc) < 20) {
                InSigns.sendSignChange(p, s);
            }
        }
    }
}
