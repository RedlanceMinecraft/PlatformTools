package org.redlance.platformtools.accent.impl.linux;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.accent.PlatformAccent;

import java.awt.Color;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LinuxAccent implements PlatformAccent {
    private final LinuxAccentSource source;
    private final List<Consumer<Color>> consumers = new CopyOnWriteArrayList<>();

    private long listenerGeneration;
    private boolean listenerActive;

    public LinuxAccent() {
        this(new XdgSettingsPortal());
    }

    LinuxAccent(LinuxAccentSource source) {
        this.source = source;
    }

    @Override
    public Color getAccent(@NotNull Supplier<Color> fallback) {
        Color color = readAccent();
        return color != null ? color : fallback.get();
    }

    @Override
    public void subscribeToChanges(Consumer<Color> consumer) {
        this.consumers.add(consumer);
        startListener();
    }

    @Override
    public boolean unsubscribeFromChanges(Consumer<Color> consumer) {
        boolean removed = this.consumers.remove(consumer);
        if (this.consumers.isEmpty()) stopListener();
        return removed;
    }

    @Override
    public synchronized void resubscribe() {
        this.listenerGeneration++;
        this.listenerActive = false;
        startListener();
    }

    @Override
    public boolean isAvailable() {
        return readAccent() != null;
    }

    private @Nullable Color readAccent() {
        try {
            return this.source.readAccent();
        } catch (Throwable ignored) {
            return null;
        }
    }

    private synchronized void startListener() {
        if (this.listenerActive || this.consumers.isEmpty()) return;

        this.listenerActive = true;
        long generation = ++this.listenerGeneration;
        Thread listener = new Thread(() -> runListener(generation), "PlatformTools Linux accent listener");
        listener.setDaemon(true);
        listener.start();
    }

    private void runListener(long generation) {
        try {
            this.source.listen(
                    () -> isListenerCurrent(generation),
                    color -> notifyConsumers(generation, color)
            );
        } catch (Throwable ignored) {
        } finally {
            synchronized (this) {
                if (this.listenerGeneration == generation) this.listenerActive = false;
            }
        }
    }

    private synchronized boolean isListenerCurrent(long generation) {
        return this.listenerActive && this.listenerGeneration == generation && !this.consumers.isEmpty();
    }

    private void notifyConsumers(long generation, Color color) {
        if (!isListenerCurrent(generation)) return;
        for (Consumer<Color> consumer : this.consumers) {
            consumer.accept(color);
        }
    }

    private synchronized void stopListener() {
        this.listenerGeneration++;
        this.listenerActive = false;
    }
}
