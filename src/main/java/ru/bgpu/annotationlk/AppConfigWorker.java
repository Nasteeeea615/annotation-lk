package ru.bgpu.annotationlk;

import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class AppConfigWorker {

    private static Logger logger = Logger.getLogger(AppConfigWorker.class.getName());

    public static void configProcessing(String prefix, String filePropName) {

        Reflections reflections = new Reflections(prefix, Scanners.FieldAnnotated());

        File prop = new File(filePropName);
        if(prop.isFile()) {
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(prop));

                reflections.getFieldsAnnotatedWith(AppConfig.class).forEach(
                        field -> {

                            String value = properties.getProperty(
                                    field.getName(),
                                    field.getAnnotation(AppConfig.class).defValue()
                            );
                            Object targetValue = null;

                            if(field.getType().equals(String.class)) {
                                targetValue = value;
                            } else if(field.getType().equals(Integer.class)) {
                                targetValue = Integer.valueOf(value);
                            }else if (field.getType().equals(Float.class) || field.getType().equals(float.class)) {
                                targetValue = Float.valueOf(value);
                            } else if (field.getType().equals(Double.class) || field.getType().equals(double.class)) {
                                targetValue = Double.valueOf(value);
                            } else if (field.getType().isArray()) { // Проверка на массив
                                Class<?> componentType = field.getType().getComponentType();
                                if (componentType.equals(String.class)) {
                                    targetValue = Stream.of(value.split(",")).toArray(String[]::new);
                                } else if (componentType.equals(Integer.TYPE)) {
                                    targetValue = Stream.of(value.split(","))
                                            .mapToInt(Integer::parseInt)
                                            .toArray();
                                } else if (componentType.equals(Float.TYPE)) {
                                    targetValue = Stream.of(value.split(","))
                                            .map(Float::parseFloat)
                                            .toArray(Float[]::new);
                                } else if (componentType.equals(Double.TYPE)) {
                                    targetValue = Stream.of(value.split(","))
                                            .mapToDouble(Double::parseDouble)
                                            .toArray();
                                } else if (componentType.equals(Integer.class)) {
                                    targetValue = Stream.of(value.split(","))
                                            .map(Integer::valueOf)
                                            .toArray(Integer[]::new);
                                } else if (componentType.equals(Float.class)) {
                                    targetValue = Stream.of(value.split(","))
                                            .map(Float::valueOf)
                                            .toArray(Float[]::new);
                                } else if (componentType.equals(Double.class)) {
                                    targetValue = Stream.of(value.split(","))
                                            .map(Double::valueOf)
                                            .toArray(Double[]::new);
                                }
                            }



                            try {
                                field.setAccessible(true);
                                field.set(field.getDeclaringClass(), targetValue);
                                field.setAccessible(false);
                            } catch (IllegalAccessException e) {
                                logger.log(
                                        Level.WARNING,
                                        "error set "+field.getDeclaringClass().getName()
                                                +"."+field.getName()+" "+value
                                );
                            }

//                            System.out.println(field.getName());
                        }
                );
            } catch (Exception e) {
                logger.log(Level.WARNING, "error load properties", e);
            }
        } else {
            logger.log(Level.WARNING, "config file not found");
        }
    }

}
