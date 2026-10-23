package nl.han.ica.datastructures;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class HANStack<T> implements IHANStack<T>{

    private final ArrayList<T> items = new ArrayList<>();

    @Override
    public void push(T value) {
        items.add(value);
    }

    @Override
    public T pop() {
        if (items.isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
       return items.remove(items.size()-1);
    }

    @Override
    public T peek() {
        if (items.isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }

      return items.get(items.size() - 1 );
    }
}
