import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.regex.*;

public class TagExtractor extends JFrame {
    private final JTextArea resultArea;
    private final JLabel fileLabel;
    private final Set<String> stopWords;
    private final Map<String, Integer> tagFrequencies;

    public TagExtractor() {
        // Initialize data structures
        stopWords = new TreeSet<>();
        tagFrequencies = new TreeMap<>();

        // Set up the frame
        setTitle("Tag Extractor");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Create main panel with BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // File label
        fileLabel = new JLabel("No file selected");
        mainPanel.add(fileLabel, BorderLayout.NORTH);

        // Result area
        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton selectFileButton = new JButton("Select Text File");
        JButton selectStopWordsButton = new JButton("Select Stop Words File");
        JButton saveTagsButton = new JButton("Save Tags");

        buttonPanel.add(selectFileButton);
        buttonPanel.add(selectStopWordsButton);
        buttonPanel.add(saveTagsButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Add main panel to frame
        add(mainPanel);

        // Action listeners
        selectFileButton.addActionListener(e -> selectAndProcessFile());
        selectStopWordsButton.addActionListener(e -> loadStopWords());
        saveTagsButton.addActionListener(e -> saveTags());
    }

    private void selectAndProcessFile() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            fileLabel.setText("Selected file: " + file.getName());
            processFile(file);
        }
    }

    private void loadStopWords() {
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                stopWords.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    String word = line.trim().toLowerCase();
                    if (!word.isEmpty()) {
                        stopWords.add(word);
                    }
                }
                JOptionPane.showMessageDialog(this,
                        "Loaded " + stopWords.size() + " stop words",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error loading stop words: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void processFile(File file) {
        tagFrequencies.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            Pattern pattern = Pattern.compile("[a-zA-Z]+");

            while ((line = reader.readLine()) != null) {
                // Convert to lowercase and find all words
                Matcher matcher = pattern.matcher(line.toLowerCase());

                while (matcher.find()) {
                    String word = matcher.group();
                    // Skip if it's a stop word
                    if (!stopWords.contains(word)) {
                        tagFrequencies.put(word,
                                tagFrequencies.getOrDefault(word, 0) + 1);
                    }
                }
            }

            // Display results
            displayResults();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this,
                    "Error processing file: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayResults() {
        StringBuilder result = new StringBuilder();
        result.append("Tag Frequencies:\n\n");

        for (Map.Entry<String, Integer> entry : tagFrequencies.entrySet()) {
            result.append(String.format("%s: %d\n",
                    entry.getKey(), entry.getValue()));
        }

        resultArea.setText(result.toString());
        resultArea.setCaretPosition(0);
    }

    private void saveTags() {
        if (tagFrequencies.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No tags to save",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (PrintWriter writer = new PrintWriter(file)) {
                for (Map.Entry<String, Integer> entry : tagFrequencies.entrySet()) {
                    writer.printf("%s,%d\n",
                            entry.getKey(), entry.getValue());
                }
                JOptionPane.showMessageDialog(this,
                        "Tags saved successfully",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error saving tags: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}