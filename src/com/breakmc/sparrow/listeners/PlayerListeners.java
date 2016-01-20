package com.breakmc.sparrow.listeners;

import com.breakmc.sparrow.Sparrow;
import com.breakmc.sparrow.queue.Server;
import com.breakmc.sparrow.queue.ServerManager;
import com.breakmc.sparrow.utils.Cooldowns;
import com.breakmc.sparrow.utils.MessageManager;
import com.empcraft.InSignsPlus;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;

public class PlayerListeners implements Listener {

    private Sparrow main = Sparrow.getInstance();
    private ServerManager serverManager = main.getServerManager();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        e.setJoinMessage(null);

        p.teleport(new Location(p.getWorld(), -0.5, 69, 0.5));
        p.getInventory().clear();
        p.updateInventory();

        MessageManager.sendMessage(p, "&8&m-----------------------------------------------------");
        MessageManager.sendMessage(p, "&r");
        MessageManager.sendMessage(p, "&r &r&3Welcome to the &bBreakMC Network! &3Engage in the community below!");
        MessageManager.sendMessage(p, "&r");
        MessageManager.sendMessage(p, "&r &r&a\u25B6 &3Website&7: &bwww.breakmc.com");
        MessageManager.sendMessage(p, "&r &r&a\u25B6 &3TeamSpeak&7: &bts.breakmc.com");
        MessageManager.sendMessage(p, "&r &r&a\u25B6 &3Store&7: &bwww.breakmc.com/store");
        MessageManager.sendMessage(p, "&r");
        MessageManager.sendMessage(p, "&8&m-----------------------------------------------------");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        e.setQuitMessage(null);
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (!e.getPlayer().hasPermission("sparrow.build")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (!e.getPlayer().hasPermission("sparrow.build")) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncPlayerChatEvent e) {
        if (!e.getPlayer().hasPermission("sparrow.chat")) {
            e.setCancelled(true);
            MessageManager.sendMessage(e.getPlayer(), "&8&m-----------------------------------------------------");
            MessageManager.sendMessage(e.getPlayer(), "&r");
            MessageManager.sendMessage(e.getPlayer(), "&r &r&7Sorry! Only &aMembers &7and above may chat in the lobby!");
            MessageManager.sendMessage(e.getPlayer(), "&r &r&bPurchase a rank &7on our website at &bwww.breakmc.com/store");
            MessageManager.sendMessage(e.getPlayer(), "&r");
            MessageManager.sendMessage(e.getPlayer(), "&8&m-----------------------------------------------------");
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!e.getWhoClicked().hasPermission("sparrow.build")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo().getBlockY() <= 0) {
            e.getPlayer().teleport(new Location(e.getPlayer().getWorld(), -0.5, 69, 0.5));
        }
    }

    @EventHandler
    public void onFoodLoss(FoodLevelChangeEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onItemCreat(ItemSpawnEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onPortal(PlayerPortalEvent e) {
        e.setCancelled(true);
    }

    @EventHandler
    public void onSignPlace(SignChangeEvent e) {
        Player p = e.getPlayer();

        if (p.hasPermission("sparrow.build")) {
            if (e.getLine(0).equalsIgnoreCase("[server]")) {
                try {
                    String name = e.getLine(1);
                    int maxPlayerCount = Integer.parseInt(e.getLine(2));

                    serverManager.createServer(p, name, maxPlayerCount, (Sign) e.getBlock().getState());

                    e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&3{sn}"));
                    e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&b{aa}&7/&b{bb}"));
                    e.setLine(2, ChatColor.translateAlternateColorCodes('&', "{ln1}"));
                    e.setLine(3, ChatColor.translateAlternateColorCodes('&', "{ln2}"));
                    e.getBlock().getState().update(true);
                } catch (NumberFormatException ex) {
                    MessageManager.sendMessage(p, "&cCould not create Server sign.");
                    e.getBlock().breakNaturally();
                }
            }

            if (e.getLine(0).equalsIgnoreCase("[queue]")) {
                String name = e.getLine(1);

                Server server = null;

                for (Server s : serverManager.getServers()) {
                    if (s.getName().equalsIgnoreCase(name)) {
                        server = s;
                    }
                }

                if (server == null) {
                    MessageManager.sendMessage(p, "&cCould not create Queue sign.");
                    e.getBlock().breakNaturally();
                    return;
                }

                server.setQueueSignLocation(e.getBlock().getState().getLocation());

                e.setLine(0, ChatColor.translateAlternateColorCodes('&', "&3Current Queue"));
                e.setLine(1, ChatColor.translateAlternateColorCodes('&', "&bSupreme &a{s}"));
                e.setLine(2, ChatColor.translateAlternateColorCodes('&', "&bDonator &a{d}"));
                e.setLine(3, ChatColor.translateAlternateColorCodes('&', "&7Normal &a{n}"));
                e.getBlock().getState().update(true);
                MessageManager.sendMessage(p, "&aSuccessfully created Queue sign for server: &a" + server.getName());
            }
        }
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e) {
        Player p = e.getPlayer();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (e.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) e.getClickedBlock().getState();

                if (serverManager.getServerFromServerSignLocation(sign.getLocation()) != null) {
                    Server server = serverManager.getServerFromServerSignLocation(sign.getLocation());
                    e.setCancelled(true);

                    if (Cooldowns.tryCooldown(p.getUniqueId(), "sign-cooldown", 3000)) {
                        if (!server.getServerQueue().isInQueue(p.getUniqueId())) {
                            serverManager.addToServerQueue(p, server);
                        } else {
                            serverManager.removeFromServerQueue(p, server);
                        }
                    } else {
                        MessageManager.sendMessage(p, "&cDon't spam the queue.");
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSignBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        Block b = e.getBlock();

        if (b.getState() instanceof Sign) {
            if (!p.hasPermission("sparrow.build")) {
                e.setCancelled(true);
                return;
            }

            for (Server s : serverManager.getServers()) {
                if (s.getServerSignLocation().equals(b.getLocation())) {
                    serverManager.removeServer(p, s);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent e) {
        Player p = e.getPlayer();

        if (p.getGameMode() != GameMode.CREATIVE) {
            e.setCancelled(true);
            p.setAllowFlight(false);
            p.setFlying(false);
            p.setVelocity(p.getLocation().getDirection().multiply(1.6).setY(1.0));
        }
    }
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();

        if ((p.getGameMode() != GameMode.CREATIVE) && (p.getLocation().subtract(0, 1, 0).getBlock().getType() != Material.AIR) && (!p.isFlying()))
            p.setAllowFlight(true);
    }
}
