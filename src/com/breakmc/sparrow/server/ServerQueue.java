package com.breakmc.sparrow.server;

import com.breakmc.sparrow.Sparrow;
import com.breakmc.sparrow.utils.Cooldowns;
import com.breakmc.sparrow.utils.MessageManager;
import com.breakmc.sparrow.utils.PlayerUtility;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.UUID;

@Getter
@Setter
public class ServerQueue {

    private Server server;
    private LinkedList<UUID> supremeQueue = new LinkedList<>();
    private LinkedList<UUID> donatorQueue = new LinkedList<>();
    private LinkedList<UUID> normalQueue = new LinkedList<>();
    private boolean paused;

    public ServerQueue(Server server) {
        this.server = server;
        this.paused = false;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (isPaused()) {
                    LinkedList<UUID> queue = new LinkedList<>();

                    ListIterator<UUID> supremeItr = supremeQueue.listIterator();
                    while (supremeItr.hasNext()) {
                        queue.add(supremeQueue.pollFirst());
                    }

                    ListIterator<UUID> donatorItr = donatorQueue.listIterator();
                    while (donatorItr.hasNext()) {
                        queue.add(donatorQueue.pollFirst());
                    }

                    ListIterator<UUID> normalItr = normalQueue.listIterator();
                    System.out.println("Normal Queue: " + normalQueue);
                    while (normalItr.hasNext()) {
                        queue.add(normalQueue.pollFirst());
                    }

                    for (UUID id : queue) {
                        if (Cooldowns.tryCooldown(id, "paused-cooldown", 10000)) {
                            MessageManager.sendMessage(id, "&4&m-----------------------------------------------------");
                            MessageManager.sendMessage(id, "&r");
                            MessageManager.sendMessage(id, "&r &r&cThe server is currently paused.");
                            MessageManager.sendMessage(id, "&r &r&cQueuing will resume shortly!");
                            MessageManager.sendMessage(id, "&r");
                            MessageManager.sendMessage(id, "&4&m-----------------------------------------------------");
                        }
                    }
                } else if (!server.isWhitelisted()) {
                    ArrayList<UUID> joinList = new ArrayList<>();

                    LinkedList<UUID> queue = new LinkedList<>();

                    ListIterator<UUID> supremeItr = supremeQueue.listIterator();
                    while (supremeItr.hasNext()) {
                        queue.add(supremeQueue.pollFirst());
                    }

                    ListIterator<UUID> donatorItr = donatorQueue.listIterator();
                    while (donatorItr.hasNext()) {
                        queue.add(donatorQueue.pollFirst());
                    }

                    ListIterator<UUID> normalItr = normalQueue.listIterator();
                    while (normalItr.hasNext()) {
                        queue.add(normalQueue.pollFirst());
                    }

                    for (int count = 0; count < 3; count++) {
                        Player poll = Bukkit.getPlayer(queue.pollFirst());

                        if (poll != null) {
                            if (PlayerUtility.getGroup(poll.getName()).equalsIgnoreCase("normal") && server.getPlayerCount() >= server.getMaxPlayerCount()) {
                                return;
                            }

                            System.out.println("Polling: " + poll.getName());

                            joinList.add(poll.getUniqueId());
                        }
                    }

                    for (UUID id : joinList) {
                        Sparrow.getInstance().getServerManager().removeFromServerQueue(Bukkit.getPlayer(id), server);
                        PlayerUtility.connect(Bukkit.getPlayer(id), server.getName());
                        MessageManager.sendMessage(id, "&bConnecting you to " + server.getName() + "...");
                    }
                }
            }
        }.runTaskTimerAsynchronously(Sparrow.getInstance(), 0L, 2*20L);
    }

    public boolean isInQueue(UUID id) {
        return supremeQueue.contains(id) || donatorQueue.contains(id) || normalQueue.contains(id);
    }
}
