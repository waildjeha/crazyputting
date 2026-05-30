## Getting Started

Welcome to Phase 2 of the Crazy Putting simulator! This JavaFX-based application is a physics-driven golf game where you must navigate sloped terrains, avoid water and tree hazards, and deal with different surface frictions (grass and sand) to sink your ball into the target hole.

---

## Folder Structure

The workspace contains two folders by default, where:

- `vscode`: the folder to launch the code via VSCode
- `src`: the folder to maintain sources
- `lib`: the folder to maintain dependencies
- `images`: the folder to stock texture images for the golf course

Meanwhile, the compiled output files will be generated in the `bin` folder by default.

> If you want to customize the folder structure, open `.vscode/settings.json` and update the related settings there.

---

## Features
* **Custom Physics Engine:** Accurately models gravity, kinetic/static friction, and terrain slopes using RK4 and Euler ODE Solvers.
* **Dynamic JavaFX GUI:** Renders a 3D-shaded top-down view of the course, automatically tracking the ball and panning the camera when necessary.
* **Machine Learning Bot:** Includes a Newton-Raphson-based AI bot (`MachineBot.java`) that can calculate the exact optimal initial velocity to score a hole-in-one (adhering to the 5.0 m/s game limit).
* **Custom Course Loader:** Create your own complex courses using simple `.txt` files, complete with custom mathematical height functions, sand pits, and tree obstacles.

---

## Prerequisites
* **Java Development Kit (JDK):** Version 11 or higher.
* **JavaFX SDK:** Required to run the graphical interface.

---

## How to Launch the Simulator

Since this project uses JavaFX, you must link the JavaFX library before compiling and running the code.

### Step 1: Configure your IDE (VS Code)
If you are using Visual Studio Code, you need to update your `launch.json` file to point to your local JavaFX SDK path.
1. Open the `.vscode/launch.json` file in your project.
2. Add the `vmArgs` property to your run configuration, pointing it to the `lib` folder of your downloaded JavaFX SDK.
   
It should look something like this:

```json
{
    "type": "java",
    "name": "Run Main",
    "request": "launch",
    "mainClass": "Main",
    "vmArgs": "--module-path \\"C:/path/to/your/javafx-sdk/lib\\" --add-modules javafx.controls,javafx.fxml"
}
```

### Step 2: Launch Main Class 
When your `launch.json` file is ready, run the class called `Main` in the `src` folder.

---

## How to create a Golf course via a Text file
If the user want to load a golf course via a .txt file, they can press the button `Choose a text file` and select it. But the .txt file have to follow a certain structure.

It should look something like this :

```txt
# --- General Course Parameters (Required) ---
# The height function of the terrain z = h(x, y)
h = 0.25*sin((x+y)/10)+1

# Grass friction coefficients
muK = 0.08
muS = 0.15

# Starting position of the ball
x0 = 7.0
y0 = 8.0

# Target hole position and radius
xt = 14.0
yt = 1.0
r = 0.1

# --- Optional Obstacles ---

# Sand friction (must be higher than grass friction)
sandMuK = 0.2
sandMuS = 0.3

# Sand Pit Boundaries (Format: xMin, xMax, yMin, yMax)
sand = 5.0, 8.0, 2.0, 4.0

# Trees (Format: x, y, radius)
# You can have multiple trees by adding more lines!
tree = 10.0, 5.0, 0.5
tree = 12.0, 3.0, 0.6
```


