package MS.discordAuthPlus;

import MS.discordAuthPlus.Strcut.AuthUser;
import MS.discordAuthPlus.Strcut.BufferedPerson;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;


import java.util.EnumSet;


import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;


public class DiscordParser extends ListenerAdapter
{
    private DiscordAuthPlus plugin;


    public JDA jda;
    public DiscordParser(DiscordAuthPlus pl)
    {
        plugin = pl;
        Init();
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

                      yml.set("Nickname",player.getName());
                      yml.set("RegisterIP", player.getAddress().getAddress().getHostAddress());
                      yml.set("DiscordID",event.getMember().getId());
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
