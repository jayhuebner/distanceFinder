import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class ConnectionPool {

    public HikariDataSource create() {

        HikariConfig dwh1Config = new HikariConfig();

        dwh1Config.setJdbcUrl("jdbc:oracle:thin:@dwhprd.vwoa.na.vwg:1526/DWH1.WORLD");
        dwh1Config.setUsername("DI_ADM");
        dwh1Config.setPassword("NoTgcMOzFlBcisq6aSQ8xcv98RyBc4");
        dwh1Config.addDataSourceProperty("cachePrepStmts", "true");
        dwh1Config.addDataSourceProperty("prepStmtCacheSize", "250");
        dwh1Config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        dwh1Config.setAutoCommit(true);

        return new HikariDataSource(dwh1Config);

    }

}