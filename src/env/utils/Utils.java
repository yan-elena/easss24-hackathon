package utils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;

public class Utils {
    private static long SEED_ID = 0;
    public static String getBody(FullHttpRequest request) throws IOException {
        ByteBuf buf = request.content();
        byte[] bytes = new byte[buf.readableBytes()];
        int readerIndex = buf.readerIndex();
        buf.getBytes(readerIndex, bytes);
        return new String(bytes, "UTF-8");
    }

    public static String getIdentifier(String className, Object data) throws Exception {
        Class<?> myClass = Class.forName(className);
        Field[] fields = myClass.getFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Identifier.class)) {
                if (field.get(data) != null) return field.get(data).toString();
                System.out.println("Identifier for template: " + className + " is null");
                break;
            }
        }
        return Long.toString(SEED_ID++);
    }

    public static boolean isInvalidIdentifier(Field field, Object data, Object property)
            throws IllegalArgumentException, IllegalAccessException {
        return field.get(data) != null && !field.get(data).equals(property);
    }

    public static String[] cleanContentTypes(String types) {
        return types.replaceAll(" ", "").split(",");
    }

    public static ByteBuf toByteBuf(String content) {
        byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
        return Unpooled.wrappedBuffer(bytes);
    }

}
