package MS.discordAuthPlus;

import MS.discordAuthPlus.Strcut.AuthUser;
import MS.discordAuthPlus.Strcut.BufferedPerson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;


public class DiscordParser extends ListenerAdapter
{
    private DiscordAuthPlus plugin;
    private File UserFolder;

    public JDA jda;


    public DiscordParser(DiscordAuthPlus pl)
    {
        plugin = pl;
        UserFolder = new File(pl.getDataFolder(),"Users");
        Init();
    }

    public int MultiAccountByDiscord(String ID)
    {
        int count = 0;
        for(File f : UserFolder.listFiles())
        {
            if(f.isFile())
            {
                try
                {
                    YamlConfiguration cfg =  YamlConfiguration.loadConfiguration(f);
                    if(cfg.getString("DiscordID").equals(ID))
                    {
                        count++;
                    }
                }
                catch(Exception e)
                {
                    continue;
                }
            }
        }
        return  count;
    }
    public List<String> MultiAccountNickByDiscord(String ID)
    {
        List<String> count = new ArrayList<String>();
        for(File f : UserFolder.listFiles())
        {
            if(f.isFile())
            {
                try
                {
                    YamlConfiguration cfg =  YamlConfiguration.loadConfiguration(f);
                    if(cfg.getString("DiscordID").equals(ID))
                    {
                        count.add(cfg.getString("Nickname"));
                    }
                }
                catch(Exception e)
                {
                    continue;
                }
            }
        }
        return  count;
    }
    public int MultiAccountByIp(String Ip)
    {
        int count = 0;
        for(File f : UserFolder.listFiles())
        {
            if(f.isFile())
            {
                try
                {
                    YamlConfiguration cfg =  YamlConfiguration.loadConfiguration(f);
                    if(cfg.getString("RegisterIP").equals(Ip))
                    {
                        count++;
                    }
                }
                catch(Exception e)
                {
                    continue;
                }
            }
        }
        return  count;
    }
    public List<String> MultiAccountNickByIp(String Ip)
    {
        List<String> count = new ArrayList<String>();
        for(File f : UserFolder.listFiles())
        {
            if(f.isFile())
            {
                try
                {
                    YamlConfiguration cfg =  YamlConfiguration.loadConfiguration(f);
                    if(cfg.getString("RegisterIP").equals(Ip))
                    {
                        count.add(cfg.getString("Nickname"));
                    }
                }
                catch(Exception e)
                {
                    continue;
                }
            }
        }
        return  count;
    }
    public void Init()
    {
        jda = JDABuilder.createLight(plugin.token, EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
                .addEventListeners(this)
                .build();


        CommandListUpdateAction commands = jda.updateCommands();

        // Add all your commands on this action instance
        commands.addCommands(
                Commands.slash("connect", plugin.CDescription)
                        .addOption(INTEGER, "code", plugin.CParamDescription, true));
        commands.queue();

    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event)
    {
      switch (event.getName())
      {
          case "connect" -> {
              DiscordAuthPlus al = plugin;

              int Code = event.getOption("code", OptionMapping::getAsInt);

              for(AuthUser user : al.Auth)
              {
                  if(user.code == Code)
                  {

                      Player player = user.player;
                      YamlConfiguration yml = YamlWorker.GetByUUID(player.getUniqueId().toString());
                      int ds = MultiAccountByDiscord(event.getUser().getId().toString());
                      int ip = MultiAccountByIp(player.getAddress().getAddress().getHostAddress());
                      if(ip>plugin.MaxAccs || ds >plugin.MaxAccs)
                      {
                          int result =  ds >= ip ? ds: ip;
                          event.reply(plugin.MultiAccountLimitReachD.replaceAll("%total%",String.valueOf(result)).replaceAll("%max%",String.valueOf(plugin.MaxAccs)))
                                  .setEphemeral(true)
                                  .queue();
                          return;
                      }
                      yml.set("Nickname",player.getName());
                      yml.set("RegisterIP", player.getAddress().getAddress().getHostAddress());
                      yml.set("DiscordID",event.getUser().getId());

                      YamlWorker.Save(yml,player.getUniqueId().toString());
                      al.Auth.remove(user);
                      LoginProcessed(player);
                      event.reply(plugin.Cresponce.replace("%nick%", player.getName()))
                              .setEphemeral(true)
                              .queue();
                      return;
                  }
              }


          }
      }
    }
    public void LoginProcessed(Player p)
    {
        p.sendMessage(Utility.ChatColorParser(plugin.LoginProcessed));
        plugin.Buffer.add(new BufferedPerson(p.getUniqueId().toString(),p.getAddress().getAddress().getHostAddress()));
    }

    public void NeedAccept(Player player)
    {
        YamlConfiguration yml = YamlWorker.GetByUUID(player.getUniqueId().toString());
        String ID = yml.getString("DiscordID");
        plugin.getLogger().info(Long.parseLong(ID)+"Pidorassss");
        jda.retrieveUserById(Long.parseLong(ID))
                .queue(user ->
                {
                    user.openPrivateChannel()
                            .queue(ctx ->
                            {
                                ctx.sendMessage(plugin.AcceptJoin)
                                        .addActionRow(
                                                Button.primary("accept|"+player.getUniqueId().toString(),plugin.Me),
                                                Button.danger("decine|"+player.getUniqueId().toString(),plugin.ItsNotMe))
                                        .queue();


                            });
                });

    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        event.getMessage().delete().queue();
        String UUID = id.substring(7);
        Player p = plugin.getServer().getPlayer(java.util.UUID.fromString(UUID));

        if (id.contains("accept"))
        {

            if(p==null)
            {
                event.reply(plugin.SessionLose)
                        .setEphemeral(true)
                        .queue();
                return;
            }
            if(!p.isConnected())
            {
                event.reply(plugin.SessionLose)
                        .setEphemeral(true)
                        .queue();
                return;
            }
            if(plugin.JoinedAuth.contains(p))
            {
                plugin.JoinedAuth.remove(p);
                LoginProcessed(p);
                event.reply(plugin.Accepted)
                        .setEphemeral(true)
                        .queue();
                return;
            }
            else
            {
                event.reply(plugin.SessionLose)
                        .setEphemeral(true)
                        .queue();
                return;
            }



        }
        else if (id.contains("decine")) {

            if(p==null)
            {
                event.reply(plugin.SessionLose)
                        .setEphemeral(true)
                        .queue();
                return;
            }
            if(!p.isConnected())
            {
                event.reply(plugin.SessionLose)
                        .setEphemeral(true)
                        .queue();
                return;
            }
            if(!plugin.JoinedAuth.contains(p))
            {
                event.reply(plugin.SessionLose)
                        .setEphemeral(true)
                        .queue();
                return;
            }

            plugin.getServer().getScheduler().runTask(plugin, ()->{
                    p.kickPlayer(Utility.ChatColorParser(plugin.DecineAuthMessage));
            });

            plugin.BannedIps.add(p.getAddress().getAddress().getHostAddress());
            new BukkitRunnable()
            {
                @Override
                public void run()
                {

                }
            }.runTaskLater(plugin,20*60*10);
            event.reply(plugin.Decine)
                    .setEphemeral(true)
                    .queue();
            return;
        }
    }


}
