package com.breakmc.sparrow.queue;

import com.breakmc.sparrow.Sparrow;
import com.breakmc.sparrow.utils.LocationSerialization;
import com.breakmc.sparrow.utils.MessageManager;
import com.breakmc.sparrow.utils.PlayerUtility;
import com.empcraft.InSignsPlus;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class ServerManager {

    private Sparrow main = Sparrow.getInstance();
    private DBCollection sCollection = main.getDB().getCollection("servers");
    private ArrayList<Server> servers = new ArrayList<>();

    public ServerManager() {
        loadServers();

        new BukkitRunnable() {
            @Override
            public void run() {
                saveServers();
            }
        }.runTaskTimer(main, 0L, 300*20L);
    }

    public void loadServers() {
        System.out.println("Loading " + sCollection.count() + " servers");

        DBCursor dbc = sCollection.find();

        while (dbc.hasNext()) {
            BasicDBObject dbo = (BasicDBObject) dbc.next();

            Server server = new Server(dbo.getString("name"), dbo.getInt("maxPlayerCount"), LocationSerialization.deserializeLocation(dbo.getString("serverSignLocation")));
            server.setQueueSignLocation(LocationSerialization.deserializeLocation(dbo.getString("queueSignLocation")));
            servers.add(server);
            System.out.println("Server: " + server.getName());
            System.out.println("  Max Player Count: " + server.getMaxPlayerCount());
            System.out.println("  Server Sign Location: " + server.getServerSignLocation());
            System.out.println("  Queue Sign Location: " + server.getQueueSignLocation());
        }

        System.out.println("Loaded " + servers.size() + " servers");
    }

    public void saveServers() {
        System.out.println("Saving " + servers.size() + " servers");

        for (Server s : servers) {
            BasicDBObject bdo = new BasicDBObject("name", s.getName());

            if (sCollection.find(bdo).hasNext()) {
                BasicDBObject dbo = new BasicDBObject("name", s.getName());
                dbo.append("maxPlayerCount", s.getMaxPlayerCount());
                dbo.append("serverSignLocation", LocationSerialization.serializeLocation(s.getServerSignLocation()));
                dbo.append("queueSignLocation", LocationSerialization.serializeLocation(s.getQueueSignLocation()));

                sCollection.update(bdo, dbo);
            } else {
                BasicDBObject dbo = new BasicDBObject("name", s.getName());
                dbo.append("maxPlayerCount", s.getMaxPlayerCount());
                dbo.append("serverSignLocation", LocationSerialization.serializeLocation(s.getServerSignLocation()));
                dbo.append("queueSignLocation", LocationSerialization.serializeLocation(s.getQueueSignLocation()));

                sCollection.insert(dbo);
            }
        }

        System.out.println("Saved " + servers.size() + " servers");
    }

    public void createServer(Player p, String name, int maxPlayerCount, Sign sign) {
        Server server = new Server(name, maxPlayerCount, sign.getLocation());
        servers.add(server);

        MessageManager.sendMessage(p, "&aServer sign created!");
    }

    public void removeServer(Player p, Server s) {
        BasicDBObject dbo = new BasicDBObject("name", s.getName());

        DBCursor dbc = sCollection.find(dbo);

        while (dbc.hasNext()) {
            sCollection.remove(dbc.next());
        }

        servers.remove(s);
        s.setMaxPlayerCount(0);
        s.setServerSignLocation(null);
        s.setQueueSignLocation(null);

        MessageManager.sendMessage(p, "&cYou have removed server sign for " + s.getName());
    }

    public void addToServerQueue(Player p, Server server) {
        if (p.hasPermission("sparrow.staff")) {
            PlayerUtility.connect(p, server.getName());
            return;
        }

        if (PlayerUtility.getGroup(p.getName()).equalsIgnoreCase("supreme")) {
            if (!server.getServerQueue().getSupremeQueue().contains(p.getUniqueId())) {
                server.getServerQueue().getSupremeQueue().offer(p.getUniqueId());
                server.getServerQueue().getNormalQueue().remove(p.getUniqueId());
                server.getServerQueue().getDonatorQueue().remove(p.getUniqueId());

                InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getServerSignLocation());
                InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getQueueSignLocation());
            } else {
                MessageManager.sendMessage(p, "&cYou are already in the queue.");
            }
        } else if (PlayerUtility.getGroup(p.getName()).equalsIgnoreCase("Enhanced") || PlayerUtility.getGroup(p.getName()).equalsIgnoreCase("Member")) {
            if (!server.getServerQueue().getDonatorQueue().contains(p.getUniqueId())) {
                server.getServerQueue().getDonatorQueue().offer(p.getUniqueId());
                server.getServerQueue().getSupremeQueue().remove(p.getUniqueId());
                server.getServerQueue().getNormalQueue().remove(p.getUniqueId());

                InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getServerSignLocation());
                InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getQueueSignLocation());
            } else {
                MessageManager.sendMessage(p, "&cYou are already in the queue.");
            }
        } else {
            if (!server.getServerQueue().getNormalQueue().contains(p.getUniqueId())) {
                server.getServerQueue().getNormalQueue().offer(p.getUniqueId());
                server.getServerQueue().getSupremeQueue().remove(p.getUniqueId());
                server.getServerQueue().getDonatorQueue().remove(p.getUniqueId());

                InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getServerSignLocation());
                InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getQueueSignLocation());
            } else {
                MessageManager.sendMessage(p, "&cYou are already in the queue.");
            }
        }
    }

    public void removeFromServerQueue(Player p, Server server) {
        if (server.getServerQueue().getSupremeQueue().contains(p.getUniqueId())) {
            server.getServerQueue().getSupremeQueue().remove(p.getUniqueId());
            InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getServerSignLocation());
            InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getQueueSignLocation());
        }
        if (server.getServerQueue().getDonatorQueue().contains(p.getUniqueId())) {
            server.getServerQueue().getDonatorQueue().remove(p.getUniqueId());
            InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getServerSignLocation());
            InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getQueueSignLocation());
        }
        if (server.getServerQueue().getNormalQueue().contains(p.getUniqueId())) {
            server.getServerQueue().getNormalQueue().remove(p.getUniqueId());
            InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getServerSignLocation());
            InSignsPlus.getPlugin(InSignsPlus.class).updateSign(p, server.getQueueSignLocation());
        }
    }

    public Server getServerFromServerSignLocation(Location location) {
        for (Server servers : getServers()) {
            if (servers.getServerSignLocation().equals(location)) {
                return servers;
            }
        }

        return null;
    }

    public Server getServerFromQueueSignLocation(Location location) {
        for (Server servers : getServers()) {
            if (servers.getQueueSignLocation().equals(location)) {
                return servers;
            }
        }

        return null;
    }

    public ArrayList<Server> getServers() {
        return servers;
    }
}
