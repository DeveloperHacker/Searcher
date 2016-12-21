package utils;

import javassist.NotFoundException;

import java.util.Set;

public class Sets {

    private Sets() {

    }

    public static <E, S extends Shell<E>> S getElement(Set<S> set, E element) throws NotFoundException {
        if (element == null) {
            throw new NullPointerException();
        }
        S shell = null;
        for (S s : set) {
            if (s.inner().equals(element)) {
                shell = s;
                break;
            }
        }
        if (shell == null) {
            throw new NotFoundException(String.format("Element %s not found", element));
        }
        return shell;
    }

}
