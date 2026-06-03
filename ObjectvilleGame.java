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

    static class ServiceBuilding extends Zone {
        int radius;
        ServiceBuilding(int row, int col, char type, int radius) {
            super(row, col, type);
            this.radius = radius;
        }
    }

    static class UtilityProvider extends Zone {
        int distribution;
        UtilityProvider(int row, int col, char type) {
            super(row, col, type);
            this.distribution = CAPACITY;
        }
    }

    private int rows, cols;
    private Zone[][] grid;

    private List<Zone> activeZones = new ArrayList<>();
    private List<ServiceBuilding> services = new ArrayList<>();
    private List<UtilityProvider> powerPlants = new ArrayList<>();
    private List<UtilityProvider> waterPumps = new ArrayList<>();
    private List<UtilityProvider> internetHubs = new ArrayList<>();

    private int totalPopulation = 0;
    private int totalGoods = 0;
    private int totalLifestyle = 0;

    private static final List<Character> UTILITY_ORDER = Arrays.asList(INTERNET, WATER, POWER);

    public ObjectvilleGame(String mapFile) throws IOException {
        loadMap(mapFile);
    }

    private String getZoneName(Zone zone) {
        if (zone.type == HOUSING) return "House";
        if (zone.type == COMMERCIAL) return "Commercial";
        if (zone.type == INDUSTRIAL) return "Industrial";
        return String.valueOf(zone.type);
    }

    private void loadMap(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filename));
        String line;
        rows = 0;

        List<String> mapLines = new ArrayList<>();
        while ((line = br.readLine()) != null) {
            mapLines.add(line);
            rows++;
        }
        br.close();

        if (rows == 0) return;
        cols = mapLines.get(0).length();

        grid = new Zone[rows][cols];

        for (int r = 0; r < rows; r++) {
            String mapRow = mapLines.get(r);
            for (int c = 0; c < cols; c++) {
                char cellChar = mapRow.charAt(c);

                Zone zone;
                if (cellChar == SCHOOL) {
                    zone = new ServiceBuilding(r, c, cellChar, SCHOOL_RADIUS);
                    services.add((ServiceBuilding) zone);
                } else if (cellChar == POLICE_STATION) {
                    zone = new ServiceBuilding(r, c, cellChar, POLICE_RADIUS);
                    services.add((ServiceBuilding) zone);
                } else if (cellChar == HOSPITAL) {
                    zone = new ServiceBuilding(r, c, cellChar, HOSPITAL_RADIUS);
                    services.add((ServiceBuilding) zone);
                } else if (cellChar == POWER) {
                    zone = new UtilityProvider(r, c, cellChar);
                    powerPlants.add((UtilityProvider) zone);
                } else if (cellChar == WATER) {
                    zone = new UtilityProvider(r, c, cellChar);
                    waterPumps.add((UtilityProvider) zone);
                } else if (cellChar == INTERNET) {
                    zone = new UtilityProvider(r, c, cellChar);
                    internetHubs.add((UtilityProvider) zone);
                } else {
                    zone = new Zone(r, c, cellChar);
                }

                grid[r][c] = zone;

                if (cellChar == HOUSING || cellChar == INDUSTRIAL || cellChar == COMMERCIAL) {
                    activeZones.add(zone);
                }
            }
        }
    }
}
