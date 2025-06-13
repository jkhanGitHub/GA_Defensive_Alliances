import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class PythonCaller {
    static void callPythonVisualization(String csvPath) {
        try {
            // 1. Find Python installation using registry
            Map<String, String> pythonPaths = findPythonPaths();
            System.out.println("Found Python installations: " + pythonPaths);

            // 2. Get absolute path to script
            String scriptPath = new File("visualize_ga.py").getPath();

            // 3. Try different Python paths in priority order
            String[] candidates = {
                    pythonPaths.getOrDefault("InstallPath", "") + "\\python.exe",
                    "C:\\Python312\\python.exe",
                    "C:\\Python311\\python.exe",
                    "C:\\Program Files\\Python312\\python.exe",
                    "C:\\Program Files\\Python311\\python.exe",
                    System.getenv("LOCALAPPDATA") + "\\Programs\\Python\\Python312\\python.exe",
                    System.getenv("LOCALAPPDATA") + "\\Programs\\Python\\Python311\\python.exe"
            };

            // 4. Execute with fallbacks
            boolean success = false;
            for (String pythonExe : candidates) {
                if (new File(pythonExe).exists()) {
                    System.out.println("Trying Python at: " + pythonExe);
                    success = runPython(pythonExe, scriptPath, csvPath);
                    if (success) return;
                }
            }

            // 5. Final fallback to py launcher
            System.out.println("Trying py launcher as last resort");
            success = runPython("py", scriptPath, csvPath);
            if (!success) {
                throw new Exception("All Python execution attempts failed");
            }

        } catch (Exception e) {
            System.err.println("Visualization failed: " + e.getMessage());
            System.err.println("Manual command: py visualize_ga.py \"" + csvPath + "\"");
        }
    }

    private static Map<String, String> findPythonPaths() {
        Map<String, String> paths = new HashMap<>();
        try {
            // Check registry for Python installations
            Process process = Runtime.getRuntime().exec(
                    "reg query \"HKLM\\SOFTWARE\\Python\\PythonCore\" /s /f \"InstallPath\""
            );

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("REG_SZ")) {
                    String[] parts = line.split("\\s{4,}");
                    if (parts.length > 2) {
                        String path = parts[parts.length - 1].trim();
                        paths.put("InstallPath", path);
                    }
                }
            }
            process.waitFor();
        } catch (Exception e) {
            System.err.println("Registry query failed: " + e.getMessage());
        }
        return paths;
    }

    private static boolean runPython(String pythonExe, String scriptPath, String csvPath) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    pythonExe, scriptPath, csvPath
            );
            pb.redirectErrorStream(true);

            System.out.println("Executing: " + String.join(" ", pb.command()));
            Process process = pb.start();

            // Capture output
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                output.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Visualization succeeded with " + pythonExe);
                return true;
            }
            System.err.println("Python exited with code: " + exitCode);
        } catch (Exception e) {
            System.err.println("Error with " + pythonExe + ": " + e.getMessage());
        }
        return false;
    }
}
