package auto_miner.strategy;

import auto_miner.action.TurnAction;
import auto_miner.game.Economy;
import auto_miner.item.InventoryItem;

import java.awt.*;
import java.util.Random;

public class WorstStrategyEver implements MinePlayerStrategy {
    @Override
    public void initialize(int boardSize, int maxInventorySize, int maxCharge, int winningScore, PlayerBoardView startingBoard, Point startTileLocation, boolean isRedPlayer, Random random) {

    }

    @Override
    public TurnAction getTurnAction(PlayerBoardView boardView, Economy economy, int currentCharge, boolean isRedTurn) {
        return null;
    }

    @Override
    public void onReceiveItem(InventoryItem itemReceived) {

    }

    @Override
    public void onSoldInventory(int totalSellPrice) {

    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void endRound(int pointsScored, int opponentPointsScored) {

    }
}
