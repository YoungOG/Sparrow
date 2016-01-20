package com.breakmc.sparrow.queue;

import com.breakmc.sparrow.Sparrow;
import com.breakmc.sparrow.utils.MessageManager;
import com.breakmc.sparrow.utils.PlayerUtility;
import com.empcraft.InSignsPlus;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

@Getter
@Setter
public class ServerQueue {

    private Server server;
    private Queue<UUID> supremeQueue = new LinkedList<>();
    private Queue<UUID> donatorQueue = new LinkedList<>();
    private Queue<UUID> normalQueue = new LinkedList<>();
    private boolean paused;
    private int seconds = 2;

    public ServerQueue(Server server) {
        this.server = server;
        this.paused = false;

        updateQueue();
    }

    public void updateQueue() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (isPaused()) {
                    setSeconds(10);
                } else {
                    setSeconds(2);
                }
            }
        }.runTaskTimerAsynchronously(Sparrow.getInstance(), 0L, 20L);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (isPaused()) {
                    for (UUID id : getFullQueue()) {
                        MessageManager.sendMessage(id, "&4&m-----------------------------------------------------");
                        MessageManager.sendMessage(id, "&r");
                        MessageManager.sendMessage(id, "&r &r&cThe queue is currently paused!");
                        MessageManager.sendMessage(id, "&r &r&cQueuing will resume shortly!");
                        MessageManager.sendMessage(id, "&r");
                        MessageManager.sendMessage(id, "&4&m-----------------------------------------------------");
                    }
                } else {
                    if (getFullQueue().size() > 0) {
                        ArrayList<UUID> joinList = new ArrayList<>();
                        int count = 0;

                        for (UUID id : getFullQueue()) {
                            if (count < 3) {
                                count++;
                                joinList.add(id);
                            }
                        }

                        for (UUID id : joinList) {
                            Sparrow.getInstance().getServerManager().removeFromServerQueue(Bukkit.getPlayer(id), server);
                            PlayerUtility.connect(Bukkit.getPlayer(id), server.getName());
                            MessageManager.sendMessage(id, "&bConnecting you to " + server.getName() + "...");
                        }
                    }
                }
            }
        }.runTaskTimerAsynchronously(Sparrow.getInstance(), 0L, getSeconds()*20L);
    }

    public void addToSupremeQueue(Player p) {
        supremeQueue.add(p.getUniqueId());
        InSignsPlus.getPlugin(InSignsPlus.class).updateAllSigns(p, p.getLocation());
    }

    public boolean isInQueue(UUID id) {
        return supremeQueue.contains(id) || donatorQueue.contains(id) || normalQueue.contains(id);
    }

    public Queue<UUID> getFullQueue() {
        Queue<UUID> queue = new LinkedList<>();

        for (UUID id : supremeQueue) {
            queue.add(id);
        }
        for (UUID id : donatorQueue) {
            queue.add(id);
        }
        for (UUID id : normalQueue) {
            queue.add(id);
        }

        return queue;
    }
}
