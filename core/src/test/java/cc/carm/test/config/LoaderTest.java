package cc.carm.test.config;

import cc.carm.lib.configuration.adapter.ValueAdapterRegistry;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.source.ConfigurationHolder;
import cc.carm.lib.configuration.source.loader.ConfigurationInitializer;
import cc.carm.lib.configuration.source.option.ConfigurationOptionHolder;
import org.junit.Test;

import java.util.concurrent.ConcurrentHashMap;

public class LoaderTest {

    @Test
    public void test() throws Exception {
        ConfigurationHolder<TestSource> provider = new ConfigurationHolder<>(
                new TestSource(), new ValueAdapterRegistry(), new ConfigurationOptionHolder(),
                new ConcurrentHashMap<>(), new ConfigurationInitializer()
        );

        ConfigurationInitializer loader = new ConfigurationInitializer();
        loader.initialize(provider, ROOT.class);
    }

    interface ROOT extends Configuration {

        interface SUB extends Configuration {


        }


        @ConfigPath(root = true)
        interface EXTERNAL extends Configuration {


        }

        @ConfigPath("NO")
        interface YES extends Configuration {


        }

    }
}