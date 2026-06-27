package net.mohamed.metier;

import net.mohamed.dao.IDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component("metier")
public class MetierImpl implements IMetier {
    @Autowired
    @Qualifier("d2")
    private IDao dao; // Couplage faible

    /**
     * Injection de l'attribut dao
     * Un objet d'une classe qui implémente l'interface IDao
     * au moment de l'instantiation
     */

    public MetierImpl(IDao dao){
        this.dao = dao;
    }

    public MetierImpl() {  // Constructeur sans paramètre pour l'injection des dépendances via setters
    }


    /**
     * Injection dans l'attrbut dao
     * Un objet d'une classe qui implémente l'interface IDao
     * après instantiation
     */

    public void setDao(IDao dao) {
        this.dao = dao;
    }

    @Override
    public double calcul() {
        double t = dao.getData();
        double res = t * 12 *Math.PI/2 *Math.cos(t);
        return res;
    }
}