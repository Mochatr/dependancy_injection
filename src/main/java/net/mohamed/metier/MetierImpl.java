package net.mohamed.metier;

import net.mohamed.dao.IDao;

public class MetierImpl implements IMetier {
    private IDao dao; // Couplage faible

    /**
     * Injection de l'attribut dao
     * Un objet d'une classe qui implémente l'interface IDao
     * au moment de l'instantiation
     */

    public MetierImpl(IDao dao){
        this.dao = dao;
    }

    @Override
    public double calcul() {
        double t = dao.getData();
        double res = t * 12 *Math.PI/2 *Math.cos(t);
        return res;
    }
}