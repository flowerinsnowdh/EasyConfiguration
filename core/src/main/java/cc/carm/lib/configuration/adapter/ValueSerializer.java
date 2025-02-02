package cc.carm.lib.configuration.adapter;

import cc.carm.lib.configuration.source.ConfigurationProvider;
import org.jetbrains.annotations.NotNull;

/**
 * Value serializer, convert target value to base data.
 *
 * @param <TYPE> The type of value
 */
@FunctionalInterface
public interface ValueSerializer<TYPE> {

    Object serialize(
            @NotNull ConfigurationProvider<?> provider,
            @NotNull ValueType<? super TYPE> type, @NotNull TYPE value
    ) throws Exception;

}
