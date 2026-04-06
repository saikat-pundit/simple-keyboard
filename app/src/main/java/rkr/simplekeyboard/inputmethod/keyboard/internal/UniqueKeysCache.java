package rkr.simplekeyboard.inputmethod.keyboard.internal;

import java.util.HashMap;

import rkr.simplekeyboard.inputmethod.keyboard.Key;

public abstract class UniqueKeysCache {
    public abstract void setEnabled(boolean enabled);
    public abstract void clear();
    public abstract Key getUniqueKey(Key key);

    public static final UniqueKeysCache NO_CACHE = new UniqueKeysCache() {
        @Override
        public void setEnabled(boolean enabled) {}

        @Override
        public void clear() {}

        @Override
        public Key getUniqueKey(Key key) { return key; }
    };

    public static UniqueKeysCache newInstance() {
        return new UniqueKeysCacheImpl();
    }

    private static final class UniqueKeysCacheImpl extends UniqueKeysCache {
        private final HashMap<Key, Key> mCache;

        private boolean mEnabled;

        UniqueKeysCacheImpl() {
            mCache = new HashMap<>();
        }

        @Override
        public void setEnabled(final boolean enabled) {
            mEnabled = enabled;
        }

        @Override
        public void clear() {
            mCache.clear();
        }

        @Override
        public Key getUniqueKey(final Key key) {
            if (!mEnabled) {
                return key;
            }
            final Key existingKey = mCache.get(key);
            if (existingKey != null) {
                // Reuse the existing object that equals to "key" without adding "key" to
                // the cache.
                return existingKey;
            }
            mCache.put(key, key);
            return key;
        }
    }
}
