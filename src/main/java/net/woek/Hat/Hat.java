package net.woek.Hat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;


public class Hat extends JavaPlugin{

    //on enable
    @Override
    public void onEnable() {
        YamlConfiguration messages = validateYaml("messages.yml");
        //YamlConfiguration config = validateYaml("config.yml"); //TODO: Re-enable this if anyone wants it

        boolean enabled = messages.getBoolean("messages-enabled");
        String set = (String) messages.get("set");
        String stacksize = (String) messages.get("stack-size");
        String nopermission = (String) messages.get("no-permission");
        String console = (String) messages.get("console");
        String curse = (String) messages.get("curse");

        //TODO: Make sure this works
        registerPermissions();

        HatHandler handler = new HatHandler(this, enabled, set, stacksize, nopermission, console, curse);

        this.getCommand("hat").setExecutor(handler);
        //if(config.getBoolean("manual_hat_equip.enabled")){
            this.getServer().getPluginManager().registerEvents(handler, this);
        //}

        Bukkit.getConsoleSender().sendMessage("[Hat] Hat has been enabled.");
    }

    //on disable
    @Override
    public void onDisable() {
        Bukkit.getConsoleSender().sendMessage("[Hat] Hat has been disabled.");
    }

    private YamlConfiguration validateYaml(String filename) {
        Bukkit.getConsoleSender().sendMessage("[" + this.getName() + "] Validating " + filename);

        //default config from jar
        InputStream defaultFile = this.getResource(filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(defaultFile));
        YamlConfiguration newconfig = YamlConfiguration.loadConfiguration(reader);

        //config from plugin's folder on server
        YamlConfiguration oldconfig = new YamlConfiguration();
        try {
            File config = new File(getDataFolder(), filename);
            if(config.exists()) {
                oldconfig.load(config);
            }else{
                Bukkit.getConsoleSender().sendMessage("[" + this.getName() + "] " + filename + " does not exist, creating it now.");
            }
        }catch(Throwable e){
            Bukkit.getConsoleSender().sendMessage("[" + this.getName() + "] " + filename + " does not contain a valid configuration, the default configuration will be used instead.");
            return newconfig;
        }

        //TODO: See if anything else needs to be closed.
        //closes the reader
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //save the value of each key in oldconfig that's also in newconfig to newconfig
        for(String key : oldconfig.getKeys(true)) {
            if(newconfig.contains(key) && !(oldconfig.get(key) instanceof ConfigurationSection)){
                newconfig.set(key, oldconfig.get(key));
            }
        }

        //saves new configuration file to plugin folder
        try {
            newconfig.save(new File(this.getDataFolder(), filename));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Bukkit.getConsoleSender().sendMessage("[" + this.getName() + "] " + filename + " has been validated.");

        return newconfig;
    }

    private void registerPermissions(){

        Permission basePerm = new Permission("hat.*", PermissionDefault.OP);
        Bukkit.getPluginManager().addPermission(basePerm);

        Permission blockPerm = new Permission("hat.blocks", PermissionDefault.FALSE); //Change these to OP if * permission removed
        blockPerm.addParent(basePerm, true);
        Bukkit.getPluginManager().addPermission(blockPerm);

        Permission itemPerm = new Permission("hat.items", PermissionDefault.FALSE); //^^
        itemPerm.addParent(basePerm, true);
        Bukkit.getPluginManager().addPermission(itemPerm);

        Permission cursePerm = new Permission("hat.curse", PermissionDefault.TRUE);
        cursePerm.addParent(basePerm, true);
        Bukkit.getPluginManager().addPermission(cursePerm);

        Material[] materials = Material.values();

        for(Material mat : materials){
            //TODO: rename all references of .name() to .toString() if it seems like a good idea
            Permission perm = new Permission("hat." + mat.name(), PermissionDefault.FALSE);

            //Make these children of the appropriate permission
            if(mat.isBlock()){
                perm.addParent(blockPerm, true);
            }else{
                perm.addParent(itemPerm, true);
            }

            Bukkit.getPluginManager().addPermission(perm);
        }
    }
}
