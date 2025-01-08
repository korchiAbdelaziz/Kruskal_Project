package kruskal06;

import java.awt.Point;

public class Arc {
    Point sommet1, sommet2;
    int poids;

    public Arc(Point sommet1, Point sommet2, int poids) {
        this.sommet1 = sommet1;
        this.sommet2 = sommet2;
        this.poids = poids;
    }
}
