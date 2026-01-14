package MS.discordAuthPlus;

import MS.discordAuthPlus.Strcut.AuthUser;
import MS.discordAuthPlus.Strcut.BufferedPerson;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.EntitySelectorArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import jdk.jshell.execution.Util;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DiscordAuthPlus extends JavaPlugin {
    public DiscordParser Discord;

    public String TimeExpiredMessage = "&4&lВы не успели привязать дискорд! Время вышло!";
    public String EnterCodeMessage = "&e|   Пропишите команду &6/connect &c&n&l%code%&e на дискорд сервере!";
    public String AcceptAuthMessage = "&e|   Одобрите подключение через дискорд бота(Личные сообщения должны быть открыты)";
    public String DecineAuthMessage = "&4&l Вам отклонили подключение на сервер (IP адрес забанен на 10 минут)";
    public String ConfigReloaded = "&e|   Конфиг успешно перезагружен!";

    public String UnregistredUserFail = "&c|   Данный пользователь не привязан!";
    public String SuccessUnregistredUser = "&e|   &aуспешная&e отвязка пользователя &6%player%&e!";
    public String UnregistredUserNotify = "&cВаш аккаунт отвязали от Discord!";

    public String NotPlayerError = "&c|   Вы не игрок!";
    public String SuccessUnconnectPlayer = "&eВы &aуспешно &eотвязали аккаунт от дискорда";

    public String token = "";

    public String CDescription = "Подключить дискорд аккаунт к майнкрафту";
    public String CParamDescription = "Никнейм в майнкрафте";

    public String Cresponce = "Вы успешно привязали аккаунт %nick%";
    public String LoginProcessed = "&e|   Вы &aуспешно&e авторизовались!";

    public String AcceptJoin = "Одобрите подключение на сервер";
    public String SessionLose = "Сессия истекла!";
    public String Accepted = "Вы одобрили подключение!";
    public String Decine = "Вы отклонили подключение!";
    public String Me = "Одобрить";
    public String ItsNotMe = "Это не я";
    public String MultiAccountLimitReachD = "Вы привысили лимит по мульти аккаунтам! %total%/%max%";

    public String[] InfoCmd = {"&e|   &f%nick%&7(%UUID%)&r","&e|   Мульти-аккаунтов: &6%multi%/&c&l%max% &7(%accs%)","&e|   Регистрационный айпи: %answer% &6|&a%regip%&6|&c%ip%","&e|   Закэшированный айпи: &6%CacheIp%"};
    public Integer MaxAccs=1;

    public List<Player> JoinedAuth = new ArrayList<Player>();
    public List<AuthUser> Auth = new ArrayList<AuthUser>();
    public List<BufferedPerson> Buffer = new ArrayList<BufferedPerson>();
    public List<String> BannedIps = new ArrayList<String>();

    @Override
    public void onEnable() {
        YamlWorker.Configure(this);
        saveDefaultConfig();
        saveResource("config.yml",false);
        if(getConfig().getString("token").isEmpty())
        {
            getLogger().info("TOKEN IS NULL!");
            return;
        }
        LoadCfg();

        Discord = new DiscordParser(this);
        getServer().getPluginManager().registerEvents(new AuthListener(this),this);



        LiteralArgumentBuilder<CommandSourceStack> DiscordAuthPlus = LiteralArgumentBuilder.<CommandSourceStack>literal("DiscordAuthPlus")
                .then(Commands.literal("reload")
                        .requires(ctx-> ctx.getSender().hasPermission("DiscordAuthPlus.reload"))
                        .executes(ctx->
                        {
                            LoadCfg();
                            ctx.getSource().getSender().sendMessage(Utility.ChatColorParser(ConfigReloaded));
                            return 1;
                        }))
                .then(Commands.literal("UnconnectUser")
                        .requires(ctx-> ctx.getSender().hasPermission("DiscordAuthPlus.UnconnectUser"))
                        .then(Commands.argument("User", ArgumentTypes.player())
                                .executes(ctx->
                                {
                                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("User", PlayerSelectorArgumentResolver.class);
                                    Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                    CommandSender sender = ctx.getSource().getSender();
                                    YamlConfiguration cfg = YamlWorker.GetByUUID(target.getUniqueId().toString());
                                    if(cfg.getString("Nickname") =="")
                                    {
                                        sender.sendMessage(Utility.ChatColorParser(UnregistredUserFail));
                                    }
                                    else
                                    {
                                        cfg.set("Nickname","");
                                        cfg.set("DiscordID","");
                                        cfg.set("RegisterIP","");

                                        YamlWorker.Save(cfg,target.getUniqueId().toString());
                                        if(target.isConnected())
                                        {
                                            target.kickPlayer(Utility.ChatColorParser(UnregistredUserNotify));
                                        }
                                        sender.sendMessage(Utility.ChatColorParser(SuccessUnregistredUser.replaceAll("%player%",target.getName())));

                                    }
                                    return 1;
                                })))
                .then(Commands.literal("UserInfo")
                        .requires(ctx-> ctx.getSender().hasPermission("DiscordAuthPlus.UserInfo"))
                        .then(Commands.argument("User", ArgumentTypes.player())
                                .executes(ctx->
                                {
                                    PlayerSelectorArgumentResolver targetResolver = ctx.getArgument("User", PlayerSelectorArgumentResolver.class);
                                    Player target = targetResolver.resolve(ctx.getSource()).getFirst();
                                    CommandSender sender = ctx.getSource().getSender();
                                    YamlConfiguration cfg = YamlWorker.GetByUUID(target.getUniqueId().toString());
                                    if(cfg.getString("Nickname") =="")
                                    {
                                        sender.sendMessage(Utility.ChatColorParser(UnregistredUserFail));
                                    }
                                    else
                                    {
                                        String RegistredIp = cfg.getString("RegisterIP");
                                        String TargetIp = target.getAddress().getAddress().getHostAddress();
                                        String CachedIp = "None";
                                        for(BufferedPerson p : Buffer)
                                        {
                                            if(p.player == target.getUniqueId().toString())
                                            {
                                                CachedIp = p.IP;
                                                break;
                                            }
                                        }
                                        int ds = Discord.MultiAccountByDiscord(cfg.getString("DiscordID"));
                                        int ip = Discord.MultiAccountByIp(TargetIp);
                                        int result =  ds >= ip ? ds: ip;
                                        String Answer = RegistredIp.equals(TargetIp) ? "&aTrue" : "&cFalse";
                                        List<String> array = ds >= ip ? Discord.MultiAccountNickByDiscord(TargetIp) : Discord.MultiAccountNickByIp(cfg.getString("DiscordID"));
                                        String Accs = "";
                                        for(String s : array)
                                        {
                                            Accs+= (s +" ");
                                        }

                                        String[] keys = {"%UUID%", "%nick%", "%max%", "%multi%", "%accs%", "%answer%", "%regip%", "%ip%", "%CacheIp%"};
                                        String[] values = {target.getUniqueId().toString(), target.getName(), MaxAccs.toString(), String.valueOf(result), Accs, Answer, RegistredIp, TargetIp, CachedIp};
                                        String Output = "";
                                        for(int i = 0;i<InfoCmd.length;i++) {
                                            String line = InfoCmd[i];
                                            for (int j = 0; j < keys.length; j++)
                                            {
                                                line = line.replaceAll(keys[j],values[j]);
                                            }
                                            Output+=Utility.ChatColorParser(line)+"\n";
                                        }
                                        sender.sendMessage(Output);

                                    }
                                    return 1;
                                })))
                .then(Commands.literal("Unconnect")
                        .executes(ctx->
                        {
                            CommandSender sender = ctx.getSource().getSender();
                            Entity entity = ctx.getSource().getExecutor();

                            if(entity instanceof Player)
                            {
                                Player player = (Player)entity;
                                YamlConfiguration cfg = YamlWorker.GetByUUID(player.getUniqueId().toString());
                                cfg.set("Nickname","");
                                YamlWorker.Save(cfg,player.getUniqueId().toString());
                                player.kickPlayer(Utility.ChatColorParser(UnregistredUserNotify));
                            }
                            else
                            {
                                sender.sendMessage(Utility.ChatColorParser(NotPlayerError));
                            }

                            return 1;
                        }));
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(DiscordAuthPlus.build());
        });
    }

    public void LoadCfg()
    {
        Configuration cfg = getConfig();
        InfoCmd = cfg.getStringList("InfoCommand").toArray(String[]::new);
        MultiAccountLimitReachD = cfg.getString("MultiAccLimitReach");
        SuccessUnconnectPlayer = cfg.getString("SuccessUnconnectPlayer");
        NotPlayerError = cfg.getString("NotPlayerError");
        SuccessUnregistredUser = cfg.getString("SuccessUnregistredUser");
        UnregistredUserNotify = cfg.getString("UnregistredUserNotify");
        UnregistredUserFail = cfg.getString("UnregistredUserFail");
        TimeExpiredMessage = cfg.getString("TimeExpiredMessage");
        EnterCodeMessage = cfg.getString("EnterCodeMessage");
        AcceptAuthMessage = cfg.getString("AcceptAuthMessage");
        DecineAuthMessage = cfg.getString("DeclineAuthMessage");
        ConfigReloaded = cfg.getString("CfgReloadMessage");
        token = cfg.getString("token");
        CDescription = cfg.getString("CommandDescription");
        CParamDescription = cfg.getString("CommandParamDescription");
        Cresponce = cfg.getString("Commandresponce");
        LoginProcessed = cfg.getString("LoginProcessed");
        AcceptJoin = cfg.getString("AcceptJoinMessage");
        SessionLose = cfg.getString("SessionExpired");
        Decine = cfg.getString("Decline");
        Accepted =cfg.getString("Accepted");
        Me = cfg.getString("Me");
        ItsNotMe = cfg.getString("ItsNotMe");
        MaxAccs = cfg.getInt("Max-Multi-Account");
    }
    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
