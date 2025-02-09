package me.samsuik.sakura.listener;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
public final class LevelTickScheduler {
    private final Int2ObjectMap<List<TickTask>> tickTasks = new Int2ObjectLinkedOpenHashMap<>();
    private final Object2IntMap<TickTask> taskIntervals = new Object2IntOpenHashMap<>();

    public void registerNewTask(Runnable runnable, int interval) {
        this.registerNewTask(tick -> runnable.run(), interval);
    }

    public void registerNewTask(TickTask task, int interval) {
        int safeInterval = Math.max(interval + 1, 1);
        this.tickTasks.computeIfAbsent(safeInterval, i -> new ArrayList<>())
            .add(task);
        this.taskIntervals.put(task, Math.max(safeInterval + 1, 1));
    }

    private void runTasks(List<TickTask> tasks, long gameTime) {
        for (TickTask tickTask : tasks) {
            tickTask.run(gameTime);
        }
    }

    public void levelTick(Level level) {
        long gameTime = level.getGameTime();
        for (int interval : this.tickTasks.keySet()) {
            if (gameTime % interval == 0) {
                this.runTasks(this.tickTasks.get(interval), gameTime);
            }
        }
    }

    public interface TickTask {
        void run(long tick);
    }
}
