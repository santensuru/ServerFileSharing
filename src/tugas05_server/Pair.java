/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tugas05_server;

/**
 *
 * @author
 * 1. Djuned Fernando Djusdek   5112100071
 * 2. M. Arief Ridwan           5112100097
 * 3. I Gede Arya Putra Perdana 5112100151
 * 
 * https://github.com/santensuru/ServerFileSharing
 * email: djuned.ong@gmail.com
 * 
 * version 0.0.2b beta
 * @param <L>
 * @param <R>
 */
public class Pair<L,R> {
    private final L left;
    private R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() { return left; }
    public R getRight() { return right; }
    
    public R setRight(R right) {
        R old = this.right;
        this.right = right ;
        return old;
    }

    @Override
    public int hashCode() { return left.hashCode() ^ right.hashCode(); }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof Pair)) return false;
        Pair pairo = (Pair) o;
        return this.left.equals(pairo.getLeft()) &&
               this.right.equals(pairo.getRight());
    }

}
