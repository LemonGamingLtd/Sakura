package me.samsuik.sakura.command;

import me.samsuik.sakura.command.subcommands.ConfigCommand;
import me.samsuik.sakura.command.subcommands.TPSCommand;
import me.samsuik.sakura.command.subcommands.FPSCommand;
import me.samsuik.sakura.command.subcommands.VisualCommand;
import me.samsuik.sakura.player.visibility.VisibilityTypes;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;

import java.util.HashMap;
import java.util.Map;

public final class SakuraCommands {
    static final Map<String, Command> COMMANDS = new HashMap<>();
    static {
        COMMANDS.put("sakura", new SakuraCommand("sakura"));
        COMMANDS.put("config", new ConfigCommand("config"));
        COMMANDS.put("tps", new TPSCommand("tps"));
        COMMANDS.put("fps", new FPSCommand("fps"));
        COMMANDS.put("tntvisibility", new VisualCommand(VisibilityTypes.TNT, "tnttoggle"));
        COMMANDS.put("sandvisibility", new VisualCommand(VisibilityTypes.SAND, "sandtoggle"));
    }

    public static void registerCommands(MinecraftServer server) {
        COMMANDS.forEach((s, command) -> {
            server.server.getCommandMap().register(s, "sakura", command);
        });
    }
}
