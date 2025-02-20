package cc.carm.lib.configuration.source.nightconfig;

import cc.carm.lib.configuration.commentable.Commentable;
import cc.carm.lib.configuration.source.ConfigurationHolder;
import cc.carm.lib.configuration.source.file.FileConfigSource;
import cc.carm.lib.configuration.source.section.MemorySection;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

/**
 * 抽象的 night-config 源
 * @param <F> night-config {@link ConfigFormat} 配置格式，抽象类
 */
public abstract class NightConfigSource<F extends ConfigFormat<?>>
        extends FileConfigSource<MemorySection, Map<String, Object>, NightConfigSource<F>> {
    @NotNull private final ConfigFormat<? extends Config> format;
    protected @Nullable MemorySection rootSection;

    protected NightConfigSource(
            @NotNull ConfigFormat<? extends Config> format,
            @NotNull ConfigurationHolder<? extends NightConfigSource<F>> holder,
            @NotNull File file, @Nullable String resourcePath
    ) {
        super(holder, 0, file, resourcePath);
        this.format = format;

        this.initialize();
    }

    public void initialize() {
        try {
            this.initializeFile();
            this.onReload();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected @NotNull NightConfigSource<F> self() {
        return this;
    }

    @Override
    public @NotNull Map<String, Object> original() {
        return this.section().data();
    }

    @Override
    public @NotNull MemorySection section() {
        return Objects.requireNonNull(this.rootSection, "Root section is not initialized.");
    }

    public @NotNull String saveToString() {
        return this.format.createWriter().writeToString(this.newRootNightConfig(this.original()));
    }

    @Override
    public void save() throws Exception {
        this.fileWriter(w -> w.write(NightConfigSource.this.saveToString()));
    }

    @Override
    protected void onReload() throws Exception {
        this.rootSection = this.fileReadString(this::loadFromString);
    }

    protected @NotNull MemorySection loadFromString(@NotNull String data) throws ParsingException {
        ConfigParser<? extends Config> parser = this.format.createParser(); // 从配置格式对象创建新的解析器对象
        Config src = parser.parse(data); // 解析字符串数据为 night-config 配置对象
        LinkedHashMap<String, Object> dst = new LinkedHashMap<>(); // 解析结果存放容器
        NightConfigSource.fromNightConfig(src, dst); // 递归将配置对象的内容写入进解析结果存放容器
        return MemorySection.root(this, dst);
    }

    /**
     * 递归将 night-config 配置对象的内容写入进 Map 对象
     *
     * @param src night-config 配置对象
     * @param dst Map 对象
     */
    private static void fromNightConfig(@NotNull Config src, @NotNull Map<String, Object> dst) {
        src.entrySet().forEach(entry -> { // 遍历配置对象的键值对
            String key = entry.getKey();
            Object value = entry.getRawValue();
            if (value instanceof Config) { // another section
                LinkedHashMap<String, Object> subDst = new LinkedHashMap<>(); // 创建新的子结果节点
                dst.put(key, subDst); // 将子结果节点放入父结果节点
                NightConfigSource.fromNightConfig((Config) value, subDst); // 递归
            } else { // raw value
                dst.put(key, value);
            }
        });
    }

    /**
     * 递归将 Map 对象的内容写入 night-config 配置对象
     *
     * @param dst night-config 配置对象
     * @param src Map 对象
     */
    private static void toNightConfig(@NotNull CommentedConfig dst, @NotNull Map<String, Object> src) {
        src.forEach((key, value) -> {
            if (value instanceof Map) {
                CommentedConfig subDst = dst.createSubConfig();
                @SuppressWarnings("unchecked")
                Map<String, Object> subSrc = (Map<String, Object>) value;
                NightConfigSource.toNightConfig(subDst, subSrc);
                dst.set(key, subDst);
            }
            dst.set(key, value);
        });
    }

    /**
     * 从根 Map 对象创建根 Config 对象
     * 并填充注释信息
     *
     * @param src 根 Map 对象
     * @return 新建的根 Config 对象
     * @see ConfigFormat#createConfig()
     * @see NightConfigSource#toNightConfig(CommentedConfig, Map)
     * @see NightConfigSource#fillHeaderComments(CommentedConfig)
     */
    private @NotNull CommentedConfig newRootNightConfig(@NotNull Map<String, Object> src) {
        CommentedConfig dst = (CommentedConfig) this.format.createConfig(); // 从配置格式创建空配置对象
        NightConfigSource.toNightConfig(dst, src); // 递归将 Map 对象内容写入 night-config 配置对象
        this.fillHeaderComments(dst); // 填充注释信息
        return dst;
    }

    public @Nullable List<String> getHeaderComments(@Nullable String key) {
        return Commentable.getHeaderComments(holder(), key);
    }

    /**
     * 从元数据获取所有的 (path, headerComments) 键值对
     *
     * @return 所有的 (path, headerComments) 键值对
     */
    private @NotNull Map<String, List<String>> getAllHeaderComments() {
        return this.holder().metadata().keySet().stream()
                .collect(
                        LinkedHashMap::new,
                        (result, key) -> {
                            @Nullable List<String> headerComments = NightConfigSource.this.getHeaderComments(key);
                            if (headerComments != null) {
                                result.put(key, headerComments);
                            }
                        },
                        LinkedHashMap::putAll
                );
    }

    /**
     * 填充 HeaderComments 至 night-config 配置文件对象
     *
     * @param dst 需要填充的 night-config 配置文件对象
     * @see NightConfigSource#getAllHeaderComments()
     */
    private void fillHeaderComments(@NotNull CommentedConfig dst) {
        this.getAllHeaderComments().forEach((key, value) -> {
            dst.setComment(key, String.join("\n", value));
        });
    }
}
