package auto_miner.strategy;
import auto_miner.action.TurnAction;
import auto_miner.game.Economy;
import auto_miner.item.InventoryItem;
import auto_miner.item.ResourceType;
import auto_miner.tiles.TileType;
import java.awt.*;
import java.util.*;
import java.util.List;

public class FastAndFuriousStrategy implements MinePlayerStrategy {
    private int boardSize;
    private int maxInventorySize;
    private int onStorage = 0;
    private Queue<TurnAction> route;
    private List<Point> recharges = new LinkedList<>();
    private List<Point> markets = new LinkedList<>();
    private Point currentPosition;
    private TileType tileMarket;
    boolean needToCharge = false;
    private TileType expensiveResource;
    private Set<Point> reviewedPoints = new HashSet<>();
    private int panicMovesFull = 0;
    private int panicMoves = 0;

    @Override
    public void initialize(int boardSize, int maxInventorySize, int maxCharge, int winningScore, PlayerBoardView startingBoard, Point startTileLocation, boolean isRedPlayer, Random random) {
        this.boardSize = boardSize;
        this.maxInventorySize = maxInventorySize;
        this.currentPosition = startTileLocation;
        if (isRedPlayer) {
            tileMarket = TileType.RED_MARKET;
        } else {
            tileMarket = TileType.BLUE_MARKET;
        }
        setBoardData(startingBoard, recharges, markets);
        reviewedPoints.addAll(recharges);
        reviewedPoints.addAll(markets);
        this.expensiveResource = TileType.RESOURCE_DIAMOND;
        this.route = getFirstRoute(startingBoard);
    }

    private Queue<TurnAction> getFirstRoute(PlayerBoardView boardView){
        Point startPoint = boardView.getYourLocation();
        Point endPoint = boardView.getYourLocation();
        Queue<TurnAction> tempRoute = new LinkedList<>();
        for(int x = 0 ; x < 5; x++){
            endPoint = getFirstResourceFound(boardView, boardView.getYourLocation());
            Queue<TurnAction> innerTempRoute = getRouteTo(startPoint, endPoint);
            innerTempRoute.add(TurnAction.MINE);
            innerTempRoute.add(TurnAction.MINE);
            innerTempRoute.add(TurnAction.MINE);
            innerTempRoute.add(TurnAction.PICK_UP);
            //swap to the next jump
            tempRoute.addAll(innerTempRoute);
            startPoint = endPoint;
        }
        tempRoute.addAll(getRouteTo(endPoint, boardView.getYourLocation()));
        return tempRoute;
    }

    @Override
    public TurnAction getTurnAction(PlayerBoardView boardView, Economy economy, int currentCharge, boolean isRedTurn) {
        this.currentPosition = boardView.getYourLocation();
        this.expensiveResource = getCurrentExpensiveResource(economy);
        Point nearRecharger = getNearPointTo(currentPosition, recharges);
        Queue<TurnAction> actionsToCharge =  getRouteTo(currentPosition, nearRecharger);
        // What is the next route to move
        if(route.isEmpty()){
            // do nothing, wait until currentCharge bigger than 79
            if(needToCharge){
                if(currentCharge >= 79){
                    needToCharge = false;
                }
                //A Collision Happened
                if(!nearRecharger.equals(currentPosition)){
                    route = actionsToCharge;
                }else {
                    return TurnAction.PICK_UP;
                }
            }
            if(foundResourceAtLocation(boardView)){
                return TurnAction.MINE;
            }
            if(foundItemAtLocation(boardView)){
                return TurnAction.PICK_UP;
            }
            if(isFull(this.maxInventorySize, this.onStorage)){
                Point nearMarket = getNearPointTo(currentPosition, markets);
                Queue<TurnAction> tempRoute = getRouteTo(currentPosition,nearMarket);
                int movesNextRoute = getMovesToNearestRecharger(nearMarket, nearRecharger);
                // its able to reach this route or need to recharge in the middle of the route
                if(currentCharge < tempRoute.size() + movesNextRoute + panicMovesFull){
                    needToCharge = true;
                    route = actionsToCharge;
                }else {
                    route = tempRoute;
                }
            }else{
                Point nearestResource = getFirstResourceFound(boardView, currentPosition);
                Queue<TurnAction> tempRoute = getRouteTo(currentPosition, nearestResource);
                int movesNextRoute = getMovesToNearestRecharger(nearestResource, nearRecharger);
                // its able to reach this route or need to recharge in the middle of the route we will ensure at least it is able to reach the next point and the next recharger route
                if(currentCharge < tempRoute.size() + movesNextRoute + panicMoves){
                    needToCharge = true;
                    route = actionsToCharge;
                }else {
                    route = tempRoute;
                }
            }
            return route.poll();
        }else {
            return route.poll();
        }
    }


    private int getMovesToNearestRecharger(Point currentPosition, Point nearRechargerPoint){
        return getRouteTo(currentPosition, nearRechargerPoint).size() ;
    }

    private Point getNearPointTo(Point currentPosition, List<Point> points){
        double maxDistance = boardSize*boardSize/2.0;
        Point temp = new Point();
        for(Point recharger : points){
            double innerDistance = recharger.distance(currentPosition);
            if(innerDistance < maxDistance){
                maxDistance = innerDistance;
                temp = recharger;
            }
        }
        return temp;
    }

    private TileType getCurrentExpensiveResource(Economy economy) {
        Integer innerPrice = 0;
        ResourceType higher = ResourceType.DIAMOND;
        for(Map.Entry<ResourceType, Integer> entry : economy.getCurrentPrices().entrySet()){
            if(innerPrice < entry.getValue()){
                innerPrice = entry.getValue();
                higher = entry.getKey();
            }
        }
        return higher.getResourceTileType();
    }

    /**
     * Search a round a location the first resource
     *  searching in a spiral technique
     * @param boardView
     * @param currentPosition
     * @return
     */
    private Point getFirstResourceFound(PlayerBoardView boardView, Point currentPosition){
        for(int diameter = 1; diameter < boardSize; diameter++){
            List<Point> helperList = getPerimeterPoints(currentPosition, diameter);
            Point helperPoint = filterPointsToFirstExpensiveResource(boardView, helperList);
            if(helperPoint != null) {
                //save after validated and reviewed
                reviewedPoints.add(helperPoint);
                return helperPoint;
            }
        }
        return null;
    }

    private Point filterPointsToFirstExpensiveResource(PlayerBoardView boardView, List<Point> points){
        for(Point point : points){
            if(isAResourceAtPoint(boardView, point)){
                if(isExpensiveResource(boardView, point)) {
                    return point;
                }
            }else{
                //its an empty so mark as reviewed
                reviewedPoints.add(point);
            }
        }
        return null;
    }

    private boolean isExpensiveResource(PlayerBoardView boardView, Point point) {
        return boardView.getTileTypeAtLocation(point).equals(expensiveResource);
    }

    @Override
    public void onReceiveItem(InventoryItem itemReceived) {
        onStorage++;
    }
    @Override
    public void onSoldInventory(int totalSellPrice) {
        onStorage = 0;
    }
    @Override
    public String getName() {
        return "F&F: Toreto";
    }
    @Override
    public void endRound(int pointsScored, int opponentPointsScored) {
        System.out.println("Game Over");
    }
    /**
     * Find our Color Markets and Recharges
     * @param boardView
     * @param chargers
     * @param markets
     */
    private void setBoardData(PlayerBoardView boardView, List<Point> chargers, List<Point> markets) {
        for (int x = 0; x < boardSize; x++) {
            for (int y = 0; y < boardSize; y++) {
                TileType type = boardView.getTileTypeAtLocation(x, y);
                if (type.equals(tileMarket)) {
                    markets.add(new Point(x, y));
                }
                if (type.equals(TileType.RECHARGE)) {
                    chargers.add(new Point(x, y));
                }
            }
        }
    }

    private boolean foundItemAtLocation(PlayerBoardView boardView) {
        return boardView.getItemsOnGround().get(boardView.getYourLocation()) != null;
    }

    private boolean foundResourceAtLocation(PlayerBoardView boardView) {
        Point location = boardView.getYourLocation();
        TileType type = boardView.getTileTypeAtLocation(location);
        return type.equals(TileType.RESOURCE_DIAMOND) ||
                type.equals(TileType.RESOURCE_EMERALD) ||
                type.equals(TileType.RESOURCE_RUBY);
    }

    private boolean isAResourceAtPoint(PlayerBoardView boardView, Point location) {
        TileType type = boardView.getTileTypeAtLocation(location);
        return type.equals(TileType.RESOURCE_DIAMOND) ||
                type.equals(TileType.RESOURCE_EMERALD) ||
                type.equals(TileType.RESOURCE_RUBY);
    }

    /**
     * the storage is full?
     * @param maxInventorySize
     * @param onStorage
     * @return
     */
    private boolean isFull(int maxInventorySize, int onStorage) {
        return onStorage  >= maxInventorySize;
    }

    /**
     * Get Queue of TurnActions from one point to another
     * @param currentLocation start point
     * @param pointTo endPoint
     * @return
     */
    private Queue<TurnAction> getRouteTo(Point currentLocation, Point pointTo) {
        Queue<TurnAction> tempRoute = new LinkedList<>();
        if (currentLocation.getX() > pointTo.getX()) {
            double diffX = currentLocation.getX() - pointTo.getX();
            for (double i = 0; i < diffX; i++) {
                tempRoute.add(TurnAction.MOVE_LEFT);
            }
        }
        if (currentLocation.getX() < pointTo.getX()) {
            double diffX = pointTo.getX() - currentLocation.getX();
            for (double i = 0; i < diffX; i++) {
                tempRoute.add(TurnAction.MOVE_RIGHT);
            }
        }
        if (currentLocation.getY() > pointTo.getY()) {
            double diffY = currentLocation.getY() - pointTo.getY();
            for (double i = 0; i < diffY; i++) {
                tempRoute.add(TurnAction.MOVE_DOWN);
            }
        }
        if (currentLocation.getY() < pointTo.getY()) {
            double diffY = pointTo.getY() - currentLocation.getY();
            for (double i = 0; i < diffY; i++) {
                tempRoute.add(TurnAction.MOVE_UP);
            }
        }
        return tempRoute;
    }

    /**
     * Validate if the point is inside of the board game
     * @param x
     * @param y
     * @return
     */
    private boolean isValidCoordinates(int x, int y){
        return x >= 0 && x < boardSize  && y >= 0 && y < boardSize;
    }

    /**
     * Get valid points around another point dependent of diameter
     * @param currentPosition
     * @param diameter
     * @return
     */
    private List<Point> getPerimeterPoints(Point currentPosition, int diameter){
        List<Point> auxList = new ArrayList<>();
        int lowLimitX = currentPosition.x - diameter;
        int highLimitX = currentPosition.x + diameter;
        int lowLimitY = currentPosition.y - diameter;
        int highLimitY = currentPosition.y + diameter;

        //right side x -> fixed
        for (int y = lowLimitY; y <= highLimitY; y++) {
            Point auxPoint = new Point(highLimitX, y);
            if (isValidCoordinates(highLimitX, y) && !reviewedPoints.contains(auxPoint)) {
                auxList.add(auxPoint);
            }
        }

        //top side y -> fixed
        for (int x = highLimitX - 1; x > lowLimitX - 1  ; x--) {
            Point auxPoint = new Point(x, highLimitY);
            if (isValidCoordinates(x, highLimitY) && !reviewedPoints.contains(auxPoint)) {
                auxList.add(auxPoint);
            }
        }
        //left side x -> fixed
        for(int y = highLimitY - 1; y > lowLimitY - 1 ; y--){
            Point auxPoint = new Point(lowLimitX, y);
            if (isValidCoordinates(lowLimitX, y) && !reviewedPoints.contains(auxPoint)) {
                auxList.add(auxPoint);
            }
        }

        for(int x = lowLimitX + 1; x < highLimitX ; x++){
            Point auxPoint = new Point(x, lowLimitY);
            if (isValidCoordinates(x, lowLimitY) && !reviewedPoints.contains(auxPoint)) {
                auxList.add(auxPoint);
            }
        }
        return auxList;
    }
}