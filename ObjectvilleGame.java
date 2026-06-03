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
    public void runSimulation(int ticks) {
        for (int tick = 1; tick <= ticks; tick++) {
            System.out.println("Tick " + tick);

            provideServices();
            distributeUtilities();

            if (tick > 1) {
                distributeResources();
            }

            updateZones(tick);
            accumulateProduction();
        }
    }

    private void provideServices() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].hasSecurity = false;
                grid[r][c].hasHealth = false;
                grid[r][c].hasEducation = false;
            }
        }

        for (ServiceBuilding service : services) {
            for (Zone zone : activeZones) {
                if (service.euclideanDistance(zone) <= service.radius) {
                    if (service.type == SCHOOL) {
                        zone.hasEducation = true;
                        if (zone.type == HOUSING) {
                            System.out.println(getZoneName(zone) + " at (" + zone.row + "," + zone.col + ") received education service");
                        }
                    } else if (service.type == POLICE_STATION) {
                        zone.hasSecurity = true;
                        System.out.println(getZoneName(zone) + " at (" + zone.row + "," + zone.col + ") received security service");
                    } else if (service.type == HOSPITAL) {
                        zone.hasHealth = true;
                        if (zone.type == HOUSING) {
                            System.out.println(getZoneName(zone) + " at (" + zone.row + "," + zone.col + ") received health service");
                        }
                    }
                }
            }
        }
    }
    private void distributeUtilities() {
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                grid[r][c].electricity = 0;
                grid[r][c].water = 0;
                grid[r][c].internet = 0;
            }
        }

        for (char utility : UTILITY_ORDER) {
            List<UtilityProvider> providers;
            if (utility == INTERNET) providers = internetHubs;
            else if (utility == WATER) providers = waterPumps;
            else providers = powerPlants;

            for (UtilityProvider provider : providers) {
                int remainingCapacity = CAPACITY;
                Set<String> visited = new HashSet<>();
                Queue<Zone> queue = new LinkedList<>();

                Zone startNode = grid[provider.row][provider.col];
                queue.add(startNode);
                visited.add(startNode.getKey());

                int[] dr = {-1, 1, 0, 0};
                int[] dc = {0, 0, -1, 1};

                while (!queue.isEmpty() && remainingCapacity > 0) {
                    Zone current = queue.poll();
                    int demand = calculateDemand(current, utility);
                    int alreadyReceived = 0;

                    if (utility == INTERNET) alreadyReceived = current.internet;
                    else if (utility == WATER) alreadyReceived = current.water;
                    else alreadyReceived = current.electricity;

                    int actualDemand = Math.max(0, demand - alreadyReceived);

                    if (actualDemand > 0) {
                        int deliver = Math.min(actualDemand, remainingCapacity);

                        if (utility == INTERNET) {
                            current.internet += deliver;
                        } else if (utility == WATER) {
                            current.water += deliver;
                        } else {
                            current.electricity += deliver;
                        }

                        remainingCapacity -= deliver;

                        if (current.type == HOUSING || current.type == COMMERCIAL || current.type == INDUSTRIAL) {
                            String utilityName = (utility == INTERNET) ? "internet" : (utility == WATER) ? "water" : "electricity";
                            System.out.println(getZoneName(current) + " at (" + current.row + "," + current.col + ") received " + deliver + " " + utilityName);
                        }
                    }

                    if (remainingCapacity <= 0) break;

                    for (int i = 0; i < 4; i++) {
                        int nr = current.row + dr[i];
                        int nc = current.col + dc[i];
                        if (nr >= 0 && nr < rows && nc >= 0 && nc < cols) {
                            Zone neighbor = grid[nr][nc];
                            String key = neighbor.getKey();
                            if (!visited.contains(key) && isRoadOrZone(neighbor)) {
                                visited.add(key);
                                queue.add(neighbor);
                            }
                        }
                    }
                }
            }
        }
    }
    private boolean isRoadOrZone(Zone zone) {
        return zone.type == ROAD || zone.type == HOUSING ||
                zone.type == INDUSTRIAL || zone.type == COMMERCIAL;
    }

    private int calculateDemand(Zone zone, char utility) {
        if (zone.type == ROAD || zone.type == EMPTY ||
                zone.type == SCHOOL || zone.type == POLICE_STATION ||
                zone.type == HOSPITAL || zone.type == POWER ||
                zone.type == WATER || zone.type == INTERNET) {
            return 0;
        }
        if (zone.type == INDUSTRIAL && utility == INTERNET) {
            return 0;
        }
        return Math.max(1, zone.prevOutput);
    }
}
