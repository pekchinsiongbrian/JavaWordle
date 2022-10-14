import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.stream.Stream;

public class LaunchWordle {
  public static void main(String[] args) {
    Dictionary dict = null;
    try {
      dict = new Dictionary();
    } catch (DictionaryInitializationException e) {
      System.err.println(e.getMessage());
      System.exit(0);
    }

    String[][] game = setupGame();
    assert dict instanceof Dictionary;
    String wotd = dict.getWotd();
    List<String> unusedLetters = new ArrayList<String>(
        Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"));
    displayGame(game, unusedLetters);

    Scanner sc = new Scanner(System.in);
    boolean gameOver = false;
    int guessNum = 1;
    while (!gameOver && guessNum <= 6) {
      System.out.println("Guess a 5 letter word (Attempt " + guessNum + "/6):");
      String guess = sc.next().toLowerCase();
      if (dict.isValidGuess(guess)) {
        Result res = compareWords(wotd, guess);
        game = updateGame(game, res.getNewLine(), guessNum);
        unusedLetters = updateUnusedLetters(guess, unusedLetters);
        displayGame(game, unusedLetters);
        gameOver = res.getGameOver();
        guessNum++;
      } else {
        System.out.println("Invalid input!");
      }
    }
    System.out.println(gameOver
        ? "Good job! You got the answer in " + (guessNum - 1) + " tries."
        : "Game over. You'll get the word next time! The word was: " + wotd);
  }

  public static String[][] updateGame(String[][] game, String[] newLine, int guessNum) {
    switch (guessNum) {
      case 1:
        for (int i = 0; i < 11; i++) {
          game[1][i] = newLine[i];
        }
        break;
      case 2:
        for (int i = 0; i < 11; i++) {
          game[3][i] = newLine[i];
        }
        break;
      case 3:
        for (int i = 0; i < 11; i++) {
          game[5][i] = newLine[i];
        }
        break;
      case 4:
        for (int i = 0; i < 11; i++) {
          game[7][i] = newLine[i];
        }
        break;
      case 5:
        for (int i = 0; i < 11; i++) {
          game[9][i] = newLine[i];
        }
        break;
      case 6:
        for (int i = 0; i < 11; i++) {
          game[11][i] = newLine[i];
        }
        break;
      default:
        break;
    }
    return game;
  }

  public static Result compareWords(String wotd, String guess) {
    ArrayList<Integer> idxToIgnore = new ArrayList<Integer>();
    String[] res = "| | | | | |".split("");
    Result resultObj = new Result();

    // first pass: check for same position and "lock them in" with green highlights
    for (int i = 0; i < 5; i++) {
      if (wotd.charAt(i) == guess.charAt(i)) {
        String currChar = Character.toString(guess.charAt(i));
        String greenChar = BgColours.makeGreen(currChar);
        res[i * 2 + 1] = greenChar;
        idxToIgnore.add(i);
      }
    }
    if (idxToIgnore.size() == 5) {
      resultObj.setNewLine(res);
      resultObj.setGameOver(true);
      return resultObj;
    }

    // second pass: check for contains. Max number of yellow highlights in user's
    // guess = max
    HashMap<String, Integer> wotdCharCount = new HashMap<String, Integer>();
    for (int i = 0; i < 5; i++) {
      if (!idxToIgnore.contains(i)) {
        String currChar = Character.toString(wotd.charAt(i));
        int currCharCount = wotdCharCount.containsKey(currChar) ? wotdCharCount.get(currChar) : 0;
        wotdCharCount.put(currChar, currCharCount + 1);
      }
    }
    for (int i = 0; i < 5; i++) {
      if (!idxToIgnore.contains(i)) {
        String currChar = Character.toString(guess.charAt(i));
        String redChar = BgColours.makeRed(currChar);
        String yellowChar = BgColours.makeYellow(currChar);
        if (!wotdCharCount.containsKey(currChar)) {
          res[i * 2 + 1] = redChar;
        } else {
          int numCharsLeft = wotdCharCount.get(currChar);
          if (numCharsLeft == 0) {
            res[i * 2 + 1] = redChar;
          } else {
            res[i * 2 + 1] = yellowChar;
            numCharsLeft--;
            wotdCharCount.replace(currChar, numCharsLeft);
          }
        }
      }
    }
    resultObj.setNewLine(res);
    return resultObj;
  }

  public static List<String> updateUnusedLetters(String guess, List<String> unusedLetters) {
    for (int i = 0; i < guess.length(); i++) {
      unusedLetters.remove(Character.toString(guess.charAt(i)).toUpperCase());
    }
    return unusedLetters;
  }

  public static String[][] setupGame() {
    String[] inputRow = "| | | | | |".split(""); // len=11
    String[] intersectionRow = "+-+-+-+-+-+".split(""); // len=11
    String[][] game = new String[13][11];
    for (int i = 0; i < 13; i++) {
      for (int j = 0; j < 11; j++) {
        if (i % 2 == 0) {
          game[i][j] = intersectionRow[j];
        } else {
          game[i][j] = inputRow[j];
        }
      }
    }
    return game;
  }

  public static void displayGame(String[][] game, List<String> unusedLetters) {
    for (String[] row : game) {
      for (String s : row) {
        System.out.print(s);
      }
      System.out.println();
    }
    System.out.println("Letters not used yet: " + unusedLetters);
  }
}

class BgColours {
  public static final String RESET = "\033[0m"; // Text reset
  public static final String RED_BACKGROUND = "\033[41m"; // RED
  public static final String GREEN_BACKGROUND = "\033[42m"; // GREEN
  public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW

  public static String makeGreen(String text) {
    return GREEN_BACKGROUND + text + RESET;
  }

  public static String makeYellow(String text) {
    return YELLOW_BACKGROUND + text + RESET;
  }

  public static String makeRed(String text) {
    return RED_BACKGROUND + text + RESET;
  }
}

class Dictionary {
  private HashSet<String> validDict = new HashSet<String>();
  private HashSet<String> overallDict = new HashSet<String>();

  public Dictionary() throws DictionaryInitializationException {
    // guessable, wotd: 2315
    try (Stream<String> stream = Files.lines(Paths.get("./guessableWotd.txt"))) {
      stream.forEach(x -> {
        validDict.add(x);
        overallDict.add(x);
      });
    } catch (IOException e) {
      throw new DictionaryInitializationException("Error initializing dictionary");
    }

    // guessable, never wotd: 10657
    try (Stream<String> stream = Files.lines(Paths.get("./guessableNeverWotd.txt"))) {
      stream.forEach(x -> {
        overallDict.add(x);
      });
    } catch (IOException e) {
      throw new DictionaryInitializationException("Error initializing dictionary");
    }
  }

  public String getWotd() {
    Random random = new Random();
    int randInt = random.nextInt(validDict.size());
    int i = 0;
    for (String word : validDict) {
      if (i == randInt)
        return word;
      i++;
    }
    return ""; // this will never execute
  }

  public boolean isValidGuess(String guess) {
    if (guess.length() != 5) {
      return false;
    }
    if (!overallDict.contains(guess)) {
      return false;
    }
    return true;
  }
}

class Result {
  private String[] newLine;
  private boolean gameOver;

  public Result() {
    this.newLine = null;
    this.gameOver = false;
  }

  public void setNewLine(String[] newLine) {
    this.newLine = newLine;
  }

  public void setGameOver(boolean gameOver) {
    this.gameOver = gameOver;
  }

  public String[] getNewLine() {
    return this.newLine;
  }

  public boolean getGameOver() {
    return this.gameOver;
  }
}

class DictionaryInitializationException extends IOException {
  public DictionaryInitializationException(String message) {
    super(message);
  }
}
