package me.plohn.projecttrade;

import io.github.johnnypixelz.utilizer.event.StatelessEventEmitter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class TradeData {
    private final StatelessEventEmitter emitter = new StatelessEventEmitter();
    private final List<ItemStack> player1OfferedItems = new ArrayList<>();
    private final List<ItemStack> player2OfferedItems = new ArrayList<>();
    private final Player sender;
    private final Player receiver;
    private boolean senderStatus;
    private boolean receiverStatus;
    private boolean tradeCompleted;

    private Timer timer;

    public TradeData(TradeRequest request) {
        this.sender = request.getSender();
        this.receiver = request.getReceiver();

        this.senderStatus = false;
        this.receiverStatus = false;

        this.tradeCompleted = false;
    }

    public void setStatusReady(Player player) {
        if (player.equals(sender)) senderStatus = true;
        else receiverStatus = true;
        emitter.emit();
    }

    public void setStatusNotReady(Player player){
        senderStatus = false;
        receiverStatus = false;
        emitter.emit();
    }

    public boolean isPlayerReady(Player player) {
        if (player.equals(sender)) return senderStatus;
        else return receiverStatus;
    }

    public void finalizeTrade() {
        player1OfferedItems.forEach(itemStack ->
                receiver.getInventory().addItem(itemStack));
        player2OfferedItems.forEach(itemStack ->
                sender.getInventory().addItem(itemStack));

        player1OfferedItems.clear();
        player2OfferedItems.clear();

        senderStatus = false;
        receiverStatus = false;
        tradeCompleted = true;

        emitter.emit();
    }

    public void addItem(Player player, ItemStack item) {
        if (Objects.equals(player, sender)) player1OfferedItems.add(item);
        else player2OfferedItems.add(item);
        emitter.emit();
    }

    public void removeItem(Player player, ItemStack item) {
        if (Objects.equals(player, sender)) {
            player1OfferedItems.remove(item);
        } else {
            player2OfferedItems.remove(item);
        }
        player.getInventory().addItem(item);
        emitter.emit();
    }

    public List<ItemStack> getOfferedItems(Player player) {
        if (Objects.equals(player, sender)) return player1OfferedItems;
        else return player2OfferedItems;
    }

    public boolean isTradeCompleted() {
        return tradeCompleted;
    }
    public StatelessEventEmitter getEmitter() {
        return emitter;
    }
}
