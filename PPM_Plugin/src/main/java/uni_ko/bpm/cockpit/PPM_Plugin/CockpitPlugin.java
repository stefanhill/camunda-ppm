package uni_ko.bpm.cockpit.PPM_Plugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import uni_ko.bpm.cockpit.PPM_Plugin.resources.CockpitPluginRootResource;

import org.camunda.bpm.cockpit.plugin.spi.impl.AbstractCockpitPlugin;

public class CockpitPlugin extends AbstractCockpitPlugin {

    public static final String ID = "cockpit-plugin";
    // get absolute path to current directory and navigate to Plugin resources
    public static final String resourcePath = CockpitPlugin.isEclipse() ?
            System.getProperty("user.dir") + "/../PPM_Plugin/src/main/resources" :
            System.getProperty("user.dir") + "/PPM_Plugin/src/main/resources";
    // standardize date format for prediction models
    public static final DateFormat dateFormat = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);

    public String getId() {
        return ID;
    }

    @Override
    public Set<Class<?>> getResourceClasses() {
        Set<Class<?>> classes = new HashSet<>();


        classes.add(CockpitPluginRootResource.class);

        return classes;
    }

    @Override
    public List<String> getMappingFiles() {
        return Arrays.asList("uni_ko.bpm.cockpit.PPM_Plugin".replace(".", "/") + "/cockpit-plugin-query.xml");
    }

    public static boolean isEclipse() {
        boolean isEclipse = true;
        if (System.getenv("eclipse") == null) {
            isEclipse = false;
        }
        return isEclipse;
    }

}
