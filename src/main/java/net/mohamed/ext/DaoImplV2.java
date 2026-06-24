package net.mohamed.ext;

import net.mohamed.dao.IDao;

public class DaoImplV2 implements IDao {
    @Override
    public double getData() {
        System.out.println("Version capteurs ....");
        double t = 13;
        return t;
    }
}
