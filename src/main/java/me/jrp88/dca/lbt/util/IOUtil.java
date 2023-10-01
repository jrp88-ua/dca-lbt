package me.jrp88.dca.lbt.util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class IOUtil {

    public static abstract class IOHandler<T> {

        private final Class<T> type;

        protected IOHandler(Class<T> type) {this.type = type;}

        public abstract void write(DataOutputStream out, T value) throws IOException;

        public abstract T read(DataInputStream in) throws IOException;

        public void writeAll(DataOutputStream out, Collection<T> values) throws IOException {
            out.writeInt(values.size());
            for (T value : values) {
                write(out, value);
            }
        }

        public T[] readAll(DataInputStream in) throws IOException {
            int len = in.readInt();
            T[] arr = (T[]) Array.newInstance(type, len);
            for (int i = 0; i < len; i++)
                arr[i] = read(in);
            return arr;
        }

        public Class<T> type() {
            return type;
        }
    }

    public static class EnumIOHandler<T extends Enum<?>> extends IOHandler<T> {
        public EnumIOHandler(Class<T> type) {
            super(type);
        }

        @Override
        public void write(DataOutputStream out, T value) throws IOException {
            out.writeInt(value.ordinal());
        }

        @Override
        public T read(DataInputStream in) throws IOException {
            int ordinal = in.readInt();
            T[] values = type().getEnumConstants();
            return values[ordinal];
        }
    }

    private static final Map<Class<?>, IOHandler<?>> HANDLERS = new HashMap<>();

    public static <T> void addHandler(Class<T> type, IOHandler<T> handler) {
        HANDLERS.put(type, handler);
    }

    public static <T> IOHandler<T> getHandler(Class<T> type) {
        return (IOHandler<T>) HANDLERS.get(type);
    }

    public static void writeString(DataOutputStream out, String str) throws IOException {
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    public static String readString(DataInputStream in) throws IOException {
        int len = in.readInt();
        byte[] bytes = new byte[len];
        int read = in.read(bytes);
        if (read != len)
            throw new IOException("Not enough data");
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static <T> void write(Class<T> type, DataOutputStream out, T value) throws IOException {
        getHandler(type).write(out, value);
    }

    public static <T> void writeAll(Class<T> type, DataOutputStream out, Collection<T> values) throws IOException {
        getHandler(type).writeAll(out, values);
    }

    public static <T> T read(Class<T> type, DataInputStream in) throws IOException {
        return getHandler(type).read(in);
    }

    public static <T> T[] readAll(Class<T> type, DataInputStream in) throws IOException {
        return getHandler(type).readAll(in);
    }

}
