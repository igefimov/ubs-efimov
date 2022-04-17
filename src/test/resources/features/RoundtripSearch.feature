@kayak
Feature: Round trip flight search

  Scenario Outline: Round trip flight below certain price
    Given Valid parameters are provided <originAirport> <destinationAirport> <departureDate> <returnDate> <maxPrice>
    When  User navigates to the web site
    Then  Roundtrip flights below price <maxPrice> are displayed
    Examples:
      | originAirport | destinationAirport | departureDate | returnDate   | maxPrice |
      | "ZRH"         | "VIE"              | "2022-07-20"  | "2022-08-19" | 900      |
      | "KIV"         | "VIE"              | "2022-09-11"  | "2022-09-11" | 750      |
      | "MOS"         | "SFO"              | "2022-06-10"  | "2022-06-17" | 800      |
      | "KIV"         | "SFO"              | "2022-06-10"  | "2022-06-17" | 50       |