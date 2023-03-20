import java.io.*;
import java.util.ArrayList;

public class PuzzleConfigurator {
    BufferedReader reader;
    String filename;
    String cageGoal;
    int cageTarget;
    char cageOperator;
    ArrayList<Tile> allTiles = new ArrayList<>();


    String dir = System.getProperty("user.dir");

    public PuzzleConfigurator(String filename) {
        this.filename = filename;
        try {
            reader = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public String getLine() {
        //Try-catch block in case there is no file present to read lines from. Relevant error provided.
        try {
            return reader.readLine();
        } catch (Exception e) {
            System.out.println("ERROR in readLine: File likely not found.");
            return "ERROR";
        }
    }

    public String getFilename(){
        return filename;
    }

    public boolean fileIsReady() {
        //Try-catch block in case there is no file present to read lines from. Relevant error provided.
        try {
            if (reader != null) {
                return reader.ready();
            } else {
                return false;
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public int getGameSize() {
        PuzzleConfigurator puzzleConfigurator = new PuzzleConfigurator(filename);
        int tileCount = 0;
        while (puzzleConfigurator.fileIsReady()) {
            String[] lineArray = puzzleConfigurator.getLine().split(" ");
            String[] tileIDs = lineArray[1].split(",");

            //Counts the total number of tiles
            for (int k = 0; k < tileIDs.length; k++) {
                tileCount++;
            }

        }
        return (int) Math.sqrt(tileCount);
    }

    public void getConfiguration(Board board) {
        PuzzleConfigurator puzzleConfigurator = new PuzzleConfigurator(filename);

        //Creates a new blank board of the set game size
        board.setGameSize(getGameSize());
        board.createBoard(getGameSize());

        //For each line in the txt file
        while (puzzleConfigurator.fileIsReady()) {

            String[] lineArray = puzzleConfigurator.getLine().split(" ");

            cageGoal = lineArray[0];
            char[] cageGoalArray = cageGoal.toCharArray();

            int numbersBeforeOperator = 0;
            for (int i = 0; i < cageGoal.length(); i++) {
                try {
                    int testInt = Integer.parseInt(String.valueOf(cageGoal.toCharArray()[i]));
                } catch (Exception e) {
                    break;
                }
                numbersBeforeOperator++;
            }

            StringBuilder sb = new StringBuilder();

            for (int j = 0; j < numbersBeforeOperator; j++) {
                sb.append(cageGoalArray[j]);
            }
            cageTarget = Integer.parseInt(sb.toString());
            if (numbersBeforeOperator >= cageGoalArray.length) {
                cageOperator = ' ';
            }
            else {
                cageOperator = cageGoalArray[numbersBeforeOperator];
            }

            String[] tileIDs = lineArray[1].split(",");

            ArrayList<Tile> tileArrayList = new ArrayList<>();
            for (String tileID : tileIDs) {
                tileArrayList.add(board.getTile(Integer.parseInt(tileID)-1));
            }
            board.createCage(cageTarget, String.valueOf(cageOperator), tileArrayList.toArray(new Tile[0]));
        }

        board.setGameSize(getGameSize());
    }

    public ArrayList<String> getFileNames() {
        ArrayList<String> fileNames = new ArrayList<>();

        File directory = new File(dir);
        File[] directoryFiles = directory.listFiles();
        for (File file:directoryFiles) {
            String filename = file.getName();
            if (filename.contains(".txt")) {
                String fileExt = filename.split(".")[1];
                if (fileExt.equals("txt")) {
                    fileNames.add(filename.split(".")[0]);
                }
            }
        }
        return fileNames;
    }
}
