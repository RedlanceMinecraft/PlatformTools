package org.redlance.platformtools.accent.impl.linux.jna;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface DBusLibrary extends Library {
    int DBUS_BUS_SESSION = 0;

    int DBUS_TYPE_DOUBLE = 'd';
    int DBUS_TYPE_STRING = 's';
    int DBUS_TYPE_VARIANT = 'v';
    int DBUS_TYPE_STRUCT = 'r';

    static DBusLibrary getInstance() {
        return Holder.INSTANCE;
    }

    int dbus_threads_init_default();

    Pointer dbus_bus_get_private(int type, Pointer error);

    void dbus_bus_add_match(Pointer connection, String rule, Pointer error);

    void dbus_bus_remove_match(Pointer connection, String rule, Pointer error);

    void dbus_connection_close(Pointer connection);

    void dbus_connection_unref(Pointer connection);

    void dbus_connection_set_exit_on_disconnect(Pointer connection, int exitOnDisconnect);

    int dbus_connection_get_is_connected(Pointer connection);

    void dbus_connection_flush(Pointer connection);

    int dbus_connection_read_write(Pointer connection, int timeoutMilliseconds);

    Pointer dbus_connection_pop_message(Pointer connection);

    Pointer dbus_connection_send_with_reply_and_block(
            Pointer connection,
            Pointer message,
            int timeoutMilliseconds,
            Pointer error
    );

    Pointer dbus_message_new_method_call(String busName, String path, String iface, String method);

    void dbus_message_unref(Pointer message);

    int dbus_message_is_signal(Pointer message, String iface, String signalName);

    int dbus_message_iter_init(Pointer message, DBusMessageIter iter);

    void dbus_message_iter_init_append(Pointer message, DBusMessageIter iter);

    int dbus_message_iter_append_basic(DBusMessageIter iter, int type, Pointer value);

    int dbus_message_iter_get_arg_type(DBusMessageIter iter);

    void dbus_message_iter_recurse(DBusMessageIter iter, DBusMessageIter sub);

    void dbus_message_iter_get_basic(DBusMessageIter iter, Pointer value);

    int dbus_message_iter_next(DBusMessageIter iter);

    final class Holder {
        private static final DBusLibrary INSTANCE = Native.load("dbus-1", DBusLibrary.class);

        private Holder() {
        }
    }
}
