package levelpoints.Utils;

import com.sk89q.worldguard.bukkit.RegionContainer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import levelpoints.Cache.ExternalCache;
import levelpoints.Cache.FileCache;
import levelpoints.Containers.LevelsContainer;
import levelpoints.commands.lpsCommand;
import levelpoints.events.ChatEvent;
import levelpoints.events.CustomEvents.EarnExpEvent;
import levelpoints.events.MoveEvent;
import levelpoints.events.PlayerEvents;
import levelpoints.events.WildStackerEvent;
import levelpoints.levelpoints.Formatting;
import levelpoints.levelpoints.LevelPoints;


import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class UtilCollector {

    private static HashMap<UUID, YamlConfiguration> usersConfig = new HashMap<>();
    private static HashSet<ProtectedRegion> regionsInCache = new HashSet<>();


    public static void RunModuels() {
        Plugin plugin = LevelPoints.getInstance();
        LevelPoints lps = LevelPoints.getPlugin(LevelPoints.class);


        usersConfig.clear();
        plugin.saveDefaultConfig();
        plugin.getServer().getPluginManager().registerEvents(new PlayerEvents(plugin), plugin);
        if (ExternalCache.isRunningWildStacker()) {
            plugin.getServer().getPluginManager().registerEvents(new WildStackerEvent(plugin), plugin);
        }
        if (ExternalCache.isRunningWorldGuard()) {
            System.out.println(Formatting.basicColor("&3Loading Method>>> &bWorldGuard"));
            plugin.getServer().getPluginManager().registerEvents(new MoveEvent(plugin), plugin);
        }
        if (ExternalCache.isRunningChatFormat()) {
            AsyncFileCache.createFormatsFile();

            LevelsContainer.generateFormats();
            plugin.getServer().getPluginManager().registerEvents(new ChatEvent(plugin), plugin);

            System.out.println(Formatting.basicColor("&3Loaded Method>> &bLPSFormat"));

        }


        plugin.getServer().getConsoleSender().sendMessage(Formatting.basicColor("&3Loaded Modules>>> &bEvents"));


        lps.getCommand("lps").setExecutor((CommandExecutor) new lpsCommand(plugin));
        plugin.getServer().getConsoleSender().sendMessage(Formatting.basicColor("&3Loaded Modules>>> &bCommands"));

        plugin.getServer().getConsoleSender().sendMessage(Formatting.basicColor("&3Running Files Check...."));
        AsyncFileCache.startAsyncCreate();
        AsyncEvents.startVersionCheck();
        AsyncEvents.giveTimedEXP();
        new BukkitRunnable() {
            @Override
            public void run() {
                if (FileCache.getConfig("levelConfig").getBoolean("LevelBonus.Enabled")) {
                    LevelsContainer.generateLevelBonus();
                }
                if (FileCache.getConfig("levelConfig").getBoolean("Leveling.CustomLeveling.Enabled")) {
                    LevelsContainer.generateCustomLevels();
                }


            }
        }.runTaskLaterAsynchronously(LevelPoints.getInstance(), 3 * 10);
        if (ExternalCache.isRunningWorldGuard()) {
            FileConfiguration config = AsyncFileCache.runConfig("/Settings/EXP.yml");
            if (config.getBoolean("Anti-Abuse.WorldGuard.LevelRegions.Enabled")) {
                plugin.getServer().getPluginManager().registerEvents(new MoveEvent(plugin), plugin);
            }
            System.out.println(Formatting.basicColor("&3Loading Method>>> &bWorldGuard"));
        }
    }
}
