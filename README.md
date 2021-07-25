# prometheus-client-webapp
A simple WAR wrapper for instrumenting your app server and exposing metrics.

This is an alternative to the usually preferred [jmx_exporter](https://github.com/prometheus/jmx_exporter)
and suitable when you have minimal access to your app server,
e.g. in a large centrally managed setup splitting operations and development responsiblities.

The WAR
* registers JVM [DefaultExports](https://github.com/prometheus/client_java/blob/master/simpleclient_hotspot/src/main/java/io/prometheus/client/hotspot/DefaultExports.java)
* registers [JmxCollector](https://github.com/prometheus/jmx_exporter/blob/master/collector/src/main/java/io/prometheus/jmx/JmxCollector.java)
* exposes metrics via [MetricsServlet](https://github.com/prometheus/client_java/blob/master/simpleclient_servlet/src/main/java/io/prometheus/client/exporter/MetricsServlet.java)

## Configuration
You can configure the [JmxCollector](https://github.com/prometheus/jmx_exporter#configuration)
1. by setting system property `jmx_collector_config_file` pointing to a local file
2. by setting in your web.xml a context parameter pointing to a local file
   ```
   <context-param>
        <param-name>jmx_collector_config_file</param-name>
        <param-value>/PATH/TO/CONFIGFILE</param-value>
    </context-param>
   ```
3. by setting the default config directly in the web.xml (**default config!**)  
   ```
    <context-param>
        <param-name>jmx_collector_config</param-name>
        <param-value>
            ---
            # Default config embedded in web.xml
            rules:
            - pattern: ".*"
        </param-value>
    </context-param>
   ```
This last setting is the default config.
First configuration found wins. So you can always override via system property.

## Metrics
| Metric names | Type | Descriptions |
|--------|------|--------------|
| prometheus_webapp_build_info{version, buildTime, buildScmVersion, buildScmBranch} | Info | Build info of this WAR |
| jvm_* | | Default JVM metrics, see [Prometheus client hotpot](https://prometheus.github.io/client_java/io/prometheus/client/hotspot/package-summary.html) |
| jmx_* | | Default JMX Collector metrics, see [Prometheus jmx_exporter](https://github.com/prometheus/jmx_exporter) |

## Building

```
mvn clean install
```

### A simple test run
```
mvn jetty:run
open http://localhost:8080/
```

## Requirements
For building:
* JDK 8
* [Maven 3.6.x](http://maven.apache.org)
