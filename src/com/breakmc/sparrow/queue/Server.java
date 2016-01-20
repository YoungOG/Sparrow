package com.breakmc.sparrow.queue;

import com.breakmc.sparrow.Sparrow;
import com.breakmc.sparrow.utils.PlayerUtility;
import com.empcraft.InSignsPlus;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
@Setter
public class Server {

    private String name;
    private int playerCount;
    private int maxPlayerCount;
    private Location serverSignLocation;
    private Location queueSignLocation;
    private ServerQueue serverQueue;

    public Server(String name, int maxPlayerCount, Location serverSignLocation) {
        this.name = name;
        this.playerCount = 0;
        this.maxPlayerCount = maxPlayerCount;
        this.serverSignLocation = serverSignLocation;
        this.serverQueue = new ServerQueue(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (PlayerUtility.getOnlinePlayers().length > 0) {
                    Sparrow.getInstance().getCount(PlayerUtility.getOnlinePlayers()[0], name);
                }
                for (Player all : PlayerUtility.getOnlinePlayers()) {
                    if (serverSignLocation != null) {
                        InSignsPlus.getPlugin(InSignsPlus.class).updateSign(all, serverSignLocation);
                    }

                    if (queueSignLocation != null) {
                        InSignsPlus.getPlugin(InSignsPlus.class).updateSign(all, queueSignLocation);
                    }
                }
            }
        }.runTaskTimerAsynchronously(Sparrow.getInstance(), 0L, 2*20L);
    }
}
