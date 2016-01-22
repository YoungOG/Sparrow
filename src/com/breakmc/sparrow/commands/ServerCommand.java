package com.breakmc.sparrow.commands;

import com.breakmc.sparrow.Sparrow;
import com.breakmc.sparrow.server.Server;
import com.breakmc.sparrow.server.ServerManager;
import com.breakmc.sparrow.utils.MessageManager;
import com.breakmc.sparrow.utils.command.BaseCommand;
import com.breakmc.sparrow.utils.command.CommandUsageBy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Created by Calvin on 4/22/2015.
 * Project: Legacy
 */
public class ServerCommand extends BaseCommand {

    private Sparrow main = Sparrow.getInstance();
    private ServerManager serverManager = main.getServerManager();

    public ServerCommand() {
        super("serversign", "sparrow.admin", CommandUsageBy.ANYONE);
        setUsage("&cImproper usage! /serversign");
        setMinArgs(0);
        setMaxArgs(3);
    }

    @Override
    public void execute(final CommandSender sender, String[] args) {
        if (args.length == 0) {
            MessageManager.sendMessage(sender, "&7&m----------&r&7[ &bServer Command &7]&m----------");
            MessageManager.sendMessage(sender, "&r &r&3/serversign &7- Displays this page.");
            MessageManager.sendMessage(sender, "&r &r&3/serversign setspawn &7- Sets the server spawn.");
            MessageManager.sendMessage(sender, "&r &r&3/serversign maxslots &7(&bserver&7) (&bcount&7) - Set a servers max slots.");
            MessageManager.sendMessage(sender, "&r &r&3/serversign whitelist &7(&bserver&7) - Toggle servers whitelist.");
            MessageManager.sendMessage(sender, "&r &r&3/serversign pause &7(&bserver&7) - Pause the queue of a server.");
            MessageManager.sendMessage(sender, "&7&m------------------------------------");
        } else if (args.length == 1) {
            if (args[0].equalsIgnoreCase("setspawn")) {
                if (sender instanceof Player) {
                    Player p = (Player) sender;

                    serverManager.setSpawnLocation(p.getLocation());

                    MessageManager.sendMessage(p, "&aSpawn location set at X: &e" + p.getLocation().getBlockX() + " &bY: &e" + p.getLocation().getBlockY() + " &bZ: &e" + p.getLocation().getBlockZ());
                } else {
                    MessageManager.sendMessage(sender, "&cOnly players can set the spawn.");
                }
            } else {
                MessageManager.sendMessage(sender, getUsage());
            }
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("whitelist")) {
                Server server = serverManager.getServer(args[1]);

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                if (!server.isWhitelisted()) {
                    server.setWhitelisted(true);
                    MessageManager.sendMessage(sender, "&aYou have turned on whitelisting for &b" + server.getName());
                } else {
                    server.setWhitelisted(false);
                    MessageManager.sendMessage(sender, "&aYou have turned off whitelisting for &b" + server.getName());
                }
            } else if (args[0].equalsIgnoreCase("pause")) {
                Server server = serverManager.getServer(args[1]);

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                if (!server.getServerQueue().isPaused()) {
                    server.getServerQueue().setPaused(true);
                    MessageManager.sendMessage(sender, "&aYou have paused the server for &b" + server.getName());
                } else {
                    server.getServerQueue().setPaused(false);
                    MessageManager.sendMessage(sender, "&aYou have resumed the server for &b" + server.getName());

                    for (UUID id : server.getServerQueue().getFullQueue()) {
                        MessageManager.sendMessage(id, "&2&m-----------------------------------------------------");
                        MessageManager.sendMessage(id, "&r");
                        MessageManager.sendMessage(id, "&r &r&aThe server has been unpaused.");
                        MessageManager.sendMessage(id, "&r &r&aQueuing will now resume!");
                        MessageManager.sendMessage(id, "&r");
                        MessageManager.sendMessage(id, "&2&m-----------------------------------------------------");
                    }
                }
            } else {
                MessageManager.sendMessage(sender, getUsage());
            }
        } else if (args.length == 3) {
            if (args[0].equalsIgnoreCase("maxslots")) {
                Server server = serverManager.getServer(args[1]);

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                try {
                    int maxSlots = Integer.parseInt(args[2]);

                    server.setMaxPlayerCount(maxSlots);
                    MessageManager.sendMessage(sender, "&aServer &b" + server.getName() + " &amax player count has been set to &e" + server.getMaxPlayerCount() + "&a.");
                } catch (NumberFormatException e) {
                    MessageManager.sendMessage(sender, "&cYou must enter a valid number.");
                }
            } else {
                MessageManager.sendMessage(sender, getUsage());
            }
        } else {
            MessageManager.sendMessage(sender, getUsage());
        }
    }
}
