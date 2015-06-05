package arfdatatech;

public abstract class Filter {

    public final String name;

    public Filter(String name) {
        this.name = name;
    }

    /**
     * Queries the filter for the given key.
     *
     * @param key_min The minimum key in the range
     * @param key_max The maximum key in the range
     * @return true if the database contains an entry in the range of keys,
     * false otherwise
     */
    public abstract boolean query(int key_min, int key_max);

    /**
     * Called from the experiment when a false positive is detected on the 
     * indicated range
     * @param key_min The lower key value of the range
     * @param key_max The upper key value of the range
     */
    public abstract void adjustFilter(int key_min, int key_max);
    
    /**
     * Called from the experiment to add a new key to the filter
     * 
     * @param key_min The lower key value of the range
     * @param key_max The upper key value of the range
     */
    public abstract void addKey(int key_min, int key_max);
}
