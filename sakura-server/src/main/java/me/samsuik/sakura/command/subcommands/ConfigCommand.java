package me.samsuik.sakura.command.subcommands;

import me.samsuik.sakura.command.BaseSubCommand;
import net.minecraft.server.MinecraftServer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

@DefaultQualifier(NonNull.class)
public final class ConfigCommand extends BaseSubCommand {
    public ConfigCommand(String name) {
        super(name);
        this.description = "Command for reloading the sakura configuration file";
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        Command.broadcastCommandMessage(sender, text("Please note that this command is not supported and may cause issues.", RED));
        Command.broadcastCommandMessage(sender, text("If you encounter any issues please use the /stop command to restart your server.", RED));

        MinecraftServer server = ((CraftServer) sender.getServer()).getServer();
        server.sakuraConfigurations.reloadConfigs(server);
        server.server.reloadCount++;

        Command.broadcastCommandMessage(sender, text("Sakura config reload complete.", GREEN));
    }
}
