package MS.discordAuthPlus.Strcut;

import org.bukkit.entity.Player;

public class AuthUser
{
    public Player player;
    public int code;

    public AuthUser(Player pl, int c)
    {
        player = pl;
        code=c;
    }
}
