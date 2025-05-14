import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

public class HanziWorkbook extends JFrame {
    private JPanel controlPanel;
    private DrawingPanel drawingPanel;
    private JTextField pageInput;
    private JButton prevButton, nextButton, zoomInButton, zoomOutButton, undoButton, clearButton;
    private BufferedImage image;
    private double scale = 1.0;
    private int currentPage = 26;
    private HashMap<Integer, ArrayList<Line>> pageDrawings = new HashMap<>();
    private ArrayList<Line> currentDrawings = new ArrayList<>();
    private Color drawingColor = Color.BLACK;
    private JScrollPane scrollPane;
    private JSlider brushSizeSlider;
    private int brushSize = 5;

    public HanziWorkbook() {
        setTitle("Hanzi Workbook");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
		this.setIconImage(new ImageIcon(getClass().getResource("res/hanzi.png")).getImage());
		
        drawingPanel = new DrawingPanel();
        scrollPane = new JScrollPane(drawingPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getVerticalScrollBar().setUnitIncrement(30);

        controlPanel = new JPanel();
        controlPanel.setLayout(new FlowLayout());

        prevButton = new JButton("< Previous");
        prevButton.addActionListener(e -> loadPage(currentPage - 1));

        nextButton = new JButton("Next >");
        nextButton.addActionListener(e -> loadPage(currentPage + 1));

        zoomInButton = new JButton("Zoom +");
        zoomInButton.addActionListener(e -> zoom(1.2));

        zoomOutButton = new JButton("Zoom -");
        zoomOutButton.addActionListener(e -> zoom(0.8));

        undoButton = new JButton("Undo");
        undoButton.addActionListener(e -> undoDrawing());

        clearButton = new JButton("Clear Drawing");
        clearButton.addActionListener(e -> clearDrawing());

        pageInput = new JTextField(5);
        pageInput.addActionListener(e -> loadPage(Integer.parseInt(pageInput.getText())));

        brushSizeSlider = new JSlider(JSlider.HORIZONTAL, 1, 20, brushSize);
        brushSizeSlider.setMajorTickSpacing(5);
        brushSizeSlider.setPaintTicks(true);
        brushSizeSlider.setPaintLabels(true);
        brushSizeSlider.addChangeListener(e -> {
            brushSize = brushSizeSlider.getValue();
        });
        controlPanel.add(new JLabel("Brush Size:"));
        controlPanel.add(brushSizeSlider);

        controlPanel.add(prevButton);
        controlPanel.add(nextButton);
        controlPanel.add(new JLabel("Page:"));
        controlPanel.add(pageInput);
        controlPanel.add(zoomInButton);
        controlPanel.add(zoomOutButton);
        controlPanel.add(undoButton);
        controlPanel.add(clearButton);

        add(controlPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        loadPage(currentPage);
        
        addKeyboardShortcuts();
    }

    private void addKeyboardShortcuts() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), "undo"); // Ctrl + Z
        am.put("undo", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                undoDrawing();
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "prevPage"); // Previous Page: Left Arrow
        am.put("prevPage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPage(currentPage - 1);
            }
        });

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "nextPage"); // Next Page: Right Arrow
        am.put("nextPage", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadPage(currentPage + 1);
            }
        });
        
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0), "zoomIn"); // Plus key: zoom in
		am.put("zoomIn", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				zoom(1.2);
			}
		});
		
		im.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, 0), "zoomOut"); // Minus key: zoom out
		am.put("zoomOut", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				zoom(0.8);
			}
		});
    }

    private void loadPage(int pageNum) {
        if (pageNum < 26 || pageNum > 298) {
            JOptionPane.showMessageDialog(this, "Not a valid page lol");
            return;
        }

        try {
            File imageFile = new File("worksheets/page_" + pageNum + ".png");
            if (imageFile.exists()) {
                image = ImageIO.read(imageFile);
                currentPage = pageNum;
                pageInput.setText(String.valueOf(currentPage));

                if (!pageDrawings.containsKey(currentPage)) {
                    pageDrawings.put(currentPage, new ArrayList<>());
                }
                currentDrawings = pageDrawings.get(currentPage);

                drawingPanel.setPreferredSize(new Dimension((int) (image.getWidth() * scale), (int) (image.getHeight() * scale)));
                drawingPanel.revalidate();
                drawingPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "That page can't be found (lol)");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void zoom(double factor) {
        scale *= factor;
        if (image != null) {
            drawingPanel.setPreferredSize(new Dimension((int) (image.getWidth() * scale), (int) (image.getHeight() * scale)));
            drawingPanel.revalidate();
        }
        drawingPanel.repaint();
    }

    private void undoDrawing() {
        if (!currentDrawings.isEmpty()) {
            currentDrawings.remove(currentDrawings.size() - 1);
            pageDrawings.put(currentPage, currentDrawings);
            drawingPanel.repaint();
        }
    }

    private void clearDrawing() {
        currentDrawings.clear();
        pageDrawings.put(currentPage, currentDrawings);
        drawingPanel.repaint();
    }

    class DrawingPanel extends JPanel {
        private ArrayList<Point> currentLine = new ArrayList<>();
        private Color drawingColor = Color.BLACK;

        public DrawingPanel() {
            setBackground(Color.WHITE);
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentLine.clear();
                    currentLine.add(e.getPoint());
                }

                public void mouseReleased(MouseEvent e) {
                    currentDrawings.add(new Line(new ArrayList<>(currentLine), drawingColor, brushSize));
                    pageDrawings.put(currentPage, currentDrawings);
                    currentLine.clear();
                    repaint();
                }
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent e) {
                    currentLine.add(e.getPoint());
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            if (image != null) {
                g2d.drawImage(image, 0, 0, (int) (image.getWidth() * scale), (int) (image.getHeight() * scale), null);
            }

            if (pageDrawings.containsKey(currentPage)) {
                for (Line line : pageDrawings.get(currentPage)) {
                    g2d.setColor(line.color);
                    g2d.setStroke(new BasicStroke(line.brushSize));
                    for (int i = 1; i < line.points.size(); i++) {
                        Point p1 = line.points.get(i - 1);
                        Point p2 = line.points.get(i);
                        g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
                    }
                }
            }
            g2d.setColor(drawingColor);
            g2d.setStroke(new BasicStroke(brushSize));
            for (int i = 1; i < currentLine.size(); i++) {
                Point p1 = currentLine.get(i - 1);
                Point p2 = currentLine.get(i);
                g2d.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }
    }

    class Line {
        ArrayList<Point> points;
        Color color;
        int brushSize;

        public Line(ArrayList<Point> points, Color color, int brushSize) {
            this.points = points;
            this.color = color;
            this.brushSize = brushSize;
        }

        public Line(ArrayList<Point> points, Color color) {
            this(points, color, 3);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new HanziWorkbook().setVisible(true);
        });
    }
}
