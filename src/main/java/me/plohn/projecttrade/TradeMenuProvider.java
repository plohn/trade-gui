package me.plohn.projecttrade;

import io.github.johnnypixelz.utilizer.Scheduler;
import io.github.johnnypixelz.utilizer.event.StatelessEventEmitter;
import io.github.johnnypixelz.utilizer.itemstack.Items;
import io.github.johnnypixelz.utilizer.itemstack.Skulls;
import io.github.johnnypixelz.utilizer.plugin.Logs;
import io.github.johnnypixelz.utilizer.shade.smartinvs.ClickableItem;
import io.github.johnnypixelz.utilizer.shade.smartinvs.InventoryListener;
import io.github.johnnypixelz.utilizer.shade.smartinvs.SmartInventory;
import io.github.johnnypixelz.utilizer.shade.smartinvs.content.InventoryContents;
import io.github.johnnypixelz.utilizer.shade.smartinvs.content.InventoryProvider;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class TradeMenuProvider implements InventoryProvider {
    public static ClickableItem menuDivision = ClickableItem.empty(Items.create(Material.BLACK_STAINED_GLASS_PANE, ""));
    public static ClickableItem border = ClickableItem.empty(Items.create(Material.GRAY_STAINED_GLASS_PANE, " "));

    public static void open(Player player, TradeRequest request, TradeData data) {
        Player participant;
        if (request.getSender() == player) participant = request.getReceiver();
        else participant = request.getSender();

        TradeMenuProvider tradeMenuProvider = new TradeMenuProvider(player, request, data);
        InventoryListener<InventoryClickEvent> inventoryClickEventInventoryListener = new InventoryListener<>(InventoryClickEvent.class, event -> {
            Inventory bottomInventory = event.getView().getBottomInventory();
            Inventory clickedInventory = event.getClickedInventory();

            if (clickedInventory == bottomInventory) {
                int slot = event.getSlot();
                ItemStack item = clickedInventory.getItem(slot);
                tradeMenuProvider.tradeData.addItem(player, item);
                Logs.info("Item: " + item);
            }

        });
        //Cancel Trade
        InventoryListener<InventoryCloseEvent> playerCloseInventory = new InventoryListener<>(InventoryCloseEvent.class, event -> {
            tradeMenuProvider.tradeData
                    .getOfferedItems(tradeMenuProvider.sender)
                    .forEach(itemStack -> tradeMenuProvider
                            .sender.getInventory()
                            .addItem(itemStack));

            TradeRequestManager.removeRequest(tradeMenuProvider.tradeRequest);
        });

        SmartInventory.builder()
                .title("Trading with: " + participant.getName())
                .size(6, 9)
                .provider(tradeMenuProvider)
                .listener(inventoryClickEventInventoryListener)
                .listener(playerCloseInventory)
                .build()
                .open(player);
    }

    private List<ItemStack> playerItemsOffer = new ArrayList<>();
    private List<ItemStack> receiverItemsOffer = new ArrayList<>();
    private TradeData tradeData;
    private StatelessEventEmitter emitter;
    private InventoryContents inventoryContents;

    private Player sender;
    private Player receiver;
    private ItemStack senderSkull;
    private ItemStack participantSkull;

    private ClickableItem actionToggleStatus;
    private ClickableItem receiverStatus;

    private TradeRequest tradeRequest;
    private int tradeDelayStatus;
    private BukkitTask countdown;

    private TradeMenuProvider(Player sender, TradeRequest request, TradeData data) {
        this.sender = sender;
        this.tradeRequest = request;

        if (request.getSender() == sender) this.receiver = request.getReceiver();
        else this.receiver = request.getSender();

        this.tradeData = data;
        this.emitter = tradeData.getEmitter();

        this.senderSkull = Skulls.getSkullFromUUID(sender.getUniqueId());
        this.participantSkull = Skulls.getSkullFromUUID(receiver.getUniqueId());

        ItemStack itemReceiverStatus = Items.create(Material.BOOK,"&6Not ready");
        this.receiverStatus = ClickableItem.empty(itemReceiverStatus);

        ItemStack itemToggleStatus = Items.create(Material.WRITABLE_BOOK, "&a&lConfirm",
                "&7Click to confirm your offer"
        );
        this.actionToggleStatus = ClickableItem.of(itemToggleStatus, event -> {
            if (event.getClick() != ClickType.LEFT) return;
            tradeData.setStatusReady((Player) event.getWhoClicked());
        });

        emitter.listen(() -> {
            if (tradeData.isPlayerReady(receiver)) {
                Items.edit(itemReceiverStatus).setType(Material.EMERALD);
                Items.edit(itemReceiverStatus).setDisplayName("&a&lReady");
            }
            else {
                Items.edit(itemReceiverStatus).setType(Material.BOOK);
                Items.edit(itemReceiverStatus).setDisplayName("&6Not ready");
            }
            this.receiverStatus = ClickableItem.empty(itemReceiverStatus);

            if (tradeData.isPlayerReady(sender)){
                Items.edit(itemToggleStatus).setType(Material.FEATHER);
                Items.edit(itemToggleStatus).setDisplayName("&cCancel Offer");
                Items.edit(itemToggleStatus).setLore("&7Click to cancel your offer");
            }
            else{
                Items.edit(itemToggleStatus).setType(Material.WRITABLE_BOOK);
                Items.edit(itemToggleStatus).setDisplayName("&aConfirm Offer");
                Items.edit(itemToggleStatus).setLore("&7Click to confirm your offer");
            }
            this.actionToggleStatus = ClickableItem.of(itemToggleStatus, event -> {
                if (event.getClick() != ClickType.LEFT) return;
                tradeData.setStatusReady((Player) event.getWhoClicked());
            });

            this.playerItemsOffer = tradeData.getOfferedItems(sender);
            this.receiverItemsOffer = tradeData.getOfferedItems(receiver);

            Logs.info(sender.getName() + "'s GUI his: " + playerItemsOffer.toString());
            Logs.info(sender.getName() + "'s GUI other: " + receiverItemsOffer.toString());

            if (inventoryContents == null) return;
            init(sender, inventoryContents);
        });
    }

    @Override
    public void init(Player player, InventoryContents contents) {
        if (tradeData.isTradeCompleted()) player.closeInventory();

        inventoryContents = contents;
        contents.fill(null);

        contents.fillRow(5, border); //Bottom border
        contents.fillRow(0, border); //Top border

        contents.set(5, 1, ClickableItem.empty(senderSkull)); //Trader self
        contents.set(5, 7, ClickableItem.empty(participantSkull)); //Trader participant
        contents.set(5, 6, this.receiverStatus); //Status of other participant

        //When both parties agreed to each other's offer
        if (tradeData.isPlayerReady(sender) && tradeData.isPlayerReady(receiver)) {
            ItemStack itemCountdownStarted = Items.create(Material.YELLOW_STAINED_GLASS_PANE,"&e ");
            contents.fillColumn(4, ClickableItem.empty(itemCountdownStarted)); //Menu division

            for (int slot = 0; slot < receiverItemsOffer.size(); slot++) {
                ItemStack item = receiverItemsOffer.get(slot);
                contents.set(slot / 4 + 1, (slot % 4) + 5, ClickableItem.empty(item));
            }

            for (int slot = 0; slot < playerItemsOffer.size(); slot++) {
                ItemStack item = playerItemsOffer.get(slot);
                contents.set(slot / 4 + 1, slot % 4, ClickableItem.empty(item));
            }

            this.tradeDelayStatus = 5;
            ItemStack status = Items.create(Material.CLOCK, Integer.toString(tradeDelayStatus));
            this.countdown = Scheduler.syncTimed(() -> {
                Items.edit(status).setDisplayName(Integer.toString(tradeDelayStatus));
                Items.edit(status).setAmount(tradeDelayStatus);

                contents.set(5, 2, ClickableItem.of(status, event -> {
                    tradeData.setStatusNotReady(player);
                }));

                if (tradeDelayStatus-- == 0) {
                    tradeData.finalizeTrade();
                }
            }, 20, 6);
        } else {
            if (this.countdown != null) this.countdown.cancel();
            contents.fillColumn(4, menuDivision); //Menu division

            for (int slot = 0; slot < receiverItemsOffer.size(); slot++) {
                ItemStack item = receiverItemsOffer.get(slot);
                contents.set(slot / 4 + 1, (slot % 4) + 5, ClickableItem.empty(item));
            }

            for (int slot = 0; slot < playerItemsOffer.size(); slot++) {
                ItemStack item = playerItemsOffer.get(slot);
                contents.set(slot / 4 + 1, slot % 4, ClickableItem.of(item, event -> {
                            if (event.getClick() != ClickType.LEFT) return;
                            tradeData.removeItem(player, item);
                            tradeData.setStatusNotReady(player);
                        })
                );
            }

            contents.set(5, 2, this.actionToggleStatus);
        }
    }
    public TradeData getTradeData() {
        return tradeData;
    }
}
