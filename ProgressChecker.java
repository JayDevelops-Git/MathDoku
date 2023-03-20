import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ProgressChecker {
    Board gameBoard;
    ArrayList<Cage> cageArr;
    //0 for cage not complete, 1 for cage complete
    ArrayList<Integer> completedCages;
    ArrayList<Tile> tilesOnBoard;
    ArrayList<Integer> valuesInColumn;
    ArrayList<Integer> valuesInRow;
    ArrayList<Tile> tilesInColumn;
    ArrayList<Tile> tilesInRow;
    boolean showMistakes = false;
    boolean isMistake;

    public ProgressChecker(Board gameBoard) {
        this.gameBoard = gameBoard;
        this.cageArr = gameBoard.getCages();
    }

    public boolean hasEmptyTiles() {
        for (Tile tile : gameBoard.getTileArray()) {
            if (tile.getValue() == null) {
                return true;
            }
        }
        return false;
    }

    public boolean findErrors() {
        tilesOnBoard = gameBoard.getTileArray();
        //i = row/column number of respective ArrayList
        ArrayList<ArrayList<Tile>> columnArr = new ArrayList<>();
        ArrayList<ArrayList<Tile>> rowArr = new ArrayList<>();
        //For each row / column in the grid
        for (int i = 0; i < gameBoard.getGameSize(); i++) {
            valuesInColumn = new ArrayList<>();
            valuesInRow = new ArrayList<>();
            tilesInColumn = new ArrayList<>();
            tilesInRow = new ArrayList<>();

            //Loop adds each tile to each appropriate row / column
            int gameSize = gameBoard.getGameSize();
            for (int j = 0; j < gameBoard.getGameSize(); j++) {
                valuesInColumn.add(tilesOnBoard.get(j + i * gameSize).getValue());
                tilesInColumn.add(tilesOnBoard.get(j + i * gameSize));
                valuesInRow.add(tilesOnBoard.get(i + j * gameSize).getValue());
                tilesInRow.add(tilesOnBoard.get(i + j * gameSize));
            }
            columnArr.add(tilesInColumn);
            rowArr.add(tilesInRow);
        }

        //Checks each column and row for duplicate numbers and each cage and flags row/column/cage accordingly
        boolean columnHasDuplicates = false;
        boolean rowHasDuplicates = false;
        for (ArrayList<Tile> column:columnArr) {
            Set<Integer> columnSet = new HashSet<>();
            for (Tile tile:column) {
                if (tile.getValue() != null) {
                    if (columnSet.contains(tile.getValue())) {
                        for (int n = 0; n < column.size(); n++) {
                            Tile currentTile = column.get(n);
                            currentTile.setColumnCorrect(false);
                            columnHasDuplicates = true;
                        }
                    } else {
                        columnSet.add(tile.getValue());
                    }
                }
            }
            if (!columnHasDuplicates) {
                for (int n = 0; n < column.size(); n++) {
                    Tile currentTile = column.get(n);
                    currentTile.setColumnCorrect(true);
                }
            }
        }
        for(ArrayList<Tile> row:rowArr) {
            Set<Integer> rowSet =  new HashSet<>();
            for (Tile tile:row) {
                if (tile.getValue() != null) {
                    if (rowSet.contains(tile.getValue())) {

                        for (int n = 0; n < row.size(); n++) {
                            Tile currentTile = row.get(n);
                            currentTile.setRowCorrect(false);
                        }
                        rowHasDuplicates = true;
                    } else {
                        rowSet.add(tile.getValue());
                    }
                }
            }
            if (!rowHasDuplicates) {
                for (int n = 0; n < row.size(); n++) {
                    Tile currentTile = row.get(n);
                    currentTile.setRowCorrect(true);
                }
            }
        }

        if (showMistakes) {
            //Updates and highlights tiles according to flags
            for (Tile tile : tilesOnBoard) {
                //Remove incorrect cage flag if it contains an empty tile
                if (tile.getCage().hasEmptyTile()) {
                    tile.setCageCorrect(true);
                }

                if (!tile.isColumnCorrect() || !tile.isRowCorrect() || !tile.isCageCorrect()) {
                    tile.setColour(1, 0, 0, 0.2);
                } else {
                    tile.setColour(1, 1, 1, 1);
                }

            }
        }

        gameBoard.createBoard();
        if (isMistake) {
            isMistake = false;
            return true;
        }

        return false;
    }

    public void toggleShowMistakes() {
        this.showMistakes = !showMistakes;
    }

    public boolean showingMistakes() {
        if (showMistakes) {
            return true;
        }
        return false;
    }

    public boolean hasWon() {
        if (!findErrors()) {
            completedCages = new ArrayList<>();
            for (Cage cage : cageArr) {
                if (cage.isCorrect()) {
                    completedCages.add(1);
                } else {
                    completedCages.add(0);
                }
            }
            findErrors();
            for (Tile tile:gameBoard.getTileArray()) {
                if (!tile.isColumnCorrect || !tile.isRowCorrect) {
                    return false;
                }
            }
            if (!completedCages.contains(0)) {
                return true;
            }
            }
            return false;
        }
    }
