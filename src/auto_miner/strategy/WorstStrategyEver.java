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
    private boolean isRedPlayer;
    private int boardSize; // 26 * 26 = 676
    private HashMap<Point, TileType> tileFilterLocations;
    private List<Point> myMarket = new LinkedList<>();
    private List<Point> rechargeStation = new LinkedList<>();

    @Override
    public void initialize(int boardSize, int maxInventorySize, int maxCharge, int winningScore, PlayerBoardView startingBoard, Point startTileLocation, boolean isRedPlayer, Random random) {
        this.maxCharge = maxCharge;
        this.boardSize = boardSize;
        this.isRedPlayer = isRedPlayer;
        setBoardPoints(startingBoard);
    }

    private Point getClosestMinePoint(PlayerBoardView boardView) {
        Stack<Point> sortList = new Stack<>();
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                if (boardView.getTileTypeAtLocation(x, y).toString().startsWith("RESOURCE")) {
                    sortList.add(new Point(x, y)); // ordenado de aqui
                }
            }
        }

        sortList = sortList.stream()
                //                .sorted(Comparator.comparingInt(p -> p.getKey().x))
                .sorted(Comparator.comparingDouble(p -> p.distance(boardView.getYourLocation())))
                .collect(Collectors.toCollection(Stack::new));
        System.out.println(sortList);

        return sortList.firstElement();
    }


    @Override
    public TurnAction getTurnAction(PlayerBoardView boardView, Economy economy, int currentCharge, boolean isRedTurn) {
        // TODO start looking by the closest point
        System.out.println(getClosestMinePoint(boardView));
        Point nextPoint = getClosestMinePoint(boardView);


        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        Point closestMine = getClosestMine(listMinePoints);
        return walk(nextPoint, boardView);
    }

    private TurnAction walk(Point nextPoint, PlayerBoardView boardView) {
        //   7,19               6,19
        if (nextPoint.getX() > boardView.getYourLocation().getX()) {
            System.out.println("next mayor");

            return TurnAction.MOVE_RIGHT;
        }
        if (nextPoint.getX() == boardView.getYourLocation().getX()) {
            //6,18                  6,19
            if (nextPoint.getY() < boardView.getYourLocation().getY())
                return TurnAction.MOVE_DOWN;
        }
        if (nextPoint.getX() < boardView.getYourLocation().getX()) {
            System.out.println("next menor");
            return TurnAction.MOVE_LEFT;
        }


        return null;
    }


    public HashMap<Point, TileType> getAllAvailableResources(PlayerBoardView boardView) {
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

    private void setBoardPoints(PlayerBoardView boardView) {
        TileType tileType;
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                tileType = boardView.getTileTypeAtLocation(x, y);
                if (tileType.equals(TileType.RECHARGE)) {
                    rechargeStation.add(new Point(x, y));
                }
                if (tileType.equals(isRedPlayer ? TileType.RED_MARKET : TileType.BLUE_MARKET)) {
                    myMarket.add(new Point(x, y));
                }
            }
        }
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