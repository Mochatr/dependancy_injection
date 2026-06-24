package net.mohamed.pres;

import net.mohamed.dao.DaoImpl;
import net.mohamed.ext.DaoImplV2;
import net.mohamed.metier.MetierImpl;

public class Pres1 {
    public static void main(String[] args) {
        DaoImplV2 d = new DaoImplV2();
        MetierImpl metier = new MetierImpl(d);
        // metier.setDao(d); (injection des dépendances via le setter
        System.out.println("RES= "+metier.calcul());
    }
}
