package org.redlance.platformtools.accent.impl.linux;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.DoubleByReference;
import com.sun.jna.ptr.PointerByReference;
import org.jetbrains.annotations.Nullable;
import org.redlance.platformtools.accent.impl.linux.jna.DBusLibrary;
import org.redlance.platformtools.accent.impl.linux.jna.DBusMessageIter;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

final class XdgSettingsPortal implements LinuxAccentSource {
    private static final String PORTAL_DESTINATION = "org.freedesktop.portal.Desktop";
    private static final String PORTAL_PATH = "/org/freedesktop/portal/desktop";
    private static final String SETTINGS_INTERFACE = "org.freedesktop.portal.Settings";
    private static final String APPEARANCE_NAMESPACE = "org.freedesktop.appearance";
    private static final String ACCENT_COLOR_KEY = "accent-color";
    private static final String SETTING_CHANGED_SIGNAL = "SettingChanged";
    private static final String SIGNAL_MATCH =
            "type='signal',sender='" + PORTAL_DESTINATION + "',interface='" + SETTINGS_INTERFACE
                    + "',member='" + SETTING_CHANGED_SIGNAL
                    + "',path='" + PORTAL_PATH + "',arg0='" + APPEARANCE_NAMESPACE
                    + "',arg1='" + ACCENT_COLOR_KEY + "'";

    private static final int METHOD_TIMEOUT_MILLISECONDS = 2_000;
    private static final int LISTENER_POLL_MILLISECONDS = 250;
    private static boolean dbusInitialized;

    @Override
    public @Nullable Color readAccent() {
        DBusLibrary library = DBusLibrary.getInstance();
        Pointer connection = openConnection(library);
        if (connection == null) return null;

        try {
            Pointer request = createReadRequest(library);
            if (request == null) return null;

            Pointer reply;
            try {
                reply = library.dbus_connection_send_with_reply_and_block(
                        connection,
                        request,
                        METHOD_TIMEOUT_MILLISECONDS,
                        Pointer.NULL
                );
            } finally {
                library.dbus_message_unref(request);
            }

            if (reply == null) return null;
            try {
                return readColor(library, reply);
            } finally {
                library.dbus_message_unref(reply);
            }
        } finally {
            closeConnection(library, connection);
        }
    }

    @Override
    public void listen(BooleanSupplier active, Consumer<Color> consumer) {
        DBusLibrary library = DBusLibrary.getInstance();
        Pointer connection = openConnection(library);
        if (connection == null) return;

        try {
            library.dbus_bus_add_match(connection, SIGNAL_MATCH, Pointer.NULL);
            library.dbus_connection_flush(connection);

            while (active.getAsBoolean() && library.dbus_connection_get_is_connected(connection) != 0) {
                if (library.dbus_connection_read_write(connection, LISTENER_POLL_MILLISECONDS) == 0) break;
                drainMessages(library, connection, consumer);
            }

            if (library.dbus_connection_get_is_connected(connection) != 0) {
                library.dbus_bus_remove_match(connection, SIGNAL_MATCH, Pointer.NULL);
                library.dbus_connection_flush(connection);
            }
        } finally {
            closeConnection(library, connection);
        }
    }

    private static @Nullable Pointer openConnection(DBusLibrary library) {
        synchronized (XdgSettingsPortal.class) {
            if (!dbusInitialized) {
                if (library.dbus_threads_init_default() == 0) return null;
                dbusInitialized = true;
            }
        }

        Pointer connection = library.dbus_bus_get_private(DBusLibrary.DBUS_BUS_SESSION, Pointer.NULL);
        if (connection != null) library.dbus_connection_set_exit_on_disconnect(connection, 0);
        return connection;
    }

    private static void closeConnection(DBusLibrary library, Pointer connection) {
        library.dbus_connection_close(connection);
        library.dbus_connection_unref(connection);
    }

    private static @Nullable Pointer createReadRequest(DBusLibrary library) {
        Pointer request = library.dbus_message_new_method_call(
                PORTAL_DESTINATION,
                PORTAL_PATH,
                SETTINGS_INTERFACE,
                "Read"
        );
        if (request == null) return null;

        DBusMessageIter iter = new DBusMessageIter();
        library.dbus_message_iter_init_append(request, iter);
        if (!appendString(library, iter, APPEARANCE_NAMESPACE)
                || !appendString(library, iter, ACCENT_COLOR_KEY)) {
            library.dbus_message_unref(request);
            return null;
        }
        return request;
    }

    private static boolean appendString(DBusLibrary library, DBusMessageIter iter, String value) {
        byte[] encoded = Native.toByteArray(value);
        Memory string = new Memory(encoded.length);
        string.write(0, encoded, 0, encoded.length);

        PointerByReference reference = new PointerByReference(string);
        return library.dbus_message_iter_append_basic(
                iter,
                DBusLibrary.DBUS_TYPE_STRING,
                reference.getPointer()
        ) != 0;
    }

    private static void drainMessages(DBusLibrary library, Pointer connection, Consumer<Color> consumer) {
        Pointer message;
        while ((message = library.dbus_connection_pop_message(connection)) != null) {
            try {
                if (library.dbus_message_is_signal(message, SETTINGS_INTERFACE, SETTING_CHANGED_SIGNAL) == 0) {
                    continue;
                }

                Color color = readChangedColor(library, message);
                if (color != null) consumer.accept(color);
            } finally {
                library.dbus_message_unref(message);
            }
        }
    }

    private static @Nullable Color readChangedColor(DBusLibrary library, Pointer message) {
        DBusMessageIter iter = new DBusMessageIter();
        if (library.dbus_message_iter_init(message, iter) == 0) return null;
        if (!APPEARANCE_NAMESPACE.equals(readString(library, iter))) return null;
        if (library.dbus_message_iter_next(iter) == 0) return null;
        if (!ACCENT_COLOR_KEY.equals(readString(library, iter))) return null;
        if (library.dbus_message_iter_next(iter) == 0) return null;
        return readColor(library, iter);
    }

    private static @Nullable String readString(DBusLibrary library, DBusMessageIter iter) {
        if (library.dbus_message_iter_get_arg_type(iter) != DBusLibrary.DBUS_TYPE_STRING) return null;

        PointerByReference reference = new PointerByReference();
        library.dbus_message_iter_get_basic(iter, reference.getPointer());
        Pointer value = reference.getValue();
        return value != null ? value.getString(0) : null;
    }

    private static @Nullable Color readColor(DBusLibrary library, Pointer message) {
        DBusMessageIter iter = new DBusMessageIter();
        if (library.dbus_message_iter_init(message, iter) == 0) return null;
        return readColor(library, iter);
    }

    private static @Nullable Color readColor(DBusLibrary library, DBusMessageIter iter) {
        List<Double> components = new ArrayList<>(3);
        collectDoubles(library, iter, components);
        if (components.size() != 3) return null;

        double red = components.get(0);
        double green = components.get(1);
        double blue = components.get(2);
        if (!isColorComponent(red) || !isColorComponent(green) || !isColorComponent(blue)) return null;
        return new Color((float) red, (float) green, (float) blue);
    }

    private static void collectDoubles(DBusLibrary library, DBusMessageIter iter, List<Double> components) {
        do {
            int type = library.dbus_message_iter_get_arg_type(iter);
            if (type == DBusLibrary.DBUS_TYPE_DOUBLE) {
                DoubleByReference value = new DoubleByReference();
                library.dbus_message_iter_get_basic(iter, value.getPointer());
                components.add(value.getValue());
            } else if (type == DBusLibrary.DBUS_TYPE_VARIANT || type == DBusLibrary.DBUS_TYPE_STRUCT) {
                DBusMessageIter nested = new DBusMessageIter();
                library.dbus_message_iter_recurse(iter, nested);
                collectDoubles(library, nested, components);
            }

            if (components.size() > 3) return;
        } while (library.dbus_message_iter_next(iter) != 0);
    }

    private static boolean isColorComponent(double component) {
        return Double.isFinite(component) && component >= 0.0 && component <= 1.0;
    }
}
