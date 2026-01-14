package MS.discordAuthPlus;

import MS.discordAuthPlus.Strcut.AuthUser;
import MS.discordAuthPlus.Strcut.BufferedPerson;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.event.player.PlayerPickItemEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class AuthListener  implements Listener
{
    private DiscordAuthPlus plugin;




    public AuthListener(DiscordAuthPlus p)
    {
        plugin = p;


    }

    @EventHandler(priority = EventPriority.HIGH)
    public void OnPlayerLeave(PlayerQuitEvent event)
    {
        UUID ui = event.getPlayer().getUniqueId();
        for(AuthUser user : plugin.Auth)
        {
            if(user.player.getUniqueId() == ui)
            {
                plugin.Auth.remove(user);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void OnPlayerJoin(PlayerJoinEvent event)
    {
        Player player = event.getPlayer();
        if(plugin.BannedIps.contains(player.getAddress().getAddress().getHostAddress()))
        {
            player.kickPlayer(Utility.ChatColorParser(plugin.DecineAuthMessage));
            return;
        }


        YamlConfiguration yml = YamlWorker.GetByUUID(player.getUniqueId().toString());

        if(yml.getString("Nickname") == "")
        {
            FirstTimeConnect(player);
            return;
        }


        for(BufferedPerson person : plugin.Buffer)
        {
            plugin.getLogger().info(person.player);
            plugin.getLogger().info(player.getUniqueId().toString());

            if(person.player.equals( player.getUniqueId().toString()))
            {
                plugin.getLogger().info("Govno");
                if(!person.IP.equals(player.getAddress().getAddress().getHostAddress()))
                {
                    plugin.Buffer.remove(person);
                    plugin.getLogger().info("Pizda");

                    LoginRequest(player);
                    return;
                }
                else
                {
                    return;
                }
            }
        }
        plugin.getLogger().info("sukaaaa");

        LoginRequest(player);
    }


    public void LoginRequest(Player p)
    {
        plugin.JoinedAuth.add(p);
        p.sendMessage(Utility.ChatColorParser(plugin.AcceptAuthMessage));
        plugin.Discord.NeedAccept(p);
        YamlConfiguration cfg = YamlWorker.GetByUUID(p.getUniqueId().toString());

    }
    public void FirstTimeConnect(Player player)
    {
        int RandomNumber = GenerateRandom();
        AuthUser user = new AuthUser(player,RandomNumber);
        plugin.Auth.add(user);

        player.sendMessage(Utility.ChatColorParser(plugin.EnterCodeMessage.replaceAll("%code%", String.valueOf(RandomNumber))));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (plugin.Auth.contains(user) && player.isConnected()) {
                    player.kickPlayer(Utility.ChatColorParser(plugin.TimeExpiredMessage));
                }
            }
        }.runTaskLater(plugin, 20 * 60);
    }
    private int GenerateRandom()
    {
        Random random = new Random();
        int randomValue = random.nextInt(9999);

        boolean flag = true;
        while(flag)
        {
            flag = false;
            for(AuthUser user : plugin.Auth)
            {
                if(user.code == randomValue)
                {
                    flag = true;
                    break;
                }
            }
        }
        return  randomValue;
    }


    public  boolean AuthContains(Player p)
    {
        for(AuthUser a : plugin.Auth)
        {
            if(a.player == p)
            {
                return true;
            }
        }
        return  false;
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayer0(PlayerCommandPreprocessEvent event)
    {
        Player p = event.getPlayer();

        if(plugin.JoinedAuth.contains(p) || AuthContains(p))
        {
            event.setCancelled(true);

        }
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayer1(PlayerMoveEvent event)
    {
        Player p = event.getPlayer();

        if(plugin.JoinedAuth.contains(p)  || AuthContains(p))
        {
            event.setCancelled(true);

        }
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayer2(PlayerInteractEvent event)
    {
        Player p = event.getPlayer();

        if(plugin.JoinedAuth.contains(p) || AuthContains(p))
        {
            event.setCancelled(true);

        }
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayer3(PlayerDropItemEvent event)
    {
        Player p = event.getPlayer();

        if(plugin.JoinedAuth.contains(p) || AuthContains(p))
        {
            event.setCancelled(true);

        }
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayer4(PlayerChatEvent event)
    {
        Player p = event.getPlayer();

        if(plugin.JoinedAuth.contains(p) || AuthContains(p))
        {
            event.setCancelled(true);

        }
    }
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayer5(PlayerPickItemEvent event)
    {
        Player p = event.getPlayer();

        if(plugin.JoinedAuth.contains(p) || AuthContains(p))
        {
            event.setCancelled(true);

        }
    }




}
