package org.redlance.platformtools.accent.impl.linux;

import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class LinuxAccentTest {
    @Test
    void usesFallbackWhenPortalDoesNotProvideAnAccent() {
        FakeSource source = new FakeSource(null);
        LinuxAccent accent = new LinuxAccent(source);
        Color fallback = new Color(12, 34, 56);

        assertEquals(fallback, accent.getAccent(() -> fallback));
        assertFalse(accent.isAvailable());

        source.accent = Color.ORANGE;
        assertEquals(Color.ORANGE, accent.getAccent(() -> fallback));
        assertTrue(accent.isAvailable());
    }

    @Test
    void forwardsPortalChangesOnlyWhileSubscribed() throws InterruptedException {
        FakeSource source = new FakeSource(Color.BLUE);
        LinuxAccent accent = new LinuxAccent(source);
        List<Color> changes = new CopyOnWriteArrayList<>();
        Consumer<Color> consumer = changes::add;

        accent.subscribeToChanges(consumer);
        assertTrue(source.listenerStarted.await(2, TimeUnit.SECONDS));

        source.emit(Color.RED);
        await(() -> changes.equals(List.of(Color.RED)));

        assertTrue(accent.unsubscribeFromChanges(consumer));
        source.emit(Color.GREEN);
        Thread.sleep(50);
        assertEquals(List.of(Color.RED), changes);
    }

    private static void await(BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(2).toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() >= deadline) fail("Condition was not met before the timeout");
            Thread.sleep(10);
        }
    }

    private static final class FakeSource implements LinuxAccentSource {
        private final CountDownLatch listenerStarted = new CountDownLatch(1);
        private volatile Color accent;
        private volatile Consumer<Color> listener;

        private FakeSource(Color accent) {
            this.accent = accent;
        }

        @Override
        public Color readAccent() {
            return this.accent;
        }

        @Override
        public void listen(BooleanSupplier active, Consumer<Color> consumer) {
            this.listener = consumer;
            this.listenerStarted.countDown();
            while (active.getAsBoolean()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }

        private void emit(Color color) {
            Consumer<Color> currentListener = this.listener;
            if (currentListener != null) currentListener.accept(color);
        }
    }
}
