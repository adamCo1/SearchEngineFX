package ReadFromWeb;

/**
 * represents the city with all the requested information
 *
 */
public class City {
    private String name;
    private String country;
    private String currency;
    private String population;
    private int numberOfWordsInName;

    public City(String name,String country, String currency, String population,int numberOfWordsInName) {
        this.name = name;
        this.country = country;
        this.currency = currency;
        this.population = population;
        this.numberOfWordsInName = numberOfWordsInName;
    }

    public String getCountry() {
        return country;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPopulation() {
        return population;
    }

    public String getName() {
        return name;
    }

    public int getNumberOfWordsInName() {
        return numberOfWordsInName;
    }

    @Override
    public String toString() {
        return "City{" +
                "name='" + name + '\'' +
                ", country='" + country + '\'' +
                ", currency='" + currency + '\'' +
                ", population='" + population + '\'' +
                '}';
    }
}