package de.maxhenkel.voicechat.compatibility;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils {

    public static <T> T callMethod(Object object, String... methodNames) {
        return callMethod(object, methodNames, new Class[]{});
    }

    public static <T> T callMethod(Object object, String[] methodNames, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object.getClass(), object, methodNames, parameterTypes, args);
    }

    public static <T> T callMethod(Object object, String methodName, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object.getClass(), object, new String[]{methodName}, parameterTypes, args);
    }

    public static <T> T callMethod(Class<?> clazz, Object object, String... methodNames) {
        return callMethod(clazz, object, methodNames, new Class[]{});
    }

    public static <T> T callMethod(Class<?> clazz, Object object, String methodName, Class<?>[] parameterTypes, Object... args) {
        return callMethod(clazz, object, new String[]{methodName}, parameterTypes, args);
    }

    public static <T> T callMethod(Class<?> clazz, Object object, String[] methodNames, Class<?>[] parameterTypes, Object... args) {
        for (String name : methodNames) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return (T) method.invoke(object, args);
            } catch (Throwable ignored) {
                try {
                    Method method = clazz.getMethod(name, parameterTypes);
                    method.setAccessible(true);
                    return (T) method.invoke(object, args);
                } catch (Throwable ignored2) {
                }
            }
        }
        throw new CompatibilityReflectionException(String.format("Could not find any of the following methods in the class %s: %s", clazz.getSimpleName(), String.join(", ", methodNames)));
    }

    public static <T> T callMethod(Class<?> object, String... methodNames) {
        return callMethod(object, methodNames, new Class[]{});
    }

    public static <T> T callMethod(Class<?> object, String[] methodNames, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object, null, methodNames, parameterTypes, args);
    }

    public static <T> T callMethod(Class<?> object, String methodName, Class<?>[] parameterTypes, Object... args) {
        return callMethod(object, null, new String[]{methodName}, parameterTypes, args);
    }

    public static Method getMethod(Class<?> clazz, String... methodNames) {
        return getMethod(clazz, methodNames, new Class[]{});
    }

    public static Method getMethod(Class<?> clazz, String[] methodNames, Class<?>[] parameterTypes) {
        for (String name : methodNames) {
            try {
                Method method = clazz.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (Throwable ignored) {
                try {
                    Method method = clazz.getMethod(name, parameterTypes);
                    method.setAccessible(true);
                    return method;
                } catch (Throwable ignored2) {
                }
            }
        }
        throw new CompatibilityReflectionException(String.format("Could not find any of the following methods in the class %s: %s", clazz.getSimpleName(), String.join(", ", methodNames)));
    }

    public static <T> T call(Method method, @Nullable Object instance, Object... args) {
        try {
            return (T) method.invoke(instance, args);
        } catch (Throwable t) {
            throw new CompatibilityReflectionException(String.format("Failed to invoke method %s", method.getName()), t);
        }
    }

    public static <T> T callConstructor(Class<?> object) {
        return callConstructor(object, new Class[]{});
    }

    public static <T> T callConstructor(Class<?> object, Class<?>[] parameterTypes, Object... args) {
        try {
            Constructor<?> constructor = object.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return (T) constructor.newInstance(args);
        } catch (Throwable t) {
            throw new CompatibilityReflectionException(String.format("Failed to invoke constructor of %s", object.getName()), t);
        }
    }

    public static <T> Constructor<T> getConstructor(Class<T> object, Class<?>... parameterTypes) {
        try {
            Constructor<T> constructor = object.getDeclaredConstructor(parameterTypes);
            constructor.setAccessible(true);
            return constructor;
        } catch (Throwable t) {
            throw new CompatibilityReflectionException(String.format("Failed to get constructor of %s", object.getName()), t);
        }
    }

    public static <T> T newInstance(Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (Throwable t) {
            throw new CompatibilityReflectionException(String.format("Failed to invoke constructor of %s", constructor.getDeclaringClass().getName()), t);
        }
    }

    public static <T> T getField(Object object, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = object.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                return (T) field.get(object);
            } catch (Throwable ignored) {
                try {
                    Field field = object.getClass().getField(fieldName);
                    field.setAccessible(true);
                    return (T) field.get(object);
                } catch (Throwable ignored2) {
                }
            }
        }
        throw new CompatibilityReflectionException(String.format("Could not find any of the following fields in the class %s: %s", object.getClass().getSimpleName(), String.join(", ", fieldNames)));
    }

    public static <T> T getField(Class<?> clazz, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return (T) field.get(null);
            } catch (Throwable ignored) {
                try {
                    Field field = clazz.getField(fieldName);
                    field.setAccessible(true);
                    return (T) field.get(null);
                } catch (Throwable ignored2) {
                }
            }
        }
        throw new CompatibilityReflectionException(String.format("Could not find any of the following fields in the class %s: %s", clazz.getSimpleName(), String.join(", ", fieldNames)));
    }

    /**
     * @param classNames the class names including the package name
     * @return the class
     */
    public static Class<?> getClazz(String... classNames) {
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (Throwable ignored) {
            }
        }
        throw new CompatibilityReflectionException(String.format("Could not find any of the following classes: %s", String.join(", ", classNames)));
    }

    public static boolean doesClassExist(String... classNames) {
        for (String className : classNames) {
            try {
                Class.forName(className);
                return true;
            } catch (Throwable ignored) {
            }
        }
        return false;
    }

    public static boolean doesMethodExist(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        try {
            clazz.getDeclaredMethod(methodName, parameterTypes);
            return true;
        } catch (Throwable ignored) {
            try {
                clazz.getMethod(methodName, parameterTypes);
                return true;
            } catch (Throwable ignored2) {
            }
        }
        return false;
    }

}
