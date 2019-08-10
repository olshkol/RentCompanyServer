package response;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Utils {
    public static List<Class> getClassesForPackage(String pathName){
        File file = new File(pathName);

        Class[] classes = Arrays.stream(Objects.requireNonNull(file.list((dir, name) -> name.endsWith(".java"))))
                .map(s -> s.replaceAll(".java", ""))
                .map((String className) -> {
                    try {
                        return Class.forName("cars.dto." + className);
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toArray(Class[]::new);
        return Arrays.asList(classes);
    }
}
