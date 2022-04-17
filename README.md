UBS-Efimov PROJECT

Create Web UI test which proves that https://www.kayak.ch/ always return a roundtrip flight below certain price. The test should take following parameters:

- From and To airports
- Date range
- Max price

Prerequisites
- macOS
- JDK 1.8 or above (Verify runnning "java -version") 
- Gradle 7.2 (Verify running "mvn -version")
- Firefox 99.0.1

Running the tests
- Go to the project directory and run "gradle clean test" 

Author
- Igor Efimov

Acknowledgments
- Tested on Ubuntu macOS Big Sur
- Binary geckodriver is part of the VCS repo
- No multi city support
