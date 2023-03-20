import javafx.animation.RotateTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.shape.*;
import javafx.scene.control.Label;
import javafx.util.Duration;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;


public class Board extends Application {
    int gameSize;
    int mouseX;
    int mouseY;
    String keyPressed;
    GridPane boardPane = new GridPane();;
    Tile selectedTile;
    KeyEvent lastKeyEvent;
    ProgressChecker progressChecker;
    PuzzleConfigurator puzzleConfigurator;
    boolean gridCreated = false;

    ArrayList<Tile> tileArr;
    ArrayList<Cage> cageArr = new ArrayList<>();
    ArrayList<Button> buttonArr;

    Integer turnNumber = 0;
    Map<Integer, Tile> turnHistory = new HashMap<>();

    Integer undoNumber = 0;
    Integer redoNumber = 0;
    Integer timesToRedo = 0;
    Map<Integer, Integer> undoHistory = new HashMap<>();

    Button buttonUndo;
    Button buttonRedo;

    Stage stage;
    BorderPane root = new BorderPane();

    int fontSize = 24;
    int tileSize = 50;
    String chosenTextSize = "Medium";

    @Override
    public void start(Stage gameWindow) throws Exception {

        this.stage = gameWindow;
        progressChecker = new ProgressChecker(this);
        puzzleConfigurator = new PuzzleConfigurator("6x6.txt");
        this.gameSize = puzzleConfigurator.getGameSize();

        //Setting row and column constraints to prevent gaps between tiles on game board
        for (int i = 0; i < gameSize; i++) {
            ColumnConstraints column = new ColumnConstraints(tileSize);
            boardPane.getColumnConstraints().add(column);
            RowConstraints row = new RowConstraints(tileSize);
            boardPane.getRowConstraints().add(row);
        }

        puzzleConfigurator.getConfiguration(this);

        buttonRedo = new Button("Redo");
        buttonUndo = new Button ("Undo");
        Button buttonBoardClear = new Button("Clear Board");
        Button buttonMistakes = new Button("Show Mistakes");
        Button buttonLoad = new Button ("Load");

        VBox vBoxButtons = new VBox();

        VBox.setVgrow(buttonRedo,Priority.ALWAYS);
        buttonRedo.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(buttonUndo,Priority.ALWAYS);
        buttonUndo.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(buttonBoardClear,Priority.ALWAYS);
        buttonBoardClear.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(buttonMistakes,Priority.ALWAYS);
        buttonMistakes.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(buttonLoad,Priority.ALWAYS);
        buttonLoad.setMaxWidth(Double.MAX_VALUE);

        vBoxButtons.getChildren().addAll(buttonRedo,buttonUndo,buttonBoardClear,buttonMistakes,buttonLoad);


        HBox hBoxButtons = new HBox();

        //Creating an array of numbered buttons for user enter numbers to the board + clear tile button
        buttonArr = new ArrayList<Button>();
        for (int b = 0; b < gameSize; b++) {
            buttonArr.add(new Button(Integer.toString(b+1)));
            hBoxButtons.getChildren().addAll(buttonArr.get(b));
        }
        Button buttonClearTile = new Button("Clear") ;
        buttonArr.add(buttonClearTile);
        hBoxButtons.getChildren().addAll(buttonArr.get(gameSize));

        //Adding functionality to the array of numbered buttons and the clear tile button
        for (int b = 0; b <= gameSize; b++) {
            int buttonValue = b;
            buttonArr.get(b).setOnAction(actionEvent -> {
                //Clear button
                if (buttonValue == gameSize) {
                    takeAction("");

                }
                //Numbered buttons
                else {
                    takeAction(Integer.toString(buttonValue + 1));
                }
                try {
                    showProgress();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                createBoard();
            });
        }

        //Mistake button
        buttonMistakes.setOnAction(actionEvent -> {
            progressChecker.toggleShowMistakes();
            if (progressChecker.showingMistakes()){
                System.out.println("Showing mistakes");
                progressChecker.findErrors();
            }
            else {
                System.out.println("Not showing mistakes");
                for (Tile tile:tileArr) {
                    tile.setColour(1,1,1,1);
                    createBoard();
                }
            }
        });

        //Clear Board button
        buttonBoardClear.setOnAction(actionEvent -> {
            clearBoard();
        });

        //Undo Button
        buttonUndo.setDisable(true);
        buttonUndo.setOnAction(actionEvent -> {
            undo();
        });

        //Redo Button
        buttonRedo.setDisable(true);
        buttonRedo.setOnAction(actionEvent -> {
            redo();
        });

        //Choice box for text
        ChoiceBox cb = new ChoiceBox();
        cb.getItems().add("Small");
        cb.getItems().add("Medium");
        cb.getItems().add("Large");
        cb.setValue("Medium");
        Label textSize = new Label("Text Size:");
        vBoxButtons.getChildren().add(textSize);
        vBoxButtons.getChildren().add(cb);

        cb.setOnAction(actionEvent -> {
            String chosenFont = (String) cb.getValue();
            if (chosenFont.equals("Small")) {
                chosenTextSize = "Small";
                fontSize = 18;
            }
            else if (chosenFont.equals("Medium")) {
                fontSize = 24;
                chosenTextSize = "Medium";
            }
            else if (chosenFont.equals("Large")) {
                fontSize = 36;
                chosenTextSize = "Large";
            }
            createBoard();
        });

        BorderPane.setAlignment(vBoxButtons, Pos.CENTER_RIGHT);
        BorderPane.setAlignment(boardPane, Pos.CENTER);
        BorderPane.setAlignment(hBoxButtons, Pos.BOTTOM_CENTER);
        root.setRight(vBoxButtons);
        root.setCenter(boardPane);
        root.setBottom(hBoxButtons);

        AtomicReference<Scene> scene = new AtomicReference<>(new Scene(root));

        //Load Button
        buttonLoad.setOnAction(actionEvent -> {

            tileSize = 50;

            StackPane secondaryLayout = new StackPane();
            Scene secondScene = new Scene(secondaryLayout, 230, 100);

            Stage loadWindow = new Stage();
            loadWindow.setTitle("Load a puzzle");
            loadWindow.setScene(secondScene);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select a .txt puzzle file");
            String dir = System.getProperty("user.dir");
            fileChooser.setInitialDirectory(new File(dir));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            File chosenFile = fileChooser.showOpenDialog(loadWindow);

            gridCreated = false;
            boardPane = new GridPane();

            root.getChildren().remove(boardPane);
            createGameWindow(chosenFile.getName());
        });

        //Mouse event gets mouse coordinates and converts it into grid coordinates.
        boardPane.setOnMouseClicked(mouseEvent -> {
            this.mouseX = roundDown(mouseEvent.getX());
            this.mouseY = roundDown(mouseEvent.getY());
            //If statement prevents creating new tiles outside the game-board if board is clicked at edges
            if (mouseX < gameSize && mouseY < gameSize) {
                this.selectedTile = tileArr.get(getTileIndex(mouseX, mouseY));
                System.out.println("X: " + mouseX + " Y: " + mouseY);
                System.out.println("Tile index (tile method): " + selectedTile.getIndex());
                System.out.println("Tile colour: "+(selectedTile.getRed()+","+selectedTile.getGreen()+","+selectedTile.getBlue()+","+selectedTile.getOpacity()));
                System.out.println("Tile Row-Correct: "+selectedTile.isRowCorrect()+" Tile Column-Correct: "+selectedTile.isColumnCorrect());
                System.out.println("Tile is Cage-Correct: "+selectedTile.isCageCorrect());
                System.out.println("Tile value history: " +selectedTile.getValueHistory());
                System.out.println("Associated Cage: " + selectedTile.getCage().getTarget()+selectedTile.getCage().getOperator());
                //System.out.println("Tile index (board method): " + getTileIndex(mouseX, mouseY));
                try {
                    System.out.println("Tile value: " + selectedTile.getValue());
                } catch (Exception e) {
                    System.out.println("Tile has no value");
                }
            }
        });
        //Updates selected tile with relevant value
        scene.get().setOnKeyPressed(keyEvent -> {
            //If statement prevents creating new tiles outside the game-board if board is clicked at edges
            if (mouseX < gameSize && mouseY < gameSize) {
                this.lastKeyEvent = keyEvent;
                keyPressed = keyEvent.getText();
                //Try-catch block catches events where an invalid character such as a symbol is entered
                try {
                    //If statement only allows valid values to be entered (that follow MathDoku rules)
                    if (keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
                        takeAction("");
                    }
                    else if (Integer.parseInt(keyPressed) > 0 && Integer.parseInt(keyPressed) <= gameSize) {
                        takeAction((keyPressed));
                    }
                }
                catch (Exception e) {
                    System.out.println("Invalid character entered (not from 1 - "+gameSize+" or backspace).");
                }
                try {
                    showProgress();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                createBoard();
            }
        });

        /*gameWindow.minWidthProperty().bind(boardPane.heightProperty().multiply(2));
        gameWindow.minHeightProperty().bind(boardPane.widthProperty().multiply(2));*/

        gameWindow.setScene(scene.get());
        gameWindow.minWidthProperty().bind(scene.get().heightProperty().multiply(1));
        gameWindow.minHeightProperty().bind(scene.get().widthProperty().multiply(1));
        gameWindow.show();
        System.out.println(stage.getHeight());
        System.out.println(stage.getWidth());

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            stage.heightProperty().addListener((obs1, oldVal1, newVal1) -> {
                resize();
            });
        });
    }

    public void updateTile(String label){
        Circle newTile = new Circle(10);
        newTile.setFill(Color.color(selectedTile.getRed(),selectedTile.getGreen(),selectedTile.getBlue(),selectedTile.getOpacity()));
        newTile.setStroke(Color.TRANSPARENT);
        Text text = new Text(label);
        text.setFont(Font.font(fontSize));
        boardPane.add(new StackPane(newTile, text), selectedTile.getX(), selectedTile.getY());
        //Updates value of tile object
        if (!label.equals("")){
            selectedTile.setValue(Integer.parseInt(label));
        }
        else {
            selectedTile.setValue(null);
        }
        if (progressChecker.showMistakes) {
            progressChecker.findErrors();
        }
    }

    public void updateTile(String label, boolean isUndo){
        Circle newTile = new Circle(10);
        newTile.setFill(Color.color(selectedTile.getRed(),selectedTile.getGreen(),selectedTile.getBlue(),selectedTile.getOpacity()));
        newTile.setStroke(Color.TRANSPARENT);
        Text text = new Text(label);
        text.setFont(Font.font(fontSize));
        boardPane.add(new StackPane(newTile, text), mouseX, mouseY);
        //Updates value of tile object
        if (!label.equals("")){
            selectedTile.setValue(Integer.parseInt(label),isUndo);
        }
        else {
            selectedTile.setValue(null,isUndo);
        }
        if (progressChecker.showMistakes) {
            progressChecker.findErrors();
        }
    }

    public void createCage(int target, String operator, Tile[] tiles) {
        Cage cage = new Cage(target,operator,tiles);
        this.cageArr.add(cage);
        createCageShape(cage);
        for (Tile tile:tiles) {
            tile.assignCage(cage);
        }
    }

    public void createCageShape(Cage cage) {

        Tile[] tiles = cage.getTiles();
        int target = cage.getTarget();
        String operator = cage.getOperator();
        Text cageBoxValue;
        int headColumnIndex = tiles[0].getY();
        int headRowIndex = tiles[0].getX();
        //Adds grid to cage along with values if applicable
        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i].getValue() != null) {
                cageBoxValue = new Text(Integer.toString(tiles[i].getValue()));
            }
            else {
                cageBoxValue = new Text("");
            }
            cageBoxValue.setFont(Font.font(fontSize));
            int tileColumnIndex = tiles[i].getY();
            int tileRowIndex = tiles[i].getX();

            Rectangle fillTile = new Rectangle(tileSize,tileSize);
            fillTile.setFill(Color.color(tiles[i].getRed(),tiles[i].getGreen(),tiles[i].getBlue(),tiles[i].getOpacity()));
            fillTile.setStroke(Color.BLACK);
            boardPane.add(new StackPane(fillTile,cageBoxValue), tileRowIndex,tileColumnIndex);
        }
        //Adding cage values to tiles
        Rectangle cageShape = new Rectangle(cage.getWidth(),cage.getHeight());
        cageShape.setFill(Color.TRANSPARENT);
        cageShape.setStroke(Color.TRANSPARENT);
        Text operatorLabel = new Text(" "+target+operator);
        operatorLabel.setFont(Font.font(fontSize-10));
        StackPane finalCage = new StackPane(cageShape,operatorLabel);
        boardPane.add(finalCage,headRowIndex,headColumnIndex);
        GridPane.setValignment(finalCage, VPos.TOP);
        StackPane.setAlignment(operatorLabel,Pos.TOP_LEFT);

        //Creating cage shape using lines
        ArrayList<Double> cageEdgePoints = cage.getCageEdges(gameSize);
        for (int n = 0; n < cageEdgePoints.size()/4; n++) {
            if (n == 0) {
                Line cageEdge = new Line(cageEdgePoints.get(n)*tileSize, cageEdgePoints.get(n + 1)*tileSize, cageEdgePoints.get(n + 2)*tileSize, cageEdgePoints.get(n + 3)*tileSize);
                cageEdge.setStroke(Color.BLACK);
                cageEdge.setStrokeWidth(3);
                boardPane.add(cageEdge,(int) Math.round(cageEdgePoints.get(n)),(int) Math.round(cageEdgePoints.get(n + 1)));
                GridPane.setValignment(cageEdge, VPos.TOP);
            }
            else {
                int m = n*4;
                Line cageEdge = new Line(cageEdgePoints.get(m)*tileSize, cageEdgePoints.get(m + 1)*tileSize, cageEdgePoints.get(m + 2)*tileSize, cageEdgePoints.get(m + 3)*tileSize);
                cageEdge.setStroke(Color.BLACK);
                cageEdge.setStrokeWidth(3);
                boardPane.add(cageEdge,(int) Math.round(cageEdgePoints.get(m)),(int) Math.round(cageEdgePoints.get(m + 1)));
                GridPane.setValignment(cageEdge, VPos.TOP);
            }
        }
    }

    public void resize() {
        root.getChildren().remove(boardPane);
        boardPane = new GridPane();

        double leftSize = root.getWidth() - root.getWidth()*0.01;
        if (gameSize < 5) {
            tileSize = (int) leftSize / 6;
        }
        else if (gameSize < 7) {
            tileSize = (int) leftSize / 8;
        }
        else{
            tileSize = (int) leftSize / 10;
        }

        //Setting row and column constraints to prevent gaps between tiles on game board
        for (int i = 0; i < gameSize; i++) {
            ColumnConstraints column = new ColumnConstraints(tileSize);
            boardPane.getColumnConstraints().add(column);
            RowConstraints row = new RowConstraints(tileSize);
            boardPane.getRowConstraints().add(row);
        }
        createBoard();
        root.setCenter(boardPane);
        createBoard();

        boardPane.setOnMouseClicked(mouseEvent -> {
            this.mouseX = roundDown(mouseEvent.getX());
            this.mouseY = roundDown(mouseEvent.getY());
            //If statement prevents creating new tiles outside the game-board if board is clicked at edges
            if (mouseX < gameSize && mouseY < gameSize) {
                this.selectedTile = tileArr.get(getTileIndex(mouseX, mouseY));
                System.out.println("X: " + mouseX + " Y: " + mouseY);
                System.out.println("Tile index (tile method): " + selectedTile.getIndex());
                System.out.println("Tile colour: "+(selectedTile.getRed()+","+selectedTile.getGreen()+","+selectedTile.getBlue()+","+selectedTile.getOpacity()));
                System.out.println("Tile Row-Correct: "+selectedTile.isRowCorrect()+" Tile Column-Correct: "+selectedTile.isColumnCorrect());
                System.out.println("Tile is Cage-Correct: "+selectedTile.isCageCorrect());
                System.out.println("Tile value history: " +selectedTile.getValueHistory());
                System.out.println("Associated Cage: " + selectedTile.getCage().getTarget()+selectedTile.getCage().getOperator());
                //System.out.println("Tile index (board method): " + getTileIndex(mouseX, mouseY));
                try {
                    System.out.println("Tile value: " + selectedTile.getValue());
                } catch (Exception e) {
                    System.out.println("Tile has no value");
                }
            }
        });
    }

    public int getTileIndex(int xPos, int yPos) {
        int n = 0;
        System.out.println("X: "+xPos+" Y: "+yPos);
        for (Tile tile : tileArr) {
            if (xPos == tile.getX() && yPos == tile.getY()) {
                return n;
            }
            n++;
        }
        return -1;
    }

    public ArrayList<Cage> getCages() {
        return cageArr;
    }

    public int roundDown(double value){
        double correctedValue = value/tileSize;
        correctedValue = Math.floor(correctedValue);
        return (int)correctedValue;
    }

    public int getGameSize() {
        return gameSize;
    }

    public ArrayList<Tile> getTileArray() {
        return tileArr;
    }

    public void clearBoard() {
        for (Tile tile:tileArr) {
            tile.setValue(null);
            tile.setColour(1,1,1,1);
            tile.setColumnCorrect(true);
            tile.setRowCorrect(true);
            tile.setCageCorrect(true);
            tile.resetHistory();
        }
        for (Cage cage:cageArr) {
            createCageShape(cage);
        }
        undoHistory = new HashMap<>();
        undoNumber = 0;
        turnNumber = 0;
        turnHistory = new HashMap<>();
        buttonUndo.setDisable(true);
        buttonRedo.setDisable(true);
    }

    public void takeAction(String valueEntered) {
        turnHistory.put(turnNumber,selectedTile);
        turnNumber++;
        if (valueEntered == null) {
            updateTile("");
        }
        else {
            updateTile(valueEntered);
        }
        buttonUndo.setDisable(false);
        System.out.println("Turns Taken: " + turnNumber);

        timesToRedo = 0;
        buttonRedo.setDisable(true);
    }

    public void undo() {
        System.out.println("START OF TILE UNDO");
        buttonRedo.setDisable(false);
        Tile lastTile = turnHistory.get(turnNumber-1);

        undoNumber++;
        timesToRedo++;
        undoHistory.put(undoNumber,lastTile.getValue());
        System.out.println("Undo number: "+undoNumber);

        selectedTile = lastTile;


        System.out.println("Selected tile: "+lastTile.getValue());
        ArrayList<Integer> tileHistory = lastTile.getValueHistory();
        System.out.println("Selected tile history: "+tileHistory);
        if (tileHistory.get(tileHistory.size()-2) == null) {
            updateTile("",true);
        }
        else {
            updateTile(Integer.toString(tileHistory.get(tileHistory.size()-2)),true);
        }
        lastTile.removeLastHistory();

        turnNumber--;
        //turnHistory.remove(turnNumber);
        tileHistory = lastTile.getValueHistory();
        System.out.println("New Tile History: "+tileHistory);

        System.out.println("Turn history: " + turnHistory);
        System.out.println("Undo history: "+undoHistory);
        System.out.println("Turn number: " + turnNumber);
        createBoard();
        if (selectedTile.equals(turnHistory.get(0)) && selectedTile.getValue() == null) {
            buttonUndo.setDisable(true);
        }
        if (redoNumber > 0) {
            redoNumber--;
        }
        if (undoHistory.size() == 0) {
            buttonUndo.setDisable(true);
        }
    }

    public void redo() {
        System.out.println("START OF TILE REDO");
        redoNumber++;
        timesToRedo--;
        Integer undoneValue = undoHistory.get(undoNumber);
        selectedTile = turnHistory.get(turnNumber);
        //System.out.println("Selected tile value: "+selectedTile.getValue());
        turnNumber++;

        if (undoneValue == null) {
            updateTile("");
        }
        else {
            updateTile(Integer.toString(undoneValue));
        }
        System.out.println("Turn history: "+turnHistory);
        System.out.println("Undo history: "+undoHistory);
        System.out.println("Turn number: " + turnNumber);
        System.out.println("Redo number: " + redoNumber);
        System.out.println("Turns left to be redone: " + timesToRedo);
        undoNumber--;
        if (timesToRedo == 0) {
            buttonRedo.setDisable(true);
        }
        buttonUndo.setDisable(false);
    }

    public void showProgress() throws InterruptedException {
        if (!progressChecker.hasEmptyTiles()) {
            StackPane progressLayout = new StackPane();
            Stage progressWindow = new Stage();
            Scene completionScene = new Scene(progressLayout, 200, 100);
            progressWindow.setScene(completionScene);
            if (progressChecker.hasWon()) {
                progressWindow.setTitle("Puzzle solved");
                Text text = new Text("Congratulations!\n Puzzle correctly solved");
                Random r = new Random();
                for (Tile tile:tileArr) {
                    double x = r.nextDouble();
                    double y = r.nextDouble();
                    double z = r.nextDouble();
                    tile.setColour(x,y,z,1);
                    createBoard();

                    Rectangle rectangle = new Rectangle(tileSize,tileSize);
                    rectangle.setFill(Color.color(x,y,z,1));
                    RotateTransition rt = new RotateTransition();
                    rt.setDuration(Duration.millis(50));
                    rt.setNode(rectangle);
                    rt.setByAngle(180);
                    rt.setRate(0.3);
                    rt.setCycleCount(5);
                    rt.play();
                    Thread.sleep(1);

                    root.getChildren().remove(boardPane);
                    GridPane boardPane = new GridPane();
                    root.getChildren().add(boardPane);
                    for (int i = 0; i < gameSize; i++) {
                        ColumnConstraints column = new ColumnConstraints(tileSize);
                        boardPane.getColumnConstraints().add(column);
                        RowConstraints row = new RowConstraints(tileSize);
                        boardPane.getRowConstraints().add(row);
                    }
                    boardPane.add(rectangle,tile.getX(),tile.getY());
                    Thread.sleep(1);

                }
                progressLayout.getChildren().add(text);
                progressWindow.show();
            }
        }
        if (progressChecker.showingMistakes()) {
            progressChecker.findErrors();
        }
    }

    public void createBoard(int gameSize) {
        if (!gridCreated) {
            tileArr = new ArrayList<>();
            int tileIndex = 0;
            for (int i = 0; i < gameSize; i++) {
                for (int j = 0; j < gameSize; j++) {
                    Rectangle tile = new Rectangle(tileSize, tileSize);
                    tile.setFill(Color.WHITE);
                    tile.setStroke(Color.BLACK);
                    boardPane.add(tile, j, i);

                    tileArr.add(new Tile(i, j, tileIndex, null));

                    tileIndex++;
                }
            }
            this.gridCreated = true;
        }
        else {
            for (Tile oldTile:tileArr) {
                Rectangle tile = new Rectangle(tileSize,tileSize);
                tile.setFill(Color.color(oldTile.getRed(),oldTile.getGreen(),oldTile.getBlue(), oldTile.getOpacity()));
                tile.setStroke(Color.BLACK);
                Rectangle whiteTile = new Rectangle(tileSize,tileSize);
                whiteTile.setFill(Color.WHITE);
                whiteTile.setStroke(Color.BLACK);
                boardPane.add(whiteTile,oldTile.getX(),oldTile.getY());
                if (oldTile.getValue() != null) {
                    Text label = new Text(Integer.toString(oldTile.getValue()));
                    label.setFont(Font.font(fontSize));
                    boardPane.add(new StackPane(tile,label),oldTile.getX(),oldTile.getY());
                }
                else {
                    boardPane.add(tile,oldTile.getX(),oldTile.getY());
                }

            }
            for (Cage cage:cageArr) {
                createCageShape(cage);
            }
        }

    }

    public void createBoard() {
        if (!gridCreated) {
            tileArr = new ArrayList<Tile>();
            int tileIndex = 0;
            for (int i = 0; i < gameSize; i++) {
                for (int j = 0; j < gameSize; j++) {
                    Rectangle tile = new Rectangle(tileSize, tileSize);
                    tile.setFill(Color.WHITE);
                    tile.setStroke(Color.BLACK);
                    boardPane.add(tile, j, i);
                    tileArr.add(new Tile(i, j, tileIndex, null));
                    tileIndex++;
                }
            }
            this.gridCreated = true;
        }
        else {
            for (Tile oldTile:tileArr) {
                Rectangle tile = new Rectangle(tileSize,tileSize);
                tile.setFill(Color.color(oldTile.getRed(),oldTile.getGreen(),oldTile.getBlue(), oldTile.getOpacity()));
                tile.setStroke(Color.BLACK);
                Rectangle whiteTile = new Rectangle(tileSize,tileSize);
                whiteTile.setFill(Color.WHITE);
                whiteTile.setStroke(Color.BLACK);
                boardPane.add(whiteTile,oldTile.getX(),oldTile.getY());
                if (oldTile.getValue() != null) {
                    Text label = new Text(Integer.toString(oldTile.getValue()));
                    label.setFont(Font.font(fontSize));
                    boardPane.add(new StackPane(tile,label),oldTile.getX(),oldTile.getY());
                }
                else {
                    boardPane.add(tile,oldTile.getX(),oldTile.getY());
                }

            }
            for (Cage cage:cageArr) {
                createCageShape(cage);
            }
        }

    }

    public Tile getTile(int index) {
        for (Tile tile:tileArr) {
            if (tile.getIndex() == index) {
                return tile;
            }
        }
        return null;
    }

    public void setGameSize(int number) {
        this.gameSize = number;
    }

    public static void main(String[] args){
        launch(args);
    }

    public void createGameWindow(String filename) {

        //Clearing the cage array
        this.cageArr = new ArrayList<>();

        this.progressChecker = new ProgressChecker(this);
        this.puzzleConfigurator = new PuzzleConfigurator(filename);
        this.gameSize = puzzleConfigurator.getGameSize();

        //this.boardPane = new GridPane();
        //Setting row and column constraints to prevent gaps between tiles on game board
        for (int i = 0; i < gameSize; i++) {
            ColumnConstraints column = new ColumnConstraints(tileSize);
            boardPane.getColumnConstraints().add(column);
            RowConstraints row = new RowConstraints(tileSize);
            boardPane.getRowConstraints().add(row);
        }

        puzzleConfigurator.getConfiguration(this);

        buttonRedo = new Button("Redo");
        buttonUndo = new Button ("Undo");
        Button buttonBoardClear = new Button("Clear Board");
        Button buttonMistakes = new Button("Show Mistakes");
        Button buttonLoad = new Button ("Load");

        VBox vBoxButtons = new VBox();

        VBox.setVgrow(buttonRedo,Priority.ALWAYS);
        buttonRedo.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(buttonUndo,Priority.ALWAYS);
        buttonUndo.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(buttonBoardClear,Priority.ALWAYS);
        buttonBoardClear.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(buttonMistakes,Priority.ALWAYS);
        buttonMistakes.setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(buttonLoad,Priority.ALWAYS);
        buttonLoad.setMaxWidth(Double.MAX_VALUE);

        vBoxButtons.getChildren().addAll(buttonRedo,buttonUndo,buttonBoardClear,buttonMistakes,buttonLoad);

        HBox hBoxButtons = new HBox();

        //Creating an array of numbered buttons for user enter numbers to the board + clear tile button
        this.buttonArr = new ArrayList<Button>();
        for (int b = 0; b < gameSize; b++) {
            buttonArr.add(new Button(Integer.toString(b+1)));
            hBoxButtons.getChildren().addAll(buttonArr.get(b));
        }
        Button buttonClearTile = new Button("Clear") ;
        buttonArr.add(buttonClearTile);
        hBoxButtons.getChildren().addAll(buttonArr.get(gameSize));

        //Adding functionality to the array of numbered buttons and the clear tile button
        for (int b = 0; b <= gameSize; b++) {
            int buttonValue = b;
            buttonArr.get(b).setOnAction(actionEvent -> {
                //Clear button
                if (buttonValue == gameSize) {
                    takeAction("");

                }
                //Numbered buttons
                else {
                    takeAction(Integer.toString(buttonValue + 1));
                }
                try {
                    showProgress();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                createBoard();
            });
        }

        //Mistake button
        buttonMistakes.setOnAction(actionEvent -> {
            progressChecker.toggleShowMistakes();
            if (progressChecker.showingMistakes()){
                System.out.println("Showing mistakes");
                progressChecker.findErrors();
            }
            else {
                System.out.println("Not showing mistakes");
                for (Tile tile:tileArr) {
                    tile.setColour(1,1,1,1);
                    createBoard();
                }
            }
        });

        //Clear Board button
        buttonBoardClear.setOnAction(actionEvent -> {
            clearBoard();
        });

        //Undo Button
        buttonUndo.setDisable(true);
        buttonUndo.setOnAction(actionEvent -> {
            undo();
        });

        //Redo Button
        buttonRedo.setDisable(true);
        buttonRedo.setOnAction(actionEvent -> {
            redo();
        });

        ChoiceBox cb = new ChoiceBox();
        cb.getItems().add("Small");
        cb.getItems().add("Medium");
        cb.getItems().add("Large");
        cb.setValue(chosenTextSize);
        Label textSize = new Label("Text Size:");
        vBoxButtons.getChildren().add(textSize);
        vBoxButtons.getChildren().add(cb);

        cb.setOnAction(actionEvent -> {
            String chosenFont = (String) cb.getValue();
            if (chosenFont.equals("Small")) {
                fontSize = 18;
                chosenTextSize = "Small";
            }
            else if (chosenFont.equals("Medium")) {
                fontSize = 24;
                chosenTextSize = "Medium";
            }
            else if (chosenFont.equals("Large")) {
                fontSize = 36;
                chosenTextSize = "Large";
            }
            createBoard();
        });

        this.root = new BorderPane();

        BorderPane.setAlignment(vBoxButtons, Pos.CENTER_RIGHT);
        BorderPane.setAlignment(boardPane, Pos.CENTER);
        BorderPane.setAlignment(hBoxButtons, Pos.BOTTOM_CENTER);
        this.root.setRight(vBoxButtons);
        this.root.setCenter(boardPane);
        this.root.setBottom(hBoxButtons);

        AtomicReference<Scene> scene = new AtomicReference<>(new Scene(root));

        //Load Button
        buttonLoad.setOnAction(actionEvent -> {

            tileSize = 50;

            StackPane secondaryLayout = new StackPane();
            Scene secondScene = new Scene(secondaryLayout, 230, 100);

            Stage loadWindow = new Stage();
            loadWindow.setTitle("Load a puzzle");
            loadWindow.setScene(secondScene);

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select a .txt puzzle file");
            String dir = System.getProperty("user.dir");
            fileChooser.setInitialDirectory(new File(dir));
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Text Files", "*.txt"));

            File chosenFile = fileChooser.showOpenDialog(loadWindow);

            gridCreated = false;
            boardPane = new GridPane();

            root.getChildren().remove(boardPane);
            createGameWindow(chosenFile.getName());

            //puzzleConfigurator.getConfiguration(this);
        });

        //Mouse event gets mouse coordinates and converts it into grid coordinates.
        boardPane.setOnMouseClicked(mouseEvent -> {
            this.mouseX = roundDown(mouseEvent.getX());
            this.mouseY = roundDown(mouseEvent.getY());
            //If statement prevents creating new tiles outside the game-board if board is clicked at edges
            if (mouseX < gameSize && mouseY < gameSize) {
                this.selectedTile = tileArr.get(getTileIndex(mouseX, mouseY));
                System.out.println("X: " + mouseX + " Y: " + mouseY);
                System.out.println("Tile index (tile method): " + selectedTile.getIndex());
                System.out.println("Tile colour: "+(selectedTile.getRed()+","+selectedTile.getGreen()+","+selectedTile.getBlue()+","+selectedTile.getOpacity()));
                System.out.println("Tile Row-Correct: "+selectedTile.isRowCorrect()+" Tile Column-Correct: "+selectedTile.isColumnCorrect());
                System.out.println("Tile is Cage-Correct: "+selectedTile.isCageCorrect());
                System.out.println("Tile value history: " +selectedTile.getValueHistory());
                System.out.println("Associated Cage: " + selectedTile.getCage().getTarget()+selectedTile.getCage().getOperator());
                //System.out.println("Tile index (board method): " + getTileIndex(mouseX, mouseY));
                try {
                    System.out.println("Tile value: " + selectedTile.getValue());
                } catch (Exception e) {
                    System.out.println("Tile has no value");
                }
            }
        });
        //Updates selected tile with relevant value
        scene.get().setOnKeyPressed(keyEvent -> {
            //If statement prevents creating new tiles outside the game-board if board is clicked at edges
            if (mouseX < gameSize && mouseY < gameSize) {
                this.lastKeyEvent = keyEvent;
                keyPressed = keyEvent.getText();
                //Try-catch block catches events where an invalid character such as a symbol is entered
                try {
                    //If statement only allows valid values to be entered (that follow MathDoku rules)
                    if (keyEvent.getCode().equals(KeyCode.BACK_SPACE)) {
                        takeAction("");
                    }
                    else if (Integer.parseInt(keyPressed) > 0 && Integer.parseInt(keyPressed) <= gameSize) {
                        takeAction((keyPressed));
                    }
                }
                catch (Exception e) {
                    System.out.println("Invalid character entered (not from 1 - "+gameSize+" or backspace).");
                }
                try {
                    showProgress();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                createBoard();
            }
        });

        cb.setOnAction(actionEvent -> {
            String chosenFont = (String) cb.getValue();
            if (chosenFont.equals("Small")) {
                fontSize = 18;
            }
            else if (chosenFont.equals("Medium")) {
                fontSize = 24;
            }
            else if (chosenFont.equals("Large")) {
                fontSize = 36;
            }
            createBoard();
        });

        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            stage.heightProperty().addListener((obs1, oldVal1, newVal1) -> {
                resize();
            });
        });

        stage.setScene(scene.get());
        stage.show();
    }
}
