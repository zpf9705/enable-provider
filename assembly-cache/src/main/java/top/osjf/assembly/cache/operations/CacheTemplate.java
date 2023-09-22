package top.osjf.assembly.cache.operations;

import org.springframework.util.Assert;
import top.osjf.assembly.cache.command.CacheKeyCommands;
import top.osjf.assembly.cache.factory.CacheFactory;
import top.osjf.assembly.cache.factory.CacheFactoryAccessor;
import top.osjf.assembly.cache.serializer.PairSerializer;
import top.osjf.assembly.cache.serializer.StringPairSerializer;
import top.osjf.assembly.util.annotation.CanNull;
import top.osjf.assembly.util.annotation.NotNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class that simplifies Cache data access code.
 * <p>
 * Performs automatic serialization/deserialization between the given objects
 * and the underlying binary data in the expiry store. By default, it uses
 * Generic String serialization for its objects(through {@link StringPairSerializer}).
 * For String intensive operations consider the dedicated {@link StringCacheTemplate}.
 * <p>
 * The Expiry of the template model , imitate expireTemplate encapsulation mode
 * The cache operation way to connect assembly simulation for the executor,
 * and is equipped with a variety of executor factory
 * <p>
 * When the configuration is completed of this class are thread safe operation.
 * <p>
 * <b>his is the central class in Expiry support</b>
 *
 * @author zpf
 * @since 1.0.0
 **/
public class CacheTemplate<K, V> extends CacheFactoryAccessor implements CacheCommonsOperations<K, V>, Serializable {

    private static final long serialVersionUID = -8020854200126293536L;
    @SuppressWarnings("rawtypes")
    private @CanNull PairSerializer defaultSerializer;
    private boolean enableDefaultSerializer = true;
    private boolean initialized = false;

    private PairSerializer<K> keySerialize;
    private PairSerializer<V> valueSerialize;

    private final ValueOperations<K, V> valueOperations = new DefaultValueOperations<>(this);
    private final TimeOperations<K, V> timeOperations = new DefaultTimeOperations<>(this);

    /**
     * Constructs a new <code>CacheTemplate</code> instance.
     */
    public CacheTemplate() {
    }

    @Override
    @SuppressWarnings("unchecked")
    public void afterPropertiesSet() {
        super.afterPropertiesSet();

        boolean defaultUsed = false;

        if (this.defaultSerializer == null) {
            this.defaultSerializer = new StringPairSerializer();
        }

        if (this.enableDefaultSerializer) {

            if (this.keySerialize == null) {
                this.keySerialize = defaultSerializer;
                defaultUsed = true;
            }

            if (this.valueSerialize == null) {
                this.valueSerialize = defaultSerializer;
                defaultUsed = true;
            }
        }

        if (this.enableDefaultSerializer && defaultUsed) {

            Assert.notNull(this.defaultSerializer, "defaultSerializer must initialized");
        }

        this.initialized = true;
    }

    /**
     * Whether the default serializer should be used. If not, any serializers not explicitly
     * set will remain null and values will not be serialized or deserialized.
     *
     * @param enableDefaultSerializer The above
     */
    public void setEnableDefaultSerializer(boolean enableDefaultSerializer) {
        this.enableDefaultSerializer = enableDefaultSerializer;
    }

    /**
     * Set the template key PairSerializer.
     *
     * @param keySerializer key Serializer.
     */
    public void setKeySerializer(PairSerializer<K> keySerializer) {
        Assert.isTrue(this.keySerialize == null,
                "kPairSerializer existing configuration values, please do not cover");
        this.keySerialize = keySerializer;
    }

    /**
     * Set the template value PairSerializer.
     *
     * @param valueSerializer value Serializer.
     */
    public void setValueSerializer(PairSerializer<V> valueSerializer) {
        Assert.isTrue(this.valueSerialize == null,
                "vPairSerializer existing configuration values, please do not cover");
        this.valueSerialize = valueSerializer;
    }

    @Override
    @NotNull
    public PairSerializer<K> getKeySerializer() {
        return this.keySerialize;
    }

    @Override
    @NotNull
    public PairSerializer<V> getValueSerializer() {
        return this.valueSerialize;
    }

    @Override
    public ValueOperations<K, V> opsForValue() {
        return this.valueOperations;
    }

    @Override
    public TimeOperations<K, V> opsForTime() {
        return this.timeOperations;
    }


    @Override
    @CanNull
    public <T> T execute(CacheValueCallback<T> action) {

        Assert.isTrue(initialized, "Execute must before initialized");

        CacheFactory factory = getCacheFactory();

        Assert.notNull(factory, "CacheExecutorFactory must not be null");

        return action.doInExecutor(factory.executor());
    }

    @CanNull
    @Override
    public Boolean delete(K key) {
        Long result = execute((executor) -> executor.delete(
                this.rawKey(key)
        ));
        return result != null && result.intValue() == 1;
    }

    @CanNull
    @Override
    public Long delete(Collection<K> keys) {
        if (keys == null || keys.isEmpty()) {
            return 0L;
        }
        return this.execute((executor) -> executor.delete(
                this.rawKeys(keys)
        ));
    }

    @Override
    public Map<K, V> deleteType(K key) {

        Map<byte[], byte[]> map = this.execute((executor) -> executor.deleteType(
                this.rawKey(key)
        ));

        if (map == null || map.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<K, V> ma = new HashMap<>();
        for (byte[] keyBytes : map.keySet()) {
            byte[] valueBytes = map.get(keyBytes);
            ma.put(this.keySerialize.deserialize(keyBytes), this.valueSerialize.deserialize(valueBytes));
        }
        return ma;
    }

    @Override
    public Boolean deleteAll() {
        return this.execute(CacheKeyCommands::deleteAll);
    }

    @Override
    public Boolean exist(K key) {
        return this.execute((executor) -> executor.hasKey(
                this.rawKey(key)
        ));
    }

    private byte[] rawKey(K key) {
        Assert.notNull(key, "Non null key required");
        byte[] v;
        if (this.keySerialize != null) {
            v = this.keySerialize.serialize(key);
        } else {
            if (key instanceof byte[]) {
                return (byte[]) key;
            } else {
                v = null;
            }
        }
        return v;
    }

    private byte[][] rawKeys(Collection<K> keys) {
        final byte[][] rawKeys = new byte[keys.size()][];
        int i = 0;
        for (K key : keys) {
            rawKeys[i++] = rawKey(key);
        }
        return rawKeys;
    }

    private byte[] rawValue(V value) {
        Assert.notNull(value, "Non null value required");
        byte[] v;
        if (this.valueSerialize != null) {
            v = this.valueSerialize.serialize(value);
        } else {
            if (value instanceof byte[]) {
                v = (byte[]) value;
            } else {
                v = null;
            }
        }
        return v;
    }
}
