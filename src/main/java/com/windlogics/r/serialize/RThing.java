package com.windlogics.r.serialize;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all R objects
 * @param <T> class of underlying vector data, roughly corresponding to R's <code>typeof()</code>
 */
public abstract class RThing<T> {
    protected List<T> data;
    private RPairlist attrs;

    /**
     * Create a new R object with the given data and attributes.  The arguments are stored by reference, not cloned.
     */
    public RThing(List<T> data, RPairlist attrs) {
        this.data = data;
        this.attrs = attrs;
    }

    /**
     * Create a new R object with the given data (and no attributes).  The data is stored by reference, not cloned.
     */
    public RThing(List<T> data) {
        this(data, null);
    }

    private void putFlags(DataOutputStream os) throws IOException {
        int flags = type();
        if (flags == 0) return;

        if (attrs != null) {
            if (attrs.length() > 0)
                flags |= 1 << 9;

            if (attrs.hasKey("class"))
                flags |= 1 << 8;
        }

        os.writeInt(flags);
    }

    private void putAttrs(DataOutputStream os) throws IOException {
        if (attrs != null)
            attrs.serialize(os);
    }

    public void serialize(DataOutputStream os) throws IOException {
        putFlags(os);
        putData(os);
        putAttrs(os);
    }

    /**
     * Set an R attribute on the object.  Roughly equivalent to R's <code>attr(object, key) <- value</code>.
     */
    public RThing setAttr(String key, RThing value) {
        if (attrs == null)
            attrs = new RPairlist();
        attrs.put(key, value);
        return this;
    }

    /**
     * Sets the R attribute "class" to the given list of Strings.
     */
    public RThing setClass(List<String> c) {
        return setAttr("class", new RString(c));
    }
    /**
     * Sets the R attribute "class" to the given array of Strings.
     */
    public RThing setClass(String... c) {
        return setClass(new ArrayList<String>(Arrays.asList(c)));
    }
    /**
     * Sets the R attribute "class" to the given String.
     */
    public RThing setClass(String c) {
        return setClass(new ArrayList<>(Collections.singletonList(c)));
    }

    public abstract void putData(DataOutputStream os) throws IOException;
    public int type() { return 0; } // Override in subclasses
}
