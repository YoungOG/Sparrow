package com.breakmc.sparrow;

import com.breakmc.sparrow.commands.QueueCommand;
import com.breakmc.sparrow.listeners.PlayerListeners;
import com.breakmc.sparrow.queue.Server;
import com.breakmc.sparrow.queue.ServerManager;
import com.breakmc.sparrow.queue.ServerQueue;
import com.breakmc.sparrow.utils.PlayerUtility;
import com.breakmc.sparrow.utils.command.Register;
import com.empcraft.InSignsPlus;
import com.empcraft.Placeholder;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.MongoClient;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.logging.Level;

public class Sparrow extends JavaPlugin implements PluginMessageListener {

    private static Sparrow instance;
    private DB database;
    private ServerManager serverManager;

    public void onEnable() {
        instance = this;

        getConfig().options().copyDefaults(true);

        setupDatabase();

        serverManager = new ServerManager();

        try {
            Register register = new Register();
            register.registerCommand("queue", new QueueCommand());
        } catch (Exception e) {
            e.printStackTrace();
        }
        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        Plugin inSignsPlus = getServer().getPluginManager().getPlugin("InSignsPlus");

        if ((inSignsPlus != null) && inSignsPlus.isEnabled()) {
            InSignsPlus ISP = (InSignsPlus) inSignsPlus;

            ISP.addPlaceholder(new Placeholder("sn") {
                @Override
                public String getValue(Player player, Location location, String[] modifiers, Boolean elevation) {
                    if (location != null) {
                        for (Server s : serverManager.getServers()) {
                            if (s.getServerSignLocation().equals(location)) {
                                return s.getName();
                            }
                        }
                    }

                    return null;
                }
            });

            ISP.addPlaceholder(new Placeholder("aa") {
                @Override
                public String getValue(Player player, Location location, String[] modifiers, Boolean elevation) {
                    if (location != null) {
                        for (Server s : serverManager.getServers()) {
                            if (s.getServerSignLocation().equals(location)) {
                                return "" + s.getPlayerCount();
                            }
                        }
                    }

                    return null;
                }
            });

            ISP.addPlaceholder(new Placeholder("bb") {
                @Override
                public String getValue(Player player, Location location, String[] modifiers, Boolean elevation) {
                    if (location != null) {
                        for (Server s : serverManager.getServers()) {
                            if (s.getServerSignLocation().equals(location)) {
                                return "" + s.getMaxPlayerCount();
                            }
                        }
                    }

                    return null;
                }
            });

            ISP.addPlaceholder(new Placeholder("ln1") {
                @Override
                public String getValue(Player player, Location location, String[] modifiers, Boolean elevation) {
                    if (location != null) {
                        for (Server s : serverManager.getServers()) {
                            if (!s.getServerQueue().isInQueue(player.getUniqueId())) {
                                return ChatColor.GREEN + "Click to join";
                            } else {
                                return ChatColor.GREEN + "Your Position";
                            }
                        }
                    }

                    return null;
                }
            });

            ISP.addPlaceholder(new Placeholder("ln2") {
                @Override
                public String getValue(Player player, Location location, String[] modifiers, Boolean elevation) {
                    if (location != null) {
                        for (Server s : serverManager.getServers()) {
                            if (!s.getServerQueue().isInQueue(player.getUniqueId())) {
                                return ChatColor.GREEN + "the queue!";
                            } else {
                                ServerQueue queue = s.getServerQueue();

                                if (queue.getSupremeQueue().contains(player.getUniqueId())) {
                                    return ChatColor.translateAlternateColorCodes('&', "&e" + (PlayerUtility.findPosition(player.getUniqueId(), queue) + 1) + " of " + queue.getSupremeQueue().size());
                                } else if (queue.getDonatorQueue().contains(player.getUniqueId())) {
                                    return ChatColor.translateAlternateColorCodes('&', "&e" + (PlayerUtility.findPosition(player.getUniqueId(), queue) + 1) + " of " + queue.getDonatorQueue().size());
                                } else if (queue.getNormalQueue().contains(player.getUniqueId())) {
                                    return ChatColor.translateAlternateColorCodes('&', "&e" + (PlayerUtility.findPosition(player.getUniqueId(), queue) + 1) + " of " + queue.getNormalQueue().size());
                                }

                            }
                        }
                    }

                    return null;
                }
            });

            ISP.addPlaceholder(new Placeholder("s") {
                @Override
                public String getValue(Player player, Location location, String[] modifiers, Boolean elevation) {
                    if (location != null) {
                        for (Server s : serverManager.getServers()) {
                            if (s.getQueueSignLocation().equals(location)) {
                                return "" + s.getServerQueue().getSupremeQueue().size();
                            }
                        }
                    }

                    return null;
                }
            });

            ISP.addPlaceholder(new Placeholder("d") {
                @Override
                public String getValue(Player player, Location location, String[] modifiers, Boolean elevation) {
                    if (location != null) {
                        for (Server s : serverManager.getServers()) {
                            if (s.getQueueSignLocation().equals(location)) {
                                return "" + s.getServerQueue().getDonatorQueue().size();
                            }
                        }
                    }

                    return null;
                }
            });

            ISP.addPlaceholder(new Placeholder("n") {
                @Override
                public String getValue(Player player, Location location, String[] modifiers, Boolean elevation) {
                    if (location != null) {
                        for (Server s : serverManager.getServers()) {
                            if (s.getQueueSignLocation().equals(location)) {
                                return "" + s.getServerQueue().getNormalQueue().size();
                            }
                        }
                    }

                    return null;
                }
            });
        }
    }

    public void onDisable() {
        serverManager.saveServers();
    }

    public void setupDatabase() {
        try {
            database = MongoClient.connect(new DBAddress(getConfig().getString("database.host"), getConfig().getString("database.database-name")));
            this.getLogger().log(Level.INFO, "Sucessfully connected to MongoDB.");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            this.getLogger().log(Level.INFO, "Failed to connect to MongoDB.");
        }
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
            String subchannel = in.readUTF();

            if (subchannel.equals("PlayerCount")) {
                String server = in.readUTF();
                int playerCount = in.readInt();

                for (Server s : serverManager.getServers()) {
                    if (s.getName().equalsIgnoreCase(server)) {
                        s.setPlayerCount(playerCount);
                    }
                }
            }
        } catch (IOException e) {
        }
    }

    public void getCount(Player player, String server) {
        if (server == null) {
            server = "ALL";
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerCount");
        out.writeUTF(server);

        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    public static Sparrow getInstance() {
        return instance;
    }

    public DB getDB() {
        return database;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }
}
