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
    private HashMap tileFilterLocations;
    private List<Point> myMarket;
    private List<Point> rechargeStation;
    private HashMap getFilterPoints4Mine;
    private HashMap listMinePoints;

    @Override
    public void initialize(int boardSize, int maxInventorySize, int maxCharge, int winningScore, PlayerBoardView startingBoard, Point startTileLocation, boolean isRedPlayer, Random random) {
        this.maxCharge = maxCharge;
        this.boardSize = boardSize;
        tileFilterLocations = getAllAvailableResources(startingBoard); // This is going to be changing every turn
        rechargeStation = getLocationPoints(tileFilterLocations, TileType.RECHARGE);
        myMarket = getLocationPoints(tileFilterLocations, isRedPlayer ? TileType.RED_MARKET : TileType.BLUE_MARKET);
        listMinePoints = getJustMinePoints(tileFilterLocations);
    }

    private List getLocationPoints(HashMap<Point, TileType> tileFilterLocations, TileType tileType) {
        return tileFilterLocations.entrySet().stream().filter(resource -> resource.getValue().equals(tileType)).collect(Collectors.toList());
    }

    private HashMap getJustMinePoints(HashMap<Point, TileType> tileFilterLocations) {
        return tileFilterLocations.entrySet().stream()
                .filter(str -> str.getValue().toString().startsWith("RESOURCE"))
                // not sure if I can add a rule to compute the closest point(key)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (prev, next) -> next, HashMap::new));
    }

    @Override
    public TurnAction getTurnAction(PlayerBoardView boardView, Economy economy, int currentCharge, boolean isRedTurn) {
        Point myCurrentLocation = boardView.getYourLocation();
        // TODO start looking by the closest point
        System.out.println(getJustMinePoints(tileFilterLocations));
        Point closestMine = getClosestMine(getJustMinePoints(tileFilterLocations));
        return null;
    }

    private Point getClosestMine(HashMap justMinePoints) {
        return null;
    }


    public HashMap getAllAvailableResources(PlayerBoardView boardView) {
        HashMap<Point, TileType> list = new HashMap<>();

        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (!boardView.getTileTypeAtLocation(x, y).equals(TileType.EMPTY)) {
                    list.put(new Point(x, y), boardView.getTileTypeAtLocation(x, y));
                }
            }
        }
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