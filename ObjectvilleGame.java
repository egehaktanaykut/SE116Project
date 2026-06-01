import java.io.*;
import java.util.*;

public class ObjectvilleGame {
    private static final int CAPACITY = 100;

    private static final char EMPTY = 'E';
    private static final char ROAD = 'R';
    private static final char HOUSING = 'H';
    private static final char INDUSTRIAL = 'I';
    private static final char COMMERCIAL = 'C';
    private static final char POWER = 'P';
    private static final char WATER = 'W';
    private static final char INTERNET = 'T';
    private static final char SCHOOL = 'S';
    private static final char POLICE_STATION = 'F';
    private static final char HOSPITAL = 'D';

    private static final int SCHOOL_RADIUS = 4;
    private static final int POLICE_RADIUS = 5;
    private static final int HOSPITAL_RADIUS = 3;

    static class Zone {
        char type;
        int level = 0;
        int row, col;

        int electricity = 0;
        int water = 0;
        int internet = 0;

        boolean hasSecurity = false;
        boolean hasHealth = false;
        boolean hasEducation = false;

        int populationReceived = 0;
        int goodsReceived = 0;
        int lifestyleReceived = 0;

        int prevOutput = 0;

        Zone(int row, int col, char type) {
            this.row = row;
            this.col = col;
            this.type = type;
        }

        double euclideanDistance(Zone other) {
            double dRow = other.row - this.row;
            double dCol = other.col - this.col;
            return Math.sqrt(dRow * dRow + dCol * dCol);
        }

        String getKey() {
            return row + "," + col;
        }
    }
}