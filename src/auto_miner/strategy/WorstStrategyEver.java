package auto_miner.strategy;

import auto_miner.action.TurnAction;
import auto_miner.game.Economy;
import auto_miner.item.InventoryItem;
import auto_miner.tiles.TileType;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class WorstStrategyEver implements MinePlayerStrategy {
    private int maxCharge; // This value never changes
    private int boardSize; // 26 * 26 = 676
    private HashMap<Point,TileType> tileTypePosition;
    private List myRedMarket;
    private List rechargeStation;
    @Override
    public void initialize(int boardSize, int maxInventorySize, int maxCharge, int winningScore, PlayerBoardView startingBoard, Point startTileLocation, boolean isRedPlayer, Random random) {
        this.maxCharge = maxCharge;
        this.boardSize = boardSize;
        tileTypePosition = getPositionMines(startingBoard); // This is going to change
        rechargeStation =  tileTypePosition.entrySet().stream().filter(recharge -> recharge.getValue().equals(TileType.RECHARGE)).collect(Collectors.toList());
        myRedMarket = tileTypePosition.entrySet().stream().filter(market -> market.getValue().equals(TileType.RED_MARKET)).collect(Collectors.toList());
    }

    @Override
    public TurnAction getTurnAction(PlayerBoardView boardView, Economy economy, int currentCharge, boolean isRedTurn) {
        tileTypePosition = getPositionMines(boardView);
        if (maxCharge == currentCharge) {
//            System.out.println("1) [x: " + boardView.getYourLocation().x + ", y: " + boardView.getYourLocation().y + "]");

            getPositionMines(boardView);
            return TurnAction.MINE;
        }

//        Stream.of(boardView.getItemsOnGround().keySet().toArray()).forEach(System.out::println);
//        System.out.println("isEmpty: " + boardView.getItemsOnGround().isEmpty());
//        System.out.println("[x: " + boardView.getYourLocation().x + ", y: " + boardView.getYourLocation().y + "]");
        Map map = boardView.getItemsOnGround();
        if (!map.isEmpty()) {
            boardView.getItemsOnGround().keySet().stream().forEach(System.out::println);
        }


        if (boardView.getYourLocation().x == 25) {
            return TurnAction.MOVE_UP;
        }

        if (boardView.getYourLocation().y == 25) {
            return TurnAction.MOVE_DOWN;
        }
        return null;
    }


    public HashMap getPositionMines(PlayerBoardView boardView) {
        HashMap<Point, TileType> list = new HashMap<>();

        for (int x = 0; x < 26; x++) {
            for (int y = 0; y < 26; y++) {
                if (!boardView.getTileTypeAtLocation(x, y).equals(TileType.EMPTY)) {
                    list.put(new Point(x, y), boardView.getTileTypeAtLocation(x, y));
                }
            }
        }

        list.entrySet().stream().sorted(Map.Entry.comparingByValue()).forEach(System.out::println);
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@2222");


        System.out.println(rechargeStation +  "" + myRedMarket);
        try {
            Thread.sleep(40000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(1);
        return list;
    }


    @Override
    public void onReceiveItem(InventoryItem itemReceived) {

    }

    @Override
    public void onSoldInventory(int totalSellPrice) {

    }

    @Override
    public String getName() {
        return "WorstStrategyEver";
    }

    @Override
    public void endRound(int pointsScored, int opponentPointsScored) {

    }
}