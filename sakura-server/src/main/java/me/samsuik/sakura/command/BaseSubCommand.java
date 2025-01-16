package me.samsuik.sakura.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@DefaultQualifier(NonNull.class)
public abstract class BaseSubCommand extends Command {
    public BaseSubCommand(String name) {
        super(name);
        this.description = "Sakura Command " + name;
        this.setPermission("bukkit.command." + name);
    }

    public abstract void execute(CommandSender sender, String[] args);

    public void tabComplete(List<String> list, String[] args) throws IllegalArgumentException {}

    @Override
    @Deprecated
    public final boolean execute(CommandSender sender, String label, String[] args) {
        if (this.testPermission(sender)) {
            this.execute(sender, args);
        }

        return true;
    }

    @Override
    @NotNull
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) throws IllegalArgumentException {
        List<String> completions = new ArrayList<>(0);

        if (this.testPermissionSilent(sender)) {
            this.tabComplete(completions, args);
        }

        return completions;
    }

    protected final Optional<Integer> parseInt(String[] args, int index) {
        return this.parse(args, index, Integer::parseInt);
    }

    protected final Optional<Long> parseLong(String[] args, int index) {
        return this.parse(args, index, Long::parseLong);
    }

    protected final Optional<Float> parseFloat(String[] args, int index) {
        return this.parse(args, index, Float::parseFloat);
    }

    protected final Optional<Double> parseDouble(String[] args, int index) {
        return this.parse(args, index, Double::parseDouble);
    }

    protected final <T> Optional<T> parse(String[] args, int index, Function<String, T> func) {
        try {
            String arg = args[index];
            return Optional.of(func.apply(arg));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ignored) {
            return Optional.empty();
        }
    }
}
