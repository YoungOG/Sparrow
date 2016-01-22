package com.breakmc.sparrow;

import com.breakmc.sparrow.commands.ServerCommand;
import com.breakmc.sparrow.listeners.PlayerListeners;
import com.breakmc.sparrow.server.Server;
import com.breakmc.sparrow.server.ServerManager;
import com.breakmc.sparrow.utils.command.Register;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.MongoClient;
import org.bukkit.entity.Player;
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
            register.registerCommand("serversign", new ServerCommand());
        } catch (Exception e) {
            e.printStackTrace();
        }

        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
    }

    public void onDisable() {
        serverManager.saveServers();
        serverManager.saveSpawn();
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
