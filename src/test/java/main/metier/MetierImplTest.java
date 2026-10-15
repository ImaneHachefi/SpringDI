package main.metier;

import main.dao.IDao;
import org.junit.Assert;
import org.junit.Test;

public class MetierImplTest {

    @Test
    public void testCalcul() {
        IDao dao = new IDao() {
            @Override
            public double getValue() {
                return 10.0;
            }
        };

        MetierImpl metier = new MetierImpl();
        metier.setDao(dao);

        double result = metier.calcul();
        Assert.assertEquals(20.0, result, 0.001);  // 10.0 * 2 = 20.0
    }
}