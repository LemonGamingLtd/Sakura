package me.samsuik.sakura.command.subcommands;

import me.samsuik.sakura.command.BaseSubCommand;
import me.samsuik.sakura.player.visibility.VisibilityGui;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class FPSCommand extends BaseSubCommand {
    private final VisibilityGui visibilityGui = new VisibilityGui();

    public FPSCommand(String name) {
        super(name);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof Player player) {
            this.visibilityGui.showTo(player);
        }
    }
}
