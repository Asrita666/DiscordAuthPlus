package MS.discordAuthPlus;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.file.YamlConfigurationOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;

public  class YamlWorker
{
    private static DiscordAuthPlus plugin;
    private static File UserFolder;
    public static void Configure(DiscordAuthPlus p)
    {
        plugin = p;
        UserFolder = new File(p.getDataFolder(),"Users");
    }
    public static YamlConfiguration GetByUUID(String UUID)
    {
        File userYML = new File(UserFolder, UUID +".yml");

        if(!userYML.exists())
        {
              return CreateDefaultYaml(UUID);
        }
        return  YamlConfiguration.loadConfiguration(userYML);
    }
    public static void Save(YamlConfiguration y, String UUID)
    {
        File file = new File(UserFolder,UUID+".yml");
        try
        {
            y.save(file);
        }
        catch (IOException e)
        {
            plugin.getLogger().severe("ERROR WITH " +UUID);
        }
    }
    private static YamlConfiguration CreateDefaultYaml(String UUID)
    {
        YamlConfiguration yml = new YamlConfiguration();

        yml.set("RegisterIP","");
        yml.set("Nickname","");
        yml.set("DiscordID","");
        Save(yml,UUID);
        return yml;
    }


}
