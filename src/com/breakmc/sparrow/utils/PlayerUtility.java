package com.breakmc.sparrow.utils;

import com.breakmc.sparrow.Sparrow;
import com.breakmc.sparrow.queue.ServerQueue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import ru.tehkode.permissions.PermissionGroup;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.bukkit.PermissionsEx;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.UUID;

public class PlayerUtility {

    public static double getHealth(Player p) {
        return p.getHealth();
    }

    public static double getMaxHealth(Player p) { return p.getMaxHealth(); }

    public static void setHealth(Player p, double health) { p.setHealth(health); }

    public static double getDamage(EntityDamageByEntityEvent e) { return e.getDamage(); }

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

    public static boolean hasInventorySpace(Inventory inventory, ItemStack is) {
        Inventory inv = Bukkit.createInventory(null, inventory.getSize());

        for (int i = 0; i < inv.getSize(); i++) {
            if (inventory.getItem(i) != null) {
                ItemStack item = inventory.getItem(i).clone();
                inv.setItem(i, item);
            }
        }

        return inv.addItem(new ItemStack[]{is.clone()}).size() <= 0;
    }

    public static int checkSlotsAvailable(Inventory inv) {
        ItemStack[] items = inv.getContents();
        int emptySlots = 0;

        for (ItemStack is : items) {
            if (is == null) {
                emptySlots = emptySlots + 1;
            }
        }

        return emptySlots;
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
}
