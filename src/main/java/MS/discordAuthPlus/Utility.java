package MS.discordAuthPlus;

import net.md_5.bungee.api.ChatColor;

import java.util.HashMap;
import java.util.Map;

public class Utility
{
    public static final String[] COLOR_CODES = {
            "&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7",
            "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f",
            "&k", "&l", "&m", "&n", "&o", "&r"
    };

    // Массив ChatColor в том же порядке, что и коды выше
    public static final ChatColor[] CHAT_COLORS = {
            ChatColor.BLACK,           // &0
            ChatColor.DARK_BLUE,       // &1
            ChatColor.DARK_GREEN,      // &2
            ChatColor.DARK_AQUA,       // &3
            ChatColor.DARK_RED,        // &4
            ChatColor.DARK_PURPLE,     // &5
            ChatColor.GOLD,            // &6
            ChatColor.GRAY,            // &7
            ChatColor.DARK_GRAY,       // &8
            ChatColor.BLUE,            // &9
            ChatColor.GREEN,           // &a
            ChatColor.AQUA,            // &b
            ChatColor.RED,             // &c
            ChatColor.LIGHT_PURPLE,    // &d
            ChatColor.YELLOW,          // &e
            ChatColor.WHITE,           // &f
            ChatColor.MAGIC,           // &k
            ChatColor.BOLD,            // &l
            ChatColor.STRIKETHROUGH,   // &m
            ChatColor.UNDERLINE,       // &n
            ChatColor.ITALIC,          // &o
            ChatColor.RESET            // &r
    };

    public static String ChatColorParser(String s)
    {
        String result = s;
        for(int i = 0; i<COLOR_CODES.length;i++)
        {
            String Replace  = COLOR_CODES[i];
            String ReplaceTO = CHAT_COLORS[i].toString();
            result = result.replaceAll(Replace,ReplaceTO);
        }
        return result;
    }
}
