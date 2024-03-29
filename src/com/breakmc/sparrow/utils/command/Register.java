package com.breakmc.sparrow.utils.command;

import com.breakmc.sparrow.Sparrow;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class Register {

    private SimpleCommandMap commandMap;

    public Register() {
        super();

        try {
            this.commandMap = (SimpleCommandMap) Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap", (Class<?>[]) new Class[0]).invoke(Bukkit.getServer());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public BaseCommand constructFromAnnotation(final IBaseCommand base) {
        try {
            Method execute = base.getClass().getMethod("execute", CommandSender.class, String[].class);
            if (execute.isAnnotationPresent(BaseCommandAnn.class)) {
                BaseCommandAnn commandAnn = execute.getAnnotation(BaseCommandAnn.class);

                BaseCommand command = new BaseCommand(commandAnn.name(), (commandAnn.permission() == null ? null : commandAnn.permission()), commandAnn.commandUsage(), commandAnn.aliases()) {
                    @Override
                    public void execute(CommandSender sender, String[] args) {
                        base.execute(sender, args);
                    }
                };

                command.setMaxArgs(commandAnn.maxArgs());
                command.setMinArgs(commandAnn.minArgs());
                command.setUsage(commandAnn.usage());

                return command;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return null;
    }

    public void loadCommandsFromPackage(final String packageName) {
        for (final Class<?> clazz : this.getClassesInPackage(packageName)) {
            System.out.println(String.valueOf(clazz.getName()) + "\n\n");
            if (BaseCommand.class.isAssignableFrom(clazz)) {
                try {
                    final BaseCommand executor = (BaseCommand) clazz.newInstance();
                    this.registerCommand(executor.getName(), executor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private ArrayList<Class<?>> getClassesInPackage(final String pkgname) {
        final ArrayList<Class<?>> classes = new ArrayList<>();
        final CodeSource codeSource = Sparrow.getInstance().getClass().getProtectionDomain().getCodeSource();
        final URL resource = codeSource.getLocation();
        final String relPath = pkgname.replace('.', '/');
        final String resPath = resource.getPath().replace("%20", " ");
        final String jarPath = resPath.replaceFirst("[.]jar[!].*", ".jar").replaceFirst("file:", "");
        JarFile jFile;
        try {
            jFile = new JarFile(jarPath);
        } catch (IOException e) {
            throw new RuntimeException("Unexpected IOException reading JAR File '" + jarPath + "'", e);
        }
        final Enumeration<JarEntry> entries = jFile.entries();
        while (entries.hasMoreElements()) {
            final JarEntry entry = entries.nextElement();
            final String entryName = entry.getName();
            String className = null;
            if (entryName.endsWith(".class") && entryName.startsWith(relPath) && entryName.length() > relPath.length() + "/".length()) {
                className = entryName.replace('/', '.').replace('\\', '.').replace(".class", "");
            }
            if (className != null) {
                Class<?> c = null;
                try {
                    c = Class.forName(className);
                } catch (ClassNotFoundException e2) {
                    e2.printStackTrace();
                }
                if (c == null) {
                    continue;
                }
                classes.add(c);
            }
        }
        try {
            jFile.close();
        } catch (IOException e3) {
            e3.printStackTrace();
        }
        return classes;
    }

    public void registerCommand(final String cmd, final BaseCommand executor) throws Exception {
        PluginCommand command = Bukkit.getServer().getPluginCommand(cmd.toLowerCase());
        if (command == null) {
            final Constructor<?> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            constructor.setAccessible(true);
            command = (PluginCommand) constructor.newInstance(cmd, Sparrow.getInstance());
        }
        command.setExecutor(executor);
        final List<String> list = Arrays.asList(executor.aliases);
        command.setAliases(list);
        if (command.getAliases() != null) {
            for (final String alias : command.getAliases()) {
                this.unregisterCommand(alias);
            }
        }

        if (executor.getPermission() != null && !executor.getPermission().isEmpty()) {
            command.setPermission(executor.getPermission());
        }

        if (executor.getUsage() != null) {
            command.setUsage(executor.getUsage());
        }


        try {
            final Field field = executor.getClass().getDeclaredField("description");
            field.setAccessible(true);
            if (field.get(executor) instanceof String) {
                command.setDescription(ChatColor.translateAlternateColorCodes('&', (String) field.get(executor)));
            }
        } catch (Exception ignored) {}

        this.commandMap.register(cmd, command);
    }

    public void unregisterCommand(final String name) {
        try {
            final Field known = SimpleCommandMap.class.getDeclaredField("knownCommands");
            final Field alias = SimpleCommandMap.class.getDeclaredField("aliases");
            known.setAccessible(true);
            alias.setAccessible(true);
            final Map<String, Command> knownCommands = (Map<String, Command>) known.get(this.commandMap);
            final Set<String> aliases = (Set<String>) alias.get(this.commandMap);
            knownCommands.remove(name.toLowerCase());
            aliases.remove(name.toLowerCase());
        } catch (Exception ignored) {}
    }
}
