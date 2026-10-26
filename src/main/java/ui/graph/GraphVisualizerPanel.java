package ui.graph;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.*;
import java.util.List;

/**
 * GraphVisualizerPanel - Lightweight Swing custom-painted component for interactive graph rendering.
 * Draws cases as nodes (circles) and citations as directed arrows.
 * Requires zero external heavy libraries; uses standard Java 2D Graphics.
 */
public class GraphVisualizerPanel extends JPanel {

    private final List<Map<String, String>> nodes = new ArrayList<>();
    private final List<Map<String, String>> edges = new ArrayList<>();
    private String rootCaseNo = "";

    private final Map<String, Point> nodePositions = new HashMap<>();

    public GraphVisualizerPanel() {
        setBackground(new Color(248, 250, 252));
        setBorder(BorderFactory.createTitledBorder("Visual Citation Graph View"));
    }

    /**
     * Updates graph data and recalculates visual positions.
     */
    public void setGraphData(String rootCase, List<Map<String, String>> newNodes, List<Map<String, String>> newEdges) {
        this.rootCaseNo = rootCase != null ? rootCase : "";
        this.nodes.clear();
        this.edges.clear();
        if (newNodes != null) this.nodes.addAll(newNodes);
        if (newEdges != null) this.edges.addAll(newEdges);

        calculateLayout();
        repaint();
    }

    private void calculateLayout() {
        nodePositions.clear();
        if (nodes.isEmpty()) return;

        int width = Math.max(getWidth(), 600);
        int height = Math.max(getHeight(), 450);
        int centerX = width / 2;
        int centerY = height / 2;

        // Place root node at center
        nodePositions.put(rootCaseNo, new Point(centerX, centerY));

        // Place other nodes in a concentric circle
        List<String> otherNodes = new ArrayList<>();
        for (Map<String, String> n : nodes) {
            String id = n.get("id");
            if (!id.equals(rootCaseNo) && !otherNodes.contains(id)) {
                otherNodes.add(id);
            }
        }

        int count = otherNodes.size();
        if (count == 0) return;

        double radius = Math.min(centerX, centerY) * 0.72;
        double angleStep = (2 * Math.PI) / count;

        for (int i = 0; i < count; i++) {
            double angle = i * angleStep;
            int x = (int) (centerX + radius * Math.cos(angle));
            int y = (int) (centerY + radius * Math.sin(angle));
            nodePositions.put(otherNodes.get(i), new Point(x, y));
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (nodes.isEmpty()) {
            g2.setColor(Color.GRAY);
            g2.setFont(new Font("SansSerif", Font.ITALIC, 14));
            g2.drawString("Select a case in the Graph Explorer above and run an operation to visualize citation paths.", 30, 40);
            return;
        }

        if (nodePositions.isEmpty()) {
            calculateLayout();
        }

        // 1. Draw Directed Edges (Arrows)
        g2.setStroke(new BasicStroke(1.8f));
        for (Map<String, String> edge : edges) {
            String src = edge.get("source");
            String tgt = edge.get("target");

            Point pSrc = nodePositions.get(src);
            Point pTgt = nodePositions.get(tgt);

            if (pSrc != null && pTgt != null) {
                g2.setColor(new Color(100, 116, 139, 180));
                drawArrow(g2, pSrc.x, pSrc.y, pTgt.x, pTgt.y);
            }
        }

        // 2. Draw Nodes (Circles with Badges)
        int nodeRadius = 24;
        for (Map<String, String> node : nodes) {
            String id = node.get("id");
            Point pt = nodePositions.get(id);
            if (pt == null) continue;

            boolean isRoot = id.equalsIgnoreCase(rootCaseNo);

            // Node Circle
            if (isRoot) {
                g2.setColor(new Color(234, 88, 12)); // Orange for Root
            } else if (id.startsWith("SC-")) {
                g2.setColor(new Color(37, 99, 235)); // Blue for Supreme Court
            } else if (id.startsWith("HC-")) {
                g2.setColor(new Color(13, 148, 136)); // Teal for High Court
            } else {
                g2.setColor(new Color(107, 114, 128)); // Slate for District
            }

            g2.fillOval(pt.x - nodeRadius, pt.y - nodeRadius, nodeRadius * 2, nodeRadius * 2);

            // Border
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.5f));
            g2.drawOval(pt.x - nodeRadius, pt.y - nodeRadius, nodeRadius * 2, nodeRadius * 2);

            // Label text
            g2.setColor(new Color(15, 23, 42));
            g2.setFont(new Font("SansSerif", isRoot ? Font.BOLD : Font.PLAIN, 11));
            FontMetrics fm = g2.getFontMetrics();
            int txtWidth = fm.stringWidth(id);
            g2.drawString(id, pt.x - (txtWidth / 2), pt.y + nodeRadius + 14);
        }

        // Legend at bottom
        drawLegend(g2);
    }

    private void drawArrow(Graphics2D g2, int x1, int y1, int x2, int y2) {
        int nodeOffset = 24;
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist <= nodeOffset * 2) return;

        // Clip start and end to node boundaries
        double startX = x1 + (dx / dist) * nodeOffset;
        double startY = y1 + (dy / dist) * nodeOffset;
        double endX = x2 - (dx / dist) * nodeOffset;
        double endY = y2 - (dy / dist) * nodeOffset;

        g2.drawLine((int) startX, (int) startY, (int) endX, (int) endY);

        // Draw arrowhead
        double arrowAngle = Math.atan2(endY - startY, endX - startX);
        int arrowSize = 9;

        AffineTransform tx = g2.getTransform();
        g2.translate(endX, endY);
        g2.rotate(arrowAngle);

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint(0, 0);
        arrowHead.addPoint(-arrowSize, -arrowSize / 2);
        arrowHead.addPoint(-arrowSize, arrowSize / 2);
        g2.fill(arrowHead);

        g2.setTransform(tx);
    }

    private void drawLegend(Graphics2D g2) {
        int y = getHeight() - 22;
        if (y < 20) y = 20;
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));

        int x = 15;
        g2.setColor(new Color(234, 88, 12));
        g2.fillOval(x, y - 9, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("Target Case", x + 14, y);

        x += 105;
        g2.setColor(new Color(37, 99, 235));
        g2.fillOval(x, y - 9, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("Supreme Court", x + 14, y);

        x += 115;
        g2.setColor(new Color(13, 148, 136));
        g2.fillOval(x, y - 9, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("High Court", x + 14, y);

        x += 95;
        g2.setColor(new Color(107, 114, 128));
        g2.fillOval(x, y - 9, 10, 10);
        g2.setColor(Color.BLACK);
        g2.drawString("District Court", x + 14, y);
    }
}
