## Team06

# Crazy Putting - KEN1600 Project 1.2

A golf putting simulator with ODE solvers and machine learning, built for the Bachelor Data Science and Artificial Intelligence programme at Maastricht University.

---

## Prerequisites

- Java JDK 21 or higher —
- Maven 3.8 or higher — 


To verify your installations, run:
```
java -version
mvn -version
```

---

## Project Structure
```
golfgame/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/ken06/solvers/
                ├── ODEFunction.java
                ├── ODESolver.java
                ├── SolverResult.java
                └── RK4Solver.java
```

---

## How to Run

**1. Clone or download the project**
```
git clone <https://gitlab.maastrichtuniversity.nl/project-1-2-25-26/dsai/team_06>
cd <project_folder>
git checkout Wail
git checkout -b YourBranch
```

**2. Compile the project**
```
mvn clean compile
```

**3. Run the solver demo**
You can click the run (start button) in the RK4Solver class
```
mvn exec:java -Dexec.mainClass="com.ken06.solvers.RK4Solver"
```

---


```



---

## Authors

- 

---

## Course
KEN1600 - Bachelor Year 1, Period 1.4/1.5/1.6
Academic year 2025-2026
Maastricht University









