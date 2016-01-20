package com.breakmc.sparrow.commands;

import com.breakmc.sparrow.Sparrow;
import com.breakmc.sparrow.queue.Server;
import com.breakmc.sparrow.queue.ServerManager;
import com.breakmc.sparrow.utils.MessageManager;
import com.breakmc.sparrow.utils.command.BaseCommand;
import com.breakmc.sparrow.utils.command.CommandUsageBy;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Calvin on 4/22/2015.
 * Project: Legacy
 */
public class QueueCommand extends BaseCommand {

    private Sparrow main = Sparrow.getInstance();
    private ServerManager serverManager = main.getServerManager();

    public QueueCommand() {
        super("queue", "sparrow.admin", CommandUsageBy.ANYONE);
        setUsage("&cImproper usage! /queue pause (name)");
        setMinArgs(2);
        setMaxArgs(2);
    }

    @Override
    public void execute(final CommandSender sender, String[] args) {
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("pause")) {
                Server server = null;

                for (Server s : serverManager.getServers()) {
                    if (s.getName().equalsIgnoreCase(args[1])) {
                        server = s;
                    }
                }

                if (server == null) {
                    MessageManager.sendMessage(sender, "&cCould not find that server.");
                    return;
                }

                if (server.getServerQueue().isPaused()) {
                    server.getServerQueue().setPaused(true);
                    MessageManager.sendMessage(sender, "&aYou have paused the queue for the server " + server.getName());
                } else {
                    server.getServerQueue().setPaused(false);
                    MessageManager.sendMessage(sender, "&aYou have un paused the queue for the server " + server.getName());
                }
            }
        }
    }
}
