package cc.carm.lib.configuration.source.hocon;

import cc.carm.lib.configuration.source.ConfigurationHolder;
import cc.carm.lib.configuration.source.nightconfig.NightConfigSource;
import com.electronwill.nightconfig.hocon.HoconFormat;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class HOCONSource extends NightConfigSource<HoconFormat> {
    protected HOCONSource(
            @NotNull ConfigurationHolder<? extends HOCONSource> holder,
            @NotNull File file, @Nullable String resourcePath
    ) {
        super(HoconFormat.instance(), holder, file, resourcePath);
    }
}
