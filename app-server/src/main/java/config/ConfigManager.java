package config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import utilities.FileOptions;
import utilities.QuartzProcess;

import javax.management.*;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

import static java.nio.file.StandardWatchEventKinds.*;

public class ConfigManager {
    public static void main(String[] args) throws InterruptedException, IOException {
//        Parameters params = new Parameters();
//        ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration> builder =
//                new ReloadingFileBasedConfigurationBuilder<PropertiesConfiguration>(PropertiesConfiguration.class)
//                        .configure(params.fileBased()
//                                .setFile(new File("/Users/jarndt/Desktop/test")));
//        PeriodicReloadingTrigger trigger = new PeriodicReloadingTrigger(builder.getReloadingController(),
//                null, 1, TimeUnit.SECONDS);
//        trigger.start();
//
//        builder.addEventListener(ConfigurationBuilderEvent.ANY, (event)->{
//                System.out.println("Event:" + event);
//        });
//
//        while (true) {
//            Thread.sleep(1000);
//            System.out.println(builder.getConfiguration().getString("metrics.file"));
//        }

        System.out.println(ConfigManager.getConfig("elasticsearch.nodename"));
    }

    public static final String  STREAM_FILE = "stream:",
            MBEAN_PATH  = "config:type=ConfigManager";

    public static final List<String> EXTRACT_RESOURCES =
            Arrays.asList("config.properties");


    private FileSystem fileSystem;
    private WatchService watchService;
    private Set<String> configFiles = new HashSet<>(), metricFiles = new HashSet<>();
    private HashMap<String,String> configs = new HashMap<>();

    private ConfigManager() throws IOException {
        fileSystem = FileSystems.getDefault();
        watchService = fileSystem.newWatchService();
        FileOptions.runConcurrentProcessNonBlocking(()->{
            while (true) {
                final WatchKey wk = watchService.take();
                for (WatchEvent<?> event : wk.pollEvents()) {
                    final Path changed = (Path) event.context();
                    List<String> files = new ArrayList<>(configFiles);
                    files.addAll(metricFiles);
                    for (String file : files)
                        if (file.endsWith(changed.getFileName().toString())) {
                            if (event.kind().equals(ENTRY_CREATE) || event.kind().equals(ENTRY_MODIFY)) {
                                if (file.endsWith(".properties"))
                                    _setConfigs(file);
                            }else if(event.kind().equals(ENTRY_DELETE)){
                                if(configFiles.contains(file))
                                    configFiles.remove(file);
                                if(metricFiles.contains(file))
                                    metricFiles.remove(file);
                                configs = new HashMap<>();
                                configFiles.forEach(a -> {
                                    try {
                                        _setConfigs(a);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                });
                                QuartzProcess.getScheduler().clear();
                            }
                        }
                }
                // reset the key
                boolean valid = wk.reset();
            }
        });

        try {
            setDefaultConfigs();

            ManagementFactory.getPlatformMBeanServer().registerMBean(new Config(),new ObjectName(MBEAN_PATH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setDefaultConfigs() throws IOException, URISyntaxException {
        File f = new File(FileOptions.DEFAULT_DIR+"resources");
        f.mkdirs();
        for(String s : EXTRACT_RESOURCES) {
            s = FileOptions.extractJarResource(f.getAbsolutePath(), s);
            if(s != null && s.endsWith(".properties"))
                _addConfigFile(s);
        }

        //for resources
//        for(String s : Arrays.asList("config.properties")) {
//            Properties properties = new Properties();
//            properties.load(getClass().getClassLoader().getResourceAsStream(s));
//            for(String ss : properties.stringPropertyNames())
//                updateCache(ss,properties.getProperty(ss));
//        }
    }

    private void _setConfigs(String file) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(file));
        for(String s : properties.stringPropertyNames())
            updateCache(s,properties.getProperty(s));
    }

    public void _setConfigValue(String config, String configValue) throws IOException {
        boolean found = false;
        if(configFiles.size() == 0){
            updateCache(config,configValue);
            return;
        }
        for(String s : configFiles) {
            Properties p = new Properties();
            p.load(new FileInputStream(s));
            if (p.stringPropertyNames().contains(config)) {
                found = true;
                p.setProperty(config,configValue);
                p.store(new FileOutputStream(s),null);
                updateCache(config,configValue);
            }
        }
        if(!found){
            Properties p = new Properties();
            ArrayList<String> cons = new ArrayList<>(configFiles);
            p.load(new FileInputStream(cons.get(0)));
            p.setProperty(config,configValue);
            p.store(new FileOutputStream(cons.get(0)), "Add config: "+config+"="+configValue);
            updateCache(config,configValue);
        }
    }

    private void updateCache(String config, String configValue) throws IOException {
        if(config.startsWith("config.file"))
            _addConfigFile(configValue);
        configs.put(config,configValue);
    }

    public HashMap<String, String> _getConfigs() {
        return configs;
    }
    public List<String> _getConfigFiles(){
        return Collections.unmodifiableList(new ArrayList<>(configFiles));
    }
    public boolean _addConfigFile(String s) throws IOException {
        File f = new File(s);
        if(f.exists()) {
            configFiles.add(s);
            if (f.isFile())
                _setConfigs(f.getAbsolutePath());
        }
        s = new File(f.getAbsolutePath()).getParent();
        if(s == null || watchDirs.contains(s))
            return false;
        watchDirs.add(s);
        fileSystem.getPath(s).register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
        return true;
    }
    private Set<String> watchDirs = new HashSet<>();
    public void _addConfigFiles(List<String> configFiles) throws IOException {
        this.configFiles.addAll(configFiles);
        for (String s : configFiles)
            _addConfigFile(s);
    }

    ////////////// STATIC METHODS ///////////////////
    private static ConfigManager instance;

    public static ConfigManager getInstance() throws IOException {
        if(instance == null)
            instance = new ConfigManager();
        return instance;
    }

    public static List<String> getConfigFiles() throws IOException {
        return getInstance()._getConfigFiles();
    }

    public static HashMap<String, String> getConfigs() throws IOException {
        return getInstance()._getConfigs();
    }

    public static void setConfigFiles(String[] configs) throws IOException {
        getInstance()._addConfigFiles(Arrays.asList(configs));
    }

    public static void setConfigValue(String config, String configValue) throws IOException{
        getInstance()._setConfigValue(config,configValue);
    }

    public static String getConfig(String config) throws IOException {
        if(getConfigs().containsKey(config))
            return getConfigs().get(config);
        return null;
    }

    /////////////////////////// MBEANS //////////////////////////////////////
    public class Config implements ConfigMBean{
        @Override
        public String getConfigValue(String config) throws IOException {
            return ConfigManager.getConfigs().get(config);
        }

        @Override
        public void setConfigValue(String config, String configValue) throws IOException {
            ConfigManager.setConfigValue(config,configValue);
        }

        @Override
        public List<String> getAllConfigs() throws IOException {
            return new ArrayList<>(ConfigManager.getConfigs().keySet());
        }
    }

    public interface ConfigMBean{
        public String getConfigValue(String config) throws IOException;
        public void setConfigValue(String config, String configValue) throws IOException;
        public List<String> getAllConfigs() throws IOException;
    }
}