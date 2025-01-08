package kruskal06;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Main extends JFrame {

    private ArrayList<Point> sommets = new ArrayList<>();
    private ArrayList<Arc> arcs = new ArrayList<>();
    private ArrayList<Arc> acpm = new ArrayList<>();
    private boolean isOriented;

    public Main() {
        int choix = JOptionPane.showConfirmDialog(
                null, "Est-ce un graphe orienté ?", "Type de graphe",
                JOptionPane.YES_NO_OPTION
        );
        isOriented = (choix == JOptionPane.YES_OPTION);

        setTitle("Dessinez le graphe et calculez l'ACPM");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Configuration du panneau de dessin
        CanvasPanel canvas = new CanvasPanel(sommets, arcs, acpm, isOriented);
        canvas.setBackground(Color.WHITE);

        // Créer les boutons avec des couleurs spécifiques
        JButton ajouterArcButton = new JButton("Ajouter un arc");
        ajouterArcButton.setBackground(new Color(144, 238, 144));  // Vert pâle pour ajouter
        ajouterArcButton.addActionListener(e -> ajouterArc());

        JButton calculerButton = new JButton("Calculer ACPM");
        calculerButton.setBackground(new Color(173, 216, 230));  // Bleu clair pour le calcul
        calculerButton.addActionListener(e -> calculerACPM());

        JButton exporterButton = new JButton("Exporter l'image");
        exporterButton.setBackground(Color.ORANGE);  // Orange pour l'export
        exporterButton.addActionListener(e -> exporterImage(canvas));

        JButton supprimerSommetButton = new JButton("Supprimer un sommet");
        supprimerSommetButton.setBackground(new Color(255, 99, 71));  // Rouge plus clair pour supprimer un sommet
        supprimerSommetButton.addActionListener(e -> supprimerSommet());

        JButton supprimerArcButton = new JButton("Supprimer un arc");
        supprimerArcButton.setBackground(new Color(255, 99, 71));  // Rouge pour supprimer un arc
        supprimerArcButton.addActionListener(e -> supprimerArc());


        add(canvas, BorderLayout.CENTER);   // JPanel : Conteneur utilisé pour regrouper les boutons.
        JPanel buttonPanel = new JPanel(); 
        buttonPanel.add(ajouterArcButton);
        buttonPanel.add(calculerButton);
        buttonPanel.add(exporterButton);
        buttonPanel.add(supprimerSommetButton);
        buttonPanel.add(supprimerArcButton);
        add(buttonPanel, BorderLayout.SOUTH);  //  Ajouter le panneau des boutons au bas de la fenêtre

        setVisible(true); //  Rends la fenêtre visible à l'utilisateur.(Par défaut, un objet JFrame est créé mais n'est pas affiché.)
    }

    private void ajouterArc() {  
        if (sommets.size() < 2) {
            JOptionPane.showMessageDialog(this, "Ajoutez au moins deux sommets !");
            return;
        }
        try {
            String sommet1 = JOptionPane.showInputDialog("Nom du premier sommet (indice) :");
            String sommet2 = JOptionPane.showInputDialog("Nom du deuxième sommet (indice) :");

            int index1 = Integer.parseInt(sommet1);
            int index2 = Integer.parseInt(sommet2);

            if (index1 < 0 || index1 >= sommets.size() || index2 < 0 || index2 >= sommets.size()) {
                JOptionPane.showMessageDialog(this, "Erreur : Indices de sommet invalides !");
                return;
            }

            String poidsStr = JOptionPane.showInputDialog("Entrez le poids de l'arc/arête :");
            int poids = Integer.parseInt(poidsStr);
            Point p1 = sommets.get(index1);
            Point p2 = sommets.get(index2);
            arcs.add(new Arc(p1, p2, poids));

            if (!isOriented) {
                arcs.add(new Arc(p2, p1, poids));
            }

            repaint(); 	// Rafraîchit l'affichage pour montrer le nouvel arc.
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : Entrez des valeurs numériques valides !");
        }
    }

    private void calculerACPM() {
        if (sommets.isEmpty() || arcs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Le graphe est vide. Ajoutez des sommets et des arcs.");
            return;
        }

        acpm.clear(); //  Vider la liste des arcs de l'ACPM
        Map<Point, Point> parent = new HashMap<>(); // La clé représente un sommet. La valeur représente le "parent" de ce sommet exemple {A:GroupA1}, {B:GroupB}
        for (Point sommet : sommets) {
            parent.put(sommet, sommet);
        }

        // Trie les arcs par poids croissant.
        arcs.sort(Comparator.comparingInt(arc -> arc.poids));

        for (Arc arc : arcs) {
            Point groupe1 = find(arc.sommet1, parent);
            Point groupe2 = find(arc.sommet2, parent);

            if (!groupe1.equals(groupe2)) {
                acpm.add(arc);
                union(groupe1, groupe2, parent);
            }
        }

        repaint();

        int poidsTotal = acpm.stream().mapToInt(arc -> arc.poids).sum();
        JOptionPane.showMessageDialog(this, "Poids total de l'ACPM : " + poidsTotal);
    }

    private Point find(Point p, Map<Point, Point> parent) {
        if (parent.get(p) != p)  // Si le sommet p n'est pas son propre parent, cela signifie que p n'est pas le représentant de son ensemble.
        {   
            parent.put(p, find(parent.get(p), parent)); // Trouve le représentant de l'ensemble d'un sommet. (parent = { A -> C, B -> C, C -> C } ) 
        }
        return parent.get(p);
    }

    // fusionner deux ensembles  
    private void union(Point p1, Point p2, Map<Point, Point> parent) {
        parent.put(find(p1, parent), find(p2, parent));
    }

    private void exporterImage(CanvasPanel canvas) {
        BufferedImage image = new BufferedImage(canvas.getWidth(), canvas.getHeight(), BufferedImage.TYPE_INT_ARGB); // objet BufferedImage pour capturer le contenu du panneau de dessin.
        Graphics2D g2d = image.createGraphics();
        canvas.printAll(g2d);

        // Calculer le poids total de l'ACPM
        int poidsTotal = acpm.stream().mapToInt(arc -> arc.poids).sum();

        // Ajouter le poids total de l'ACPM sur l'image
        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Arial", Font.BOLD, 16));
        g2d.drawString("Poids total de l'ACPM: " + poidsTotal, 20, 40);

        g2d.dispose();

        try {
            File outputFile = new File("graphe_exporte.png");
            ImageIO.write(image, "png", outputFile);
            JOptionPane.showMessageDialog(this, "Image exportée avec succès : " + outputFile.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Erreur lors de l'exportation de l'image.");
        }
    }

    private void supprimerSommet() {
        if (sommets.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il n'y a pas de sommet à supprimer.");
            return;
        }

        try {
            String sommet = JOptionPane.showInputDialog("Nom du sommet à supprimer (indice) :");
            int index = Integer.parseInt(sommet);

            if (index < 0 || index >= sommets.size()) {
                JOptionPane.showMessageDialog(this, "Erreur : Indice de sommet invalide !");
                return;
            }

            // Supprimer le sommet et les arcs associés
            Point sommetASupprimer = sommets.get(index);
            sommets.remove(index);
            arcs.removeIf(arc -> arc.sommet1.equals(sommetASupprimer) || arc.sommet2.equals(sommetASupprimer));

            repaint();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : Entrez un indice valide !");
        }
    }

    private void supprimerArc() {
        if (arcs.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Il n'y a pas d'arc à supprimer.");
            return;
        }

        try {
            String sommet1 = JOptionPane.showInputDialog("Nom du premier sommet (indice) de l'arc à supprimer :");
            String sommet2 = JOptionPane.showInputDialog("Nom du deuxième sommet (indice) de l'arc à supprimer :");

            int index1 = Integer.parseInt(sommet1);
            int index2 = Integer.parseInt(sommet2);

            if (index1 < 0 || index1 >= sommets.size() || index2 < 0 || index2 >= sommets.size()) {
                JOptionPane.showMessageDialog(this, "Erreur : Indices de sommet invalides !");
                return;
            }

            // Supprimer l'arc
            Point p1 = sommets.get(index1);
            Point p2 = sommets.get(index2);
            arcs.removeIf(arc -> (arc.sommet1.equals(p1) && arc.sommet2.equals(p2)) || (arc.sommet1.equals(p2) && arc.sommet2.equals(p1)));

            repaint();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Erreur : Entrez des indices valides !");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);  // Cela lance l'interface graphique (UI) de l'application en affichant une fenêtre Swing.
    }
}
