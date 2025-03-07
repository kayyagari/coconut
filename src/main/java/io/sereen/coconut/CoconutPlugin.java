package io.sereen.coconut;

import com.mirth.connect.model.ExtensionPermission;
import com.mirth.connect.plugins.ServicePlugin;
import com.mirth.connect.server.controllers.ConfigurationController;
import com.mirth.connect.server.util.GlobalVariableStore;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.Properties;

/**
 * Kiran A (kiran@sereen.io)
 */
public class CoconutPlugin implements ServicePlugin {

    private static final Logger LOG = Logger.getLogger(CoconutPlugin.class);

    @Override
    public void init(Properties properties) {
    }

    @Override
    public void update(Properties properties) {
    }

    @Override
    public Properties getDefaultProperties() {
        return new Properties();
    }

    @Override
    public ExtensionPermission[] getExtensionPermissions() {
        return new ExtensionPermission[0];
    }

    @Override
    public String getPluginPointName() {
        return "Coconut";
    }

    @Override
    public void start() {
        LOG.info("initializing Coconut plugin");
        File appDataDir = new File(ConfigurationController.getInstance().getApplicationDataDir());
        File coconutDataDir = new File(appDataDir, "coconut");
        CoconutUtil.createDir(coconutDataDir);
        Coconut.init(coconutDataDir);
        GlobalVariableStore.getInstance().put("$cn", Coconut.getInstance());
    }

    @Override
    public void stop() {
        LOG.info("stopping Coconut plugin");
        try {
            Coconut cn = Coconut.getInstance();
            cn.close();
        }
        catch (Exception e) {
            LOG.warn("", e);
        }
    }
}
