import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class TagExtractorFrame extends JFrame {

    private JLabel fileLabel;
    private JButton chooseTextFileButton;
    private JButton chooseStopWordsButton;
    private JButton saveTagsButton;
    private JTextArea outputArea;

    private File textFile;
    private File stopWordsFile;

    private Set<String> stopWords;
    private Map<String, Integer> wordCounts;

    public TagExtractorFrame() {
        super("Tag Extractor");

        stopWords = new HashSet<String>();
        wordCounts = new HashMap<String, Integer>();

        fileLabel = new JLabel("No text file selected");
        chooseTextFileButton = new JButton("Choose Text File");
        chooseStopWordsButton = new JButton("Choose Stop Words File");
        saveTagsButton = new JButton("Save Tags To File");

        outputArea = new JTextArea();
        outputArea.setEditable(false);

        setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.add(chooseTextFileButton);
        buttonPanel.add(chooseStopWordsButton);

        topPanel.add(buttonPanel, BorderLayout.NORTH);
        topPanel.add(fileLabel, BorderLayout.SOUTH);

        JScrollPane scrollPane = new JScrollPane(outputArea);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(saveTagsButton);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        chooseStopWordsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseStopWordsFile();
            }
        });

        chooseTextFileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                chooseTextFile();
            }
        });

        saveTagsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveTagsToFile();
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void chooseStopWordsFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            stopWordsFile = chooser.getSelectedFile();
            loadStopWords();
        }
    }

    private void loadStopWords() {
        stopWords.clear();

        if (stopWordsFile == null) {
            return;
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(stopWordsFile);

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String word = line.trim().toLowerCase();

                if (word.length() > 0) {
                    stopWords.add(word);
                }
            }

            JOptionPane.showMessageDialog(this,
                    "Loaded " + stopWords.size() + " stop words.");

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error reading stop words file.");
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private void chooseTextFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            textFile = chooser.getSelectedFile();
            fileLabel.setText("Tags from: " + textFile.getName());
            scanTextFile();
        }
    }

    private void scanTextFile() {
        wordCounts.clear();
        outputArea.setText("");

        if (textFile == null) {
            return;
        }

        Scanner scanner = null;
        try {
            scanner = new Scanner(textFile);

            while (scanner.hasNext()) {
                String token = scanner.next();
                String cleaned = cleanWord(token);

                if (cleaned.length() == 0) {
                    continue;
                }

                if (stopWords.contains(cleaned)) {
                    continue;
                }

                Integer count = wordCounts.get(cleaned);
                if (count == null) {
                    wordCounts.put(cleaned, 1);
                } else {
                    wordCounts.put(cleaned, count + 1);
                }
            }

            displayTags();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error reading text file.");
        } finally {
            if (scanner != null) {
                scanner.close();
            }
        }
    }

    private String cleanWord(String token) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);

            if (Character.isLetter(c)) {
                builder.append(Character.toLowerCase(c));
            }
        }

        return builder.toString();
    }

    private void displayTags() {
        outputArea.setText("");

        ArrayList<String> words = new ArrayList<String>(wordCounts.keySet());
        Collections.sort(words);

        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            int count = wordCounts.get(word);
            builder.append(word);
            builder.append(" : ");
            builder.append(count);
            builder.append("\n");
        }

        outputArea.setText(builder.toString());
        outputArea.setCaretPosition(0);
    }

    private void saveTagsToFile() {
        if (wordCounts.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "No tags to save. Choose a text file first.");
            return;
        }

        JFileChooser chooser = new JFileChooser();
        int result = chooser.showSaveDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File outFile = chooser.getSelectedFile();
            writeTagsToFile(outFile);
        }
    }

    private void writeTagsToFile(File outFile) {
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(outFile);

            ArrayList<String> words = new ArrayList<String>(wordCounts.keySet());
            Collections.sort(words);

            for (String word : words) {
                int count = wordCounts.get(word);
                writer.println(word + " " + count);
            }

            JOptionPane.showMessageDialog(this,
                    "Tags saved to: " + outFile.getName());

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error writing tags file.");
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
