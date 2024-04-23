package me.plohn.projecttrade;

import io.github.johnnypixelz.utilizer.plugin.Logs;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TradeRequestManager {
    private static final List<TradeRequest> requests = new ArrayList<>();

    public static boolean sendRequest(Player sender, Player receiver) {
        Logs.info("[DEBUG] sendRequest()");
        if (TradeRequestManager.getRequest(sender, receiver).isPresent() || TradeRequestManager.getRequest(receiver, sender).isPresent()) {
            Logs.info("[DEBUG] has request");
            return false;
        }
        requests.add(new TradeRequest(sender, receiver));
        return true;
    }
    public static Optional<TradeRequest> getRequest(Player sender, Player receiver) {
        return requests.stream()
                .filter(request -> request.getSender() == sender && request.getReceiver() == receiver)
                .findFirst();
    }
    public static void removeRequest(TradeRequest request){
        requests.remove(request);
    }


    public static boolean isReceiver(Player player,TradeRequest request){
        return request.getReceiver() == player;
    }
    public static boolean isSender(Player player,TradeRequest request){
        return request.getSender() == player;
    }
}
