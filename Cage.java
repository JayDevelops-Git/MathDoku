import java.util.ArrayList;

public class Cage {
    int target;
    String operator;
    int cageTotal;
    int headTileIndex;
    Tile[] tiles;
    int height = 1;
    int width = 1;
    ArrayList<Integer> checkedHeights = new ArrayList<Integer>();
    ArrayList<Integer> checkedWidths = new ArrayList<Integer>();

    public Cage(int target, String operator, Tile[] tiles) {
        this.target = target;
        this.operator = operator;
        this.tiles = tiles;
        this.headTileIndex = tiles[0].getIndex();
        Tile prevTile = tiles[0];
        for (Tile tile : tiles) {
            if (tile.getY() == prevTile.getY() + 1 && !checkedHeights.contains(tile.getY())) {
                this.height++;
                checkedHeights.add(tile.getY());
            }
            if (tile.getX() == prevTile.getX() + 1 && !checkedWidths.contains(tile.getX())) {
                this.width++;
                checkedWidths.add(tile.getX());
            }
            prevTile = tile;
        }
    }

    public int getHeight() {
        return height * 50;
    }

    public int getHeadTileIndex() {
        return headTileIndex;
    }

    public Tile getHeadTile() {
        return tiles[0];
    }

    public int getWidth() {
        return width * 50;
    }

    public int getTarget() {
        return target;
    }

    public boolean hasTile(Tile tile) {
        if (tile.getCage().equals(this)) {
            return true;
        }
        return false;
    }

    public boolean hasEmptyTile() {
        for (Tile tile : tiles) {
            if (tile.getValue() == null) {
                return true;
            }
        }
        return false;
    }

    public boolean isCorrect() {
        int cageTotal = 0;
        Tile[] tilesInCage = this.getTiles();
        int higherValue;
        int lowerValue;
        int carryValue = 0;
        for (Tile tile : tilesInCage) {
            if (cageTotal > tile.getValue()) {
                higherValue = cageTotal;
                lowerValue = tile.getValue();
            } else {
                higherValue = tile.getValue();
                lowerValue = cageTotal;
            }
            if (this.getOperator().equals("+")) {
                cageTotal = cageTotal + tile.getValue();
            } else if (this.getOperator().equals("x")) {
                if (cageTotal != 0) {
                    cageTotal = cageTotal * tile.getValue();
                } else if (carryValue != 0) {
                    cageTotal = carryValue * tile.getValue();
                } else {
                    carryValue = tile.getValue();
                }
            } else if (this.getOperator().equals("-")) {
                cageTotal = higherValue - lowerValue;
            } else if (this.getOperator().equals("÷")) {
                if (lowerValue != 0) {
                    cageTotal = higherValue / lowerValue;
                } else {
                    cageTotal = tile.getValue();
                }
            } else if (this.getOperator().equals(" ")) {
                cageTotal = tile.getValue();
            }
        }
        if (cageTotal == this.getTarget()) {
            return true;
        }
        return false;
    }

    public String getOperator() {
        return operator;
    }

    public Tile[] getTiles() {
        return tiles;
    }

    public ArrayList<Double> getCageEdges(int gameSize) {

        ArrayList<Double> cageEdges = new ArrayList<>();

        ArrayList<Integer> tileIndexes = new ArrayList<>();
        for (Tile tile : tiles) {
            tileIndexes.add(tile.getIndex());
        }

        //Ensures a game of size 2x2 will have a thick border
        if (gameSize == 2) {
            //Adds top edge of tile to the cage border if no cage-tile above current tile
            for (Tile tile : tiles) {
                if (!tileIndexes.contains(tile.getIndex() - 1) || (tile.getIndex() == 0 || tile.getIndex() == 2)) {
                    cageEdges.add((double) tile.getX());
                    cageEdges.add((double) tile.getY());

                    cageEdges.add((double) tile.getX() + 1);
                    cageEdges.add((double) tile.getY());
                }
            }

            //Adds right edge of tile to the cage border if no cage-tile to the right of current tile
            for (Tile tile : tiles) {
                if (!tileIndexes.contains(tile.getIndex() + gameSize) || (tile.getIndex() == 2 || tile.getIndex() == 3)) {
                    cageEdges.add((double) tile.getX() + 1);
                    cageEdges.add((double) tile.getY());

                    cageEdges.add((double) tile.getX() + 1);
                    cageEdges.add((double) tile.getY() + 1);
                }
            }

            //Adds bottom edge of tile to the cage border if no cage-tile below current tile
            for (Tile tile : tiles) {
                if (!tileIndexes.contains(tile.getIndex() + 1) || (tile.getIndex() == 1 || tile.getIndex() == 3)) {
                    cageEdges.add((double) tile.getX());
                    cageEdges.add((double) tile.getY() + 1);

                    cageEdges.add((double) tile.getX() + 1);
                    cageEdges.add((double) tile.getY() + 1);
                }
            }

            //Adds left edge of tile to the cage border if no cage-tile left of current tile
            for (Tile tile : tiles) {
                if (!tileIndexes.contains(tile.getIndex() - gameSize) || (tile.getIndex() == 0 || tile.getIndex() == 1)) {
                    cageEdges.add((double) tile.getX());
                    cageEdges.add((double) tile.getY());

                    cageEdges.add((double) tile.getX());
                    cageEdges.add((double) tile.getY() + 1);
                }
            }
        }
        else {
            //Adds top edge of tile to the cage border if no cage-tile above current tile
            for (Tile tile : tiles) {
                if (!tileIndexes.contains(tile.getIndex() - 1)) {
                    cageEdges.add((double) tile.getX());
                    cageEdges.add((double) tile.getY());

                    cageEdges.add((double) tile.getX() + 1);
                    cageEdges.add((double) tile.getY());
                }
            }

            //Adds right edge of tile to the cage border if no cage-tile to the right of current tile
            for (Tile tile : tiles) {
                if (!tileIndexes.contains(tile.getIndex() + gameSize)) {
                    cageEdges.add((double) tile.getX() + 1);
                    cageEdges.add((double) tile.getY());

                    cageEdges.add((double) tile.getX() + 1);
                    cageEdges.add((double) tile.getY() + 1);
                }
            }

            //Adds bottom edge of tile to the cage border if no cage-tile below current tile
            for (Tile tile : tiles) {
                if (!tileIndexes.contains(tile.getIndex() + 1)) {
                    cageEdges.add((double) tile.getX());
                    cageEdges.add((double) tile.getY() + 1);

                    cageEdges.add((double) tile.getX() + 1);
                    cageEdges.add((double) tile.getY() + 1);
                }
            }

            //Adds left edge of tile to the cage border if no cage-tile left of current tile
            for (Tile tile : tiles) {
                if (!tileIndexes.contains(tile.getIndex() - gameSize)) {
                    cageEdges.add((double) tile.getX());
                    cageEdges.add((double) tile.getY());

                    cageEdges.add((double) tile.getX());
                    cageEdges.add((double) tile.getY() + 1);
                }
            }
        }
        return cageEdges;
    }
}
