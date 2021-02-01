package com.purplehillsbooks.streams;

import java.util.Comparator;
import java.util.List;


/**
 * This is for an envelope sort.  Needed when you have a collection of objects
 * but the objects themselves don't know the order, instead the order is specified externally
 * and might or might not be provided.
 *
 * In order to sort them, make a list of Envelope<MyClass> objects and specify the sort
 * value at the same time that you put the object into the envelope.  Then sort the list of
 * envelopes.  then, iterate the envelopes and retrieve the objects in the right order.
 */
public class Envelope<T> {
    public int order = 0;
    public T contents;

    public Envelope(int _order, T ttt) {
        contents = ttt;
        order = _order;
    }

    public void sortEnvelopes(List<Envelope<T>> list) {
        list.sort(new EnvelopeComparator());
    }

    private class EnvelopeComparator implements Comparator<Envelope<T>> {
        @Override
        public int compare(Envelope<T> o1, Envelope<T> o2) {
            return Integer.compare(o1.order, o2.order);
        }
    }
}