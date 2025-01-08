package kruskal06;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;

public class CanvasPanel extends JPanel {
    private ArrayList<Point> sommets;
    private ArrayList<Arc> arcs;
    private ArrayList<Arc> acpm;
    private boolean isOriented; 
    private Point sommetTemp; // utilisé temporairement pour connecter deux sommets.

    private Color acpmColor = Color.GREEN; // Couleur par défaut pour l'ACPM
    private Random random = new Random(); // Instance pour générer des couleurs aléatoires

    public CanvasPanel(ArrayList<Point> sommets, ArrayList<Arc> arcs, ArrayList<Arc> acpm, boolean isOriented) {
        this.sommets = sommets;
        this.arcs = arcs;
        this.acpm = acpm;
        this.isOriented = isOriented;
        this.sommetTemp = null;

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    sommets.add(new Point(e.getX(), e.getY()));
                    repaint();  // Redessine le panneau pour afficher le nouveau sommet ajouté. (place un "demande de redessin" dans une file d'attente d'événements. )
                } else if (SwingUtilities.isRightMouseButton(e)) {
                    for (Point p : sommets) {
                        if (p.distance(e.getPoint()) < 15) {
                            if (sommetTemp == null) {
                                sommetTemp = p;
                            } else {
                                String poidsStr = JOptionPane.showInputDialog("Entrez le poids :");
                                int poids = Integer.parseInt(poidsStr);
                                arcs.add(new Arc(sommetTemp, p, poids));
                                if (!isOriented) {
                                    arcs.add(new Arc(p, sommetTemp, poids));
                                }
                                sommetTemp = null;
                                repaint();
                                break;
                            }
                        }
                    }
                }
            }
        });
    }

    // paintComponentune méthode pré-définie dans Swing(bibliothèque graphique)
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);  // rendant les dessins plus nets et esthétiques.

        // Dessiner les arcs
        for (Arc arc : arcs) {
            g2d.setColor(Color.BLACK); // Les arcs sont initialement dessinés en noir 
            g2d.setStroke(new BasicStroke(3)); //Épaisseur de trait :
            g2d.drawLine(arc.sommet1.x, arc.sommet1.y, arc.sommet2.x, arc.sommet2.y);  //trace une ligne entre les deux sommets de l'arc.

            // Dessiner le poids
            // positionner le poids de l'arc exactement au centre de l'arc (calcule les position des deux sommmets)
            // g2d : L'objet graphique utilisé pour dessiner.
            int midX = (arc.sommet1.x + arc.sommet2.x) / 2;
            int midY = (arc.sommet1.y + arc.sommet2.y) / 2;
            g2d.setColor(Color.RED);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            g2d.drawString(String.valueOf(arc.poids), midX - 10, midY - 10);  //  mid.. - 10  Décale le texte de 10 pixels vers la gauche/haut  

            // Dessiner une flèche si le graphe est orienté
            if (isOriented) {
                drawArrow(g2d, arc.sommet1, arc.sommet2);
            }
        }

        // Dessiner les arcs de l'ACPM
        for (Arc arc : acpm) {
            g2d.setColor(acpmColor); // Utiliser la couleur définie pour l'ACPM
            g2d.setStroke(new BasicStroke(4));
            g2d.drawLine(arc.sommet1.x, arc.sommet1.y, arc.sommet2.x, arc.sommet2.y);
        }

        // Dessiner les sommets avec des couleurs aléatoires
        for (int i = 0; i < sommets.size(); i++) {
            Point p = sommets.get(i);

            Color sommetColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            g2d.setColor(sommetColor);
            g2d.fillOval(p.x - 10, p.y - 10, 20, 20);

            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            g2d.drawString("S" + i, p.x - 15, p.y - 15);
        }
    }

    private void drawArrow(Graphics2D g2d, Point from, Point to) {
    	// Les calculs suivants permettent de transformer les coordonnées pour obtenir les "ailes"(la ligne de l'arc) de la flèche dans la direction de la ligne from → to.
        int dx = to.x - from.x, dy = to.y - from.y;
        double D = Math.sqrt(dx * dx + dy * dy);  //  la distance entre from et to. Elle est utilisée pour normaliser les calculs
        double xm = D - 15, xn = xm, ym = 7, yn = -7, x;  // D-15 (pour éviter d'inclure la flèche exactement sur le sommet final).
        double sin = dy / D, cos = dx / D;

        x = xm * cos - ym * sin + from.x;
        ym = xm * sin + ym * cos + from.y;
        xm = x;

        x = xn * cos - yn * sin + from.x;
        yn = xn * sin + yn * cos + from.y;
        xn = x;

        // sont responsables du dessin de la flèche au bout de l'arc, en utilisant les points calculés précédemment
        int[] xpoints = {to.x, (int) xm, (int) xn};
        int[] ypoints = {to.y, (int) ym, (int) yn};

        g2d.setColor(Color.BLACK);
        g2d.fillPolygon(xpoints, ypoints, 3);
    }
    
    //  modifier la couleur des arcs qui appartiennent à l'ACPM 

    public void setAcpmColor(Color acpmColor) {
        this.acpmColor = acpmColor;
        repaint();
    }
}
