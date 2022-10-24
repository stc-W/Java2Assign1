import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * .
 */
public class MovieAnalyzer {
  private String datasetPath;
  ArrayList<String[]> movieList = new ArrayList<>();
  private String[] movie;
  private BufferedReader moCsv;
  private String line;

  /** .
   *

   * @param datasetPath .
   * @throws IOException .
   */

  public MovieAnalyzer(String datasetPath) throws IOException {
    this.datasetPath = datasetPath;
    FileInputStream fileInputStream = new FileInputStream(datasetPath);
    InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
    moCsv = new BufferedReader(inputStreamReader);
    moCsv.readLine();
    while ((line = moCsv.readLine()) != null) {
      movie = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)");
      for (int k = 0; k < movie.length; k++) {
        if (movie[k].contains(",")) {
          movie[k] = movie[k].substring(1, movie[k].length() - 1);
        }
      }
      movieList.add(movie);
    }
  }

  /** .
   *

   * @return Treemap
   */
  public Map<Integer, Integer> getMovieCountByYear() {
    Map<Integer, Integer> initialMap = movieList.stream().map(s -> Integer.valueOf(s[2]))
            .collect(Collectors.groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));
    Map<Integer, Integer> soredByYearMap = new TreeMap<>((i1, i2) -> i2 - i1);
    soredByYearMap.putAll(initialMap);
    return soredByYearMap;
  }

  /**
   * .

   * @return LinkedHashMap
   */
  public Map<String, Integer> getMovieCountByGenre() {
    String s1;
    String[] s2;
    List<String> genreList = new ArrayList<>();
    for (int k = 0; k < movieList.size(); k++) {
      s1 = movieList.get(k)[5].replace("\"", "");
      s2 = s1.split(", ");
      for (int i = 0; i < s2.length; i++) {
        genreList.add(s2[i]);
      }
    }
    Map<String, Integer> initialMap = genreList.stream()
            .collect(Collectors
                    .groupingBy(Function.identity(), Collectors.summingInt(e -> 1)));
    Map<String, Integer> soredByCountMap = new LinkedHashMap<>();

    initialMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .forEachOrdered(s -> soredByCountMap.put(s.getKey(), s.getValue()));
    return soredByCountMap;
  }

  /**
   * .

   * @return Map
   */
  public Map<List<String>, Integer> getCoStarCount() {
    Map<List<String>, Integer> initialMap = new HashMap<>();
    for (int k = 0; k < movieList.size(); k++) {
      for (int i = 10; i < 13; i++) {
        if (movieList.get(k).length <= i || movieList.get(k)[i].equals("")) {
          continue;
        }
        for (int c = i + 1; c < 14; c++) {
          if (movieList.get(k).length <= c || movieList.get(k)[c].equals("")) {
            continue;
          }
          List<String> list = new ArrayList<>(2);
          if (movieList.get(k)[i].compareTo(movieList.get(k)[c]) <= 0) {
            list.add(movieList.get(k)[i]);
            list.add(movieList.get(k)[c]);
          } else {
            list.add(movieList.get(k)[c]);
            list.add(movieList.get(k)[i]);
          }
          if (initialMap.containsKey(list)) {
            initialMap.put(list, initialMap.get(list) + 1);
          } else {
            initialMap.put(list, 1);
          }
        }
      }
    }
    return initialMap;
  }

  @Override
  public String toString() {
    String result = "[";
    for (int k = 0; k < movieList.size(); k++) {
      String current = "[";
      for (int i = 0; i < movieList.get(k).length - 1; i++) {
        current = current + movieList.get(k)[i] + ",";
      }
      current = current +  movieList.get(k)[movieList.get(k).length - 1] + "]";
      result = result + current;
    }
    result = result + "]";
    return result;
  }

  /**
   * .

   * @param top_k int
   * @param by String
   * @return List
   */
  public List<String> getTopMovies(int top_k, String by) {
    List<String> getTopMovies = new ArrayList<>(top_k);
    switch (by) {
      case "runtime": {
        Map<String, Integer> initialMap = new IdentityHashMap<>();
        for (int i = 0; i < movieList.size(); i++) {
          if (movieList.get(i).length <= 4 || movieList.get(i)[4].equals("")) {
            continue;
          }
          initialMap.put(movieList.get(i)[1], Integer.valueOf(movieList.get(i)[4]
                  .substring(0, movieList.get(i)[4].length() - 4)));
        }
        initialMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(top_k)
                .forEachOrdered(s -> getTopMovies.add(s.getKey()));
        break;
      }
      case "overview": {
        Map<String, String> initialMap = new HashMap<>();
        for (int i = 0; i < movieList.size(); i++) {
          if (movieList.get(i).length <= 7 || movieList.get(i)[7].equals("")) {
            continue;
          }
          initialMap.put(movieList.get(i)[7], movieList.get(i)[1]);
        }
        initialMap.entrySet().stream().sorted(Map.Entry.comparingByValue())
                .sorted(Map.Entry
                        .comparingByKey((String s1, String s2) -> s2.length() - s1.length()))
                .limit(top_k)
                .forEachOrdered(s -> getTopMovies.add(s.getValue()));
        break;
      }
      default: {
        break;
      }
    }
    return getTopMovies;
  }

  /**
   * .

   * @param top_k .
   * @param by .
   * @return List
   */
  public List<String> getTopStars(int top_k, String by) {
    List<String> getTopStars = new ArrayList<>(top_k);
    Map<String, Double> numberOfMovies = new HashMap<>();
    switch (by) {
      case "rating": {
        Map<String, Double> initialMap = new HashMap<>();
        for (int k = 0; k < movieList.size(); k++) {
          if (movieList.get(k).length <= 6 || movieList.get(k)[6].equals("")) {
            continue;
          }
          for (int i = 10; i < 14; i++) {
            if (initialMap.containsKey(movieList.get(k)[i])) {
              initialMap.put(movieList.get(k)[i], initialMap
                      .get(movieList.get(k)[i]) + Float.valueOf(movieList.get(k)[6]));
              numberOfMovies.put(movieList.get(k)[i], numberOfMovies
                      .get(movieList.get(k)[i]) + 1);
            } else {
              initialMap.put(movieList.get(k)[i],
                      Double.valueOf(Float.valueOf(movieList.get(k)[6])));
              numberOfMovies.put(movieList.get(k)[i], 1.0);
            }
          }
        }
        for (Map.Entry<String, Double> entry : numberOfMovies.entrySet()) {
          initialMap.put(entry.getKey(), initialMap.get(entry.getKey()) / entry.getValue());
        }
        initialMap.entrySet().stream().sorted(Map.Entry
                        .comparingByKey()).sorted(Map
                        .Entry.comparingByValue(Comparator.reverseOrder())).limit(top_k)
                .forEachOrdered(s -> getTopStars.add(s.getKey()));
        break;
      }
      case "gross": {
        Map<String, Double> initialMap = new HashMap<>();
        for (int k = 0; k < movieList.size(); k++) {
          if (movieList.get(k).length <= 15) {
            continue;
          }
          String temp = movieList.get(k)[15].replace(",", "");
          for (int i = 10; i < 14; i++) {
            if (initialMap.containsKey(movieList.get(k)[i])) {
              initialMap.put(movieList.get(k)[i], (initialMap
                      .get(movieList.get(k)[i]) + Double.valueOf(temp)));
              numberOfMovies.put(movieList.get(k)[i],
                      numberOfMovies.get(movieList.get(k)[i]) + 1);
            } else {
              initialMap.put(movieList.get(k)[i], Double.valueOf(temp));
              numberOfMovies.put(movieList.get(k)[i], 1.0);
            }
          }
        }
        for (Map.Entry<String, Double> entry : numberOfMovies.entrySet()) {
          initialMap.put(entry.getKey(), initialMap.get(entry.getKey()) / entry.getValue());
        }
        initialMap.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())).limit(top_k)
                .forEachOrdered(s -> getTopStars.add(s.getKey()));
        break;
      }
      default: {
        break;
      }
    }
    return getTopStars;
  }

  /**
  * .

  * @param genre .
  * @param min_rating .
  * @param max_runtime .
  * @return List
  */
  public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
    List<String> searchMovieList = new ArrayList<>();
    for (int k = 0; k < movieList.size(); k++) {
      if (movieList.get(k).length <= 6 || movieList.get(k)[4]
              .equals("") || movieList.get(k)[5].equals("") || movieList
              .get(k)[6].equals("")) {
        continue;
      }
      double rating = Double.parseDouble(movieList.get(k)[6]);
      int runtime = Integer.parseInt(movieList.get(k)[4]
              .substring(0, movieList.get(k)[4].length() - 4));
      if (movieList.get(k)[5].contains(genre) && rating >= min_rating
              && runtime <= max_runtime) {
        searchMovieList.add(movieList.get(k)[1]);
      }
      Collections.sort(searchMovieList);
    }
    return searchMovieList;
  }
}
