package me.plohn.projecttrade;

import co.aikar.commands.PaperCommandManager;
import io.github.johnnypixelz.utilizer.config.Configs;
import io.github.johnnypixelz.utilizer.plugin.UtilPlugin;
import org.bukkit.event.Listener;

public class ProjectTrade extends UtilPlugin implements Listener {
    @Override
    public void onEnable(){
        Configs.load("messages").watch();
        //Register Command
        PaperCommandManager commandManager = new PaperCommandManager(this);
        commandManager.registerCommand(new TradeCommand());
        registerListener(this);
        registerListener(new PlayerInventoryListener());
    }
}
