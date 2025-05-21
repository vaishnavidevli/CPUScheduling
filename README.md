# CPU Scheduling Simulator

This is a Java Swing desktop application that simulates CPU scheduling algorithms.  
It allows users to select one of several common scheduling algorithms, specify the number of processes, provide a source file for input data, and run a simulation that demonstrates how processes are scheduled by the CPU.

## Features

- Supports multiple CPU scheduling algorithms:
  - First-Come, First-Served (FCFS)
  - Round Robin
  - Shortest Job First (SJF)
  - Priority Scheduling
- Simple and user-friendly GUI interface
- Random generation of processes with arrival and burst times
- File input for simulation parameters
- Multithreaded simulation execution for responsive UI and concurrent processing
- Error handling for invalid inputs

## How to Use

1. Run the application by executing the `SchedulingGUI` class.
2. Select the desired scheduling algorithm from the dropdown menu.
3. Enter the number of processes (0 to 10).
4. Click the **Browse** button to select the input source file.
5. Click **Start** to begin the simulation.
6. The simulation runs on a separate thread to keep the UI responsive and displays results based on the selected algorithm and input.

---

Feel free to explore and extend this project by adding more algorithms, improving the GUI, or enhancing the simulation visuals.
