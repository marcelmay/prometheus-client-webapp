package de.m3y.prometheus.client.webapp;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Info;
import io.prometheus.client.hotspot.DefaultExports;
import io.prometheus.jmx.JmxCollector;

import javax.management.MalformedObjectNameException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.File;
import java.io.IOException;

/**
 * Handles collector configuration and registration.
 */
public class ConfigurationServletContextListener implements ServletContextListener {
    public static final String JMX_COLLECTOR_CONFIG_FILE = "jmx_collector_config_file";
    private final Info buildInfo = Info.build()
            .name("prometheus_webapp_build")
            .help("Prometheus webapp build info")
            .labelNames("version", "buildTime", "buildScmVersion", "buildScmBranch").create();
    private JmxCollector jmxCollector;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Default JVM metrics
        DefaultExports.initialize();

        // JMX metrics
        jmxCollector = initializeJmxCollector(sce);
        if (null != jmxCollector) {
            jmxCollector.register();
        }

        // Build info metric
        buildInfo.labels(
                BuildMetaInfo.INSTANCE.getVersion(),
                BuildMetaInfo.INSTANCE.getBuildTimeStamp(),
                BuildMetaInfo.INSTANCE.getBuildScmVersion(),
                BuildMetaInfo.INSTANCE.getBuildScmBranch()
        );
        buildInfo.register();
    }

    protected String getJmxCollectorConfigFile(ServletContextEvent sce) {
        // Try system property first
        String configFile = System.getProperty(JMX_COLLECTOR_CONFIG_FILE, "").trim();
        if (!configFile.isEmpty()) {
            return configFile;
        }

        // Fall back to init property
        configFile = sce.getServletContext().getInitParameter(JMX_COLLECTOR_CONFIG_FILE);
        if (null != configFile && !configFile.trim().isEmpty()) {
            return configFile;
        }

        return null;
    }

    protected JmxCollector initializeJmxCollector(ServletContextEvent sce) {
        String configFile = getJmxCollectorConfigFile(sce);
        if (null != configFile && !configFile.isEmpty()) {
            final File file = new File(configFile);
            if (!file.exists()) {
                throw new IllegalStateException("Config file " + file.getAbsolutePath() +
                        " does not exist but is set via system property " + JMX_COLLECTOR_CONFIG_FILE +
                        ". Please verify file.");
            }
            try {
                return new JmxCollector(file);
            } catch (IOException e) {
                throw new IllegalStateException("Can not initialize JMX Collector using config file " + configFile, e);
            } catch (MalformedObjectNameException e) {
                throw new IllegalStateException("Can not initialize JMX Collector", e);
            }
        }

        // Try default via web.xml init parameter
        String config = sce.getServletContext().getInitParameter("jmx_collector_config");
        if (null != config && !config.trim().isEmpty()) {
            try {
                return new JmxCollector(config);
            } catch (MalformedObjectNameException e) {
                throw new IllegalStateException("Can not initialize JMX Collector using config <" + config + ">", e);
            }
        }

        return null;
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // Currently there is unfortunately no unregister for DefaultExports

        if (null != jmxCollector) {
            CollectorRegistry.defaultRegistry.unregister(jmxCollector);
        }

        CollectorRegistry.defaultRegistry.unregister(buildInfo);
    }
}
