package com.olziedev.spotbot.utils;

import de.leonhard.storage.SimplixBuilder;
import de.leonhard.storage.Yaml;
import de.leonhard.storage.internal.settings.ConfigSettings;
import de.leonhard.storage.internal.settings.DataType;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class Configuration {

    private static Yaml config;
    private static Yaml lang;
    private static Yaml commands;
    private static Yaml customCommands;
    private static Yaml staticData;

    public void load(File folder) {
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            load(getClass().getDeclaredField("config"), new File(folder, "config.yml"));
            load(getClass().getDeclaredField("lang"), new File(folder, "lang.yml"));
            load(getClass().getDeclaredField("commands"), new File(folder, "commands.yml"));
            load(getClass().getDeclaredField("customCommands"), new File(folder, "customcommands.yml"));
            load(getClass().getDeclaredField("staticData"), new File(folder + File.separator + "data", "staticdata.yml"));
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    private void load(Field field, File file) throws Exception {
        InputStream inputStream = this.getInputStream(file.getName());
        if (!file.exists() && inputStream != null) {
            Files.copy(inputStream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        field.setAccessible(true);
        Yaml yaml = SimplixBuilder.fromFile(file)
                .addInputStream(inputStream)
                .setConfigSettings(ConfigSettings.PRESERVE_COMMENTS)
                .setDataType(DataType.SORTED)
                .createYaml();
//        yaml.addDefaultsFromInputStream();
        field.set(null, yaml);
    }

    private InputStream getInputStream(String resource) {
        try {
            URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
            if (url == null) return null;

            return new JarFile(new File(url.toURI()).getPath()).getInputStream(new ZipEntry(resource));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Yaml getConfig() {
        return config;
    }

    public static Yaml getLang() {
        return lang;
    }

    public static Yaml getCommands() {
        return commands;
    }

    public static Yaml getCustomCommands() {
        return customCommands;
    }

    public static Yaml getStaticData() {
        return staticData;
    }
}
