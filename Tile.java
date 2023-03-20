import java.util.ArrayList;

public class Tile {
    Integer value;
    int x;
    int y;
    int index;
    Cage cage;
    Double[] colour = {1.0,1.0,1.0,1.0};
    ArrayList<Integer> valueHistory = new ArrayList<>();
    boolean isRowCorrect = true;
    boolean isColumnCorrect = true;
    boolean isCageCorrect = true;

    double[] tileCorners;

    boolean topRightCorner;
    boolean topLeftCorner;
    boolean bottomRightCorner;
    boolean bottomLeftCorner;

    public Tile (int x, int y, int index, Integer value) {
        this.value = value;
        this.x = x;
        this.y = y;
        this.index = index;
        this.valueHistory.add(value);

        double topLeftX = getX();
        double topLeftY = getY();
        double topRightX = (double) getX()+1;
        double topRightY = getY();
        double bottomLeftX = getX();
        double bottomLeftY = (double) getY()+1;
        double bottomRightX = (double) getX()+1;
        double bottomRightY = (double) getY()+1;

        this.tileCorners = new double[]{topLeftX, topLeftY, topRightX, topRightY,bottomLeftX,bottomLeftY,bottomRightX,bottomRightY};
    }

    public Tile (int index) {
        this.value = null;
    }

    public Integer getValue(){
        return value;
    }

    public void setValue(Integer newValue){
       this.value = newValue;
       this.valueHistory.add(newValue);
    }

    public void setValue(Integer newValue, boolean isUndo){
        this.value = newValue;
        if (!isUndo) {
            this.valueHistory.add(newValue);
        }
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getIndex() {
        return index;
    }

    public void setColour(double red, double green, double blue, double opacity) {
        this.colour[0] = red;
        this.colour[1] = green;
        this.colour[2] = blue;
        this.colour[3] = opacity;
    }

    public Double[] getColour() {
        return colour;
    }

    public double getRed() {
        return colour[0];
    }

    public double getGreen() {
        return colour[1];
    }

    public double getBlue() {
        return colour[2];
    }
    public double getOpacity() {
        return colour[3];
    }

    public void setRowCorrect(boolean correct) {
        this.isRowCorrect = correct;
    }

    public boolean isRowCorrect (){
        return isRowCorrect;
    }

    public void setColumnCorrect(boolean correct) {
        this.isColumnCorrect = correct;
    }

    public boolean isColumnCorrect() {
        return isColumnCorrect;
    }

    public void setCageCorrect(boolean correct) {
        this.isCageCorrect = correct;
    }

    public boolean isCageCorrect() {
        return isCageCorrect;
    }

    public void assignCage(Cage cage) {
        this.cage = cage;
    }

    public Cage getCage() {
        return cage;
    }

    public ArrayList<Integer> getValueHistory() {
        return valueHistory;
    }

    public void removeLastHistory() {
        int lastInHistory = valueHistory.size()-1;
        this.valueHistory.remove(lastInHistory);
    }

    public void resetHistory() {
        this.valueHistory = new ArrayList<Integer>();
        valueHistory.add(null);
    }
}
