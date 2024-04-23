package me.plohn.projecttrade;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import io.github.johnnypixelz.utilizer.config.Messages;
import io.github.johnnypixelz.utilizer.plugin.Logs;
import org.bukkit.entity.Player;

import java.util.Optional;

@CommandAlias("trade")
public class TradeCommand extends BaseCommand {
    @Default
    private void onDefault(Player sender, OnlinePlayer player) {
        //is participant trade with himself?
        if (player.getPlayer() == sender){
            Messages.cfg("messages", "trade.error-self")
                    .send(sender);
            return;
        }
        //has sender Request?
        if (TradeRequestManager.sendRequest(sender, player.getPlayer())) {
            Logs.info("[DEBUG] Sent request "+sender.getName());
            //Send request
            Messages.cfg("messages", "trade.request-send")
                    .map("%player%", player.getPlayer().getName())
                    .send(sender);
            Messages.cfg("messages", "trade.request-receive")
                    .map("%player%", sender.getName())
                    .send(player.getPlayer());
        } else {
            //is sender Receiver?
            Optional<TradeRequest> tradeRequest = TradeRequestManager.getRequest(player.getPlayer(),sender);
            if (tradeRequest.isPresent()) {
                //Accept request
                Logs.info("[DEBUG] Accepted request "+sender.getName());
                Messages.cfg("messages", "trade.accepted-received")
                        .map("%player%", player.getPlayer().getName())
                        .send(sender);
                Messages.cfg("messages", "trade.accepted-sent")
                        .map("%player%", sender.getName())
                        .send(player.getPlayer());

                TradeData data = new TradeData(tradeRequest.get());
                TradeMenuProvider.open(sender,tradeRequest.get(),data);
                TradeMenuProvider.open(player.getPlayer(),tradeRequest.get(),data);
            }
            //Already Sent request
            else{
                Messages.cfg("messages", "trade.already-sent")
                        .map("%player%", player.getPlayer().getName())
                        .send(sender.getPlayer());
            }
        }
    }
}

