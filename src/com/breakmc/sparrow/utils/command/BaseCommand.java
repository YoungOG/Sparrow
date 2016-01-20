package com.breakmc.sparrow.utils.command;

import com.breakmc.sparrow.utils.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCommand implements TabExecutor {

    String name;
    String permission;
    CommandUsageBy commandUsage;
    String[] aliases;
    String usage;
    int maxArgs;
    int minArgs;

    public BaseCommand(String name, String permission, CommandUsageBy commandUsage, String... aliases) {
        this.name = name;
        this.commandUsage = commandUsage;
        this.permission = permission;
        this.aliases = aliases;
    }


    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] strings) {
        if (commandUsage.equals(CommandUsageBy.PlAYER) && !(sender instanceof Player)) {
            MessageManager.sendMessage(sender, "Only players can use this command.");
            return false;
        } else if (commandUsage.equals(CommandUsageBy.CONSOLE)) {
            MessageManager.sendMessage(sender, "&cOnly the console can use this command.");
            return false;
        } else {
            if (permission != null && !sender.hasPermission(this.permission)) {
                MessageManager.sendMessage(sender, "&cYou don't have access to this command.");
                return false;
            } else {
                if (!(maxArgs < 0)) {
                    if (strings.length > maxArgs) {
                        if (getUsage() != null) {
                            MessageManager.sendMessage(sender, getUsage().replace("<command>", s));
                        }
                        return true;
                    }
                }

                if (!(minArgs < 0)) {
                    if (strings.length < minArgs) {
                        if (getUsage() != null) {
                            MessageManager.sendMessage(sender, getUsage().replace("<command>", s));
                        }
                        return true;
                    }
                }

                execute(sender, strings);
            }
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] strings) {
        return tabComplete(strings, sender);
    }

    public abstract void execute(CommandSender sender, String[] args);

    public List<String> tabComplete(String[] args, CommandSender sender) {
        return new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public CommandUsageBy getCommandUsage() {
        return commandUsage;
    }

    public String getPermission() {
        return permission;
    }

    public String[] getAliases() {
        return aliases;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }

    public String getUsage() {
        return usage;
    }

    public void setMaxArgs(int maxArgs) {
        this.maxArgs = maxArgs;
    }

    public void setMinArgs(int minArgs) {
        this.minArgs = minArgs;
    }
}
