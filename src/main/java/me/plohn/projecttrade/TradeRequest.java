package me.plohn.projecttrade;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TradeRequest {
    private static final List<TradeRequest> requests = new ArrayList<>();

    int id;
    Player sender;
    Player receiver;
    public TradeRequest(Player sender,Player receiver){
        this.sender = sender;
        this.receiver = receiver;
    }
    public Player getReceiver() {
        return receiver;
    }

    public Player getSender() {
        return sender;
    }
}
