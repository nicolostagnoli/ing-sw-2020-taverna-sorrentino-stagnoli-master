package it.polimi.ingsw.model;

import it.polimi.ingsw.model.exceptions.PrepareModelException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;


public class TestUtils {

    public static GameModel prepareModel(String inputFile) throws PrepareModelException {

        //Load file
        InputStream inputStream = TestUtils.class.getClassLoader()
                .getResourceAsStream(inputFile);

        if (inputStream == null) {
            throw new PrepareModelException("Test file is absent");
        }

        Scanner scanner = new Scanner(inputStream);
        GameModel model;
        try {
            model = new GameModel();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
           throw new PrepareModelException("GameModel couldn't be created.");
        }
        Random random = new Random();

        //Read numPlayers and godUnderTest with correct skipping in stream
        int numPlayers = scanner.nextInt();
        try {
            scanner.skip("\r\n");
        }
        catch (NoSuchElementException e) {
            scanner.skip("\n");
        }
        String godUnderTest = scanner.nextLine();

        //Check that god exists (or is None)
        List<String> allGods = model.getAvailableGods().stream().map(God::getName)
                .collect(Collectors.toList());
        if (!godUnderTest.equals("None") && !allGods.contains(godUnderTest)) {
            throw new PrepareModelException("Trying to test a non-existing god.");
        }

        //Setting numPlayers and players
        model.setNumPlayers(numPlayers);
        for (int i = 0; i < numPlayers; i++) {
            Player p = new Player("Player" + (i+1));
            model.addNewPlayer(p);
        }

        //Setting used gods
        //Note: Challenger has the godUnderTest
        if (!godUnderTest.equals("None")) {
            List<String> chosenGods = new ArrayList<>();
            List<String> usableGods = Arrays.asList("Apollo", "Artemis", "Atlas");
                // These gods have no "side effects" that blocks particular moves, so they are
                // perfect to test godUnderTest without undesired effects
            usableGods = new ArrayList<>(usableGods);
            usableGods.remove(godUnderTest);
            for (int i = 1; i < numPlayers; i++) {
                chosenGods.add(usableGods.remove(random.nextInt(usableGods.size())));
            }
            chosenGods.add(godUnderTest);
            model.setGods(chosenGods);

            List<God> godsList = model.getAvailableGods();
            godsList.stream().filter(god -> !god.getName().equals(godUnderTest)).forEach(g ->
                    model.assignGodToPlayer(model.getCurrentPlayer(), g));
            God got = godsList.stream()
                    .filter(god -> god.getName().equals(godUnderTest))
                    .findFirst().orElseThrow();
            model.assignGodToPlayer(model.getCurrentPlayer(), got);
        } else {
            for (int i = 0; i < numPlayers; i++) {
                model.getCurrentPlayer().setGod(new God("None", "For test purposes only"));
                model.nextPlayer();
            }

            //Check it's challenger's turn (Players enumeration starts with 1)
            assert model.getCurrentPlayer().getNickname().equals("Player1");
        }

        //Setting challenger as startPlayer (to test immediately his powers)
        model.setStartPlayer(model.getCurrentPlayer());

        //Initializing workers on board
        for (int i = 0; i < numPlayers; i++) {
            String[] positions = scanner.nextLine().split(",");
            for (String pos : positions) {
                Coord c = Coord.convertStringToCoord(pos);
                model.initializeWorker(c);
            }
        }

        //Obtaining private model's board through reflection
        Field boardField;
        Board modelBoard;
        try {
            boardField = GameModel.class.getDeclaredField("board");
        }
        catch (NoSuchFieldException e) {
            throw new PrepareModelException("Couldn't find attribute \"board\" in GameModel");
        }
        boardField.setAccessible(true);
        try {
            modelBoard = (Board) boardField.get(model);
        }
        catch (IllegalAccessException e) {
            throw new PrepareModelException("Field \"board\" in GameModel is inaccessible");
        }

        //Obtaining private board's spaces through reflection
        Field spacesField;
        Space[][] boardSpaces;
        try {
            spacesField = Board.class.getDeclaredField("board");
        }
        catch (NoSuchFieldException e) {
            throw new PrepareModelException("Couldn't find attribute \"board\" in Board");
        }
        spacesField.setAccessible(true);
        try {
            boardSpaces = (Space[][]) spacesField.get(modelBoard);
        }
        catch (IllegalAccessException e) {
            throw new PrepareModelException("Field \"board\" in Board is inaccessible");
        }

        //Set board's spaces at desired level
        while (scanner.hasNext()) {
            String[] spaceLevel = scanner.nextLine().split(":");
            Coord c = Coord.convertStringToCoord(spaceLevel[0]);
            Level level = Level.valueOf(spaceLevel[1].equals("0") ? "GROUND" : "LVL"+spaceLevel[1]);
            boardSpaces[c.x][c.y].setLevel(level);
            if (spaceLevel.length == 3 && spaceLevel[2].equals("DOME")) {
                boardSpaces[c.x][c.y].setLevel(Level.DOME);
            }
        }

        //Check model's board is really as desired
        Board realBoard = model.getBoard();

        List<Space> realSpaces = realBoard.getAllCoord().stream()
                .map(realBoard::getSpace).collect(Collectors.toList());
        List<Space> desiredSpaces = Arrays.stream(boardSpaces).flatMap(Arrays::stream).collect(Collectors.toList());

        if (!realSpaces.equals(desiredSpaces)) {
            throw new PrepareModelException("Couldn't set spaces as desired.");
        }

        model.initRequestHandlers();
        model.changeState(new PlayerTurnState(model));
        return model;
    }

    /*public static void nextStep(GameModel model) {

        try {
            model.nextStep();
        }
        catch (RuntimeException e) {
            // Exception is thrown if there is no listeners for the player.
            // This is ok when testing the model, because Views do not exist.
        }

    }*/
}
