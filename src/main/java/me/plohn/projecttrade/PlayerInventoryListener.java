package me.plohn.projecttrade;

import io.github.johnnypixelz.utilizer.shade.smartinvs.InventoryManager;
import io.github.johnnypixelz.utilizer.shade.smartinvs.SmartInventory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class PlayerInventoryListener implements Listener {
    @EventHandler
    public void playerInventoryClickedItem(InventoryClickEvent event) {
        Optional<SmartInventory> inventoryOptional = InventoryManager.getInventory((Player) event.getWhoClicked());
        if (inventoryOptional.isEmpty()) return;

        SmartInventory inventory = inventoryOptional.get();
        if (!(inventory.getProvider() instanceof TradeMenuProvider provider)) return;
        if (event.getView().getBottomInventory() != event.getClickedInventory()) return;
        int slot = event.getSlot();

        ItemStack item = event.getClickedInventory().getItem(slot);

        if (item == null) return;

        provider.getTradeData().addItem((Player) event.getWhoClicked(), item);
        event.getClickedInventory().clear(slot);
        event.setCancelled(true);
    }
}
