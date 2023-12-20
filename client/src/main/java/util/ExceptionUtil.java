package util;

import java.util.Optional;
import java.util.stream.Stream;

public class ExceptionUtil {
    public static Throwable getRoot(Throwable e) {
        Optional<Throwable> rootCause = Stream.iterate(e, Throwable::getCause)
                .filter(element -> element.getCause() == null)
                .findFirst();

        return rootCause.orElse(e);
    }
}
