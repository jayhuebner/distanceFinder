import com.google.maps.*;
import com.google.maps.model.*;

import com.zaxxer.hikari.HikariDataSource;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.ArrayList;
import java.util.List;

public class App {

    //---------------------------------------------------------------------------------
    // Initialize the DB connection pool, dataSource and connector
    //---------------------------------------------------------------------------------
    private static final HikariDataSource dsDWH1    = new ConnectionPool().create();
    private static final Sql2o sql2o                = new Sql2o(dsDWH1);
    private static List<DBData> dataList            = new ArrayList<DBData>();

    public static void main( String[] args )  throws Exception {

        System.out.println("\nBegin Process...\n");

        // Retrieve Data and store in POJO
        // Iterate through records, make service call, update mileage property in DB

        try {

            List<DBData> dataList = getDBDataList(sql2o);

            System.out.println(dataList.size() + " Records found...\n");
            Integer thisRecord = 0;

            for (DBData d : dataList) {

                thisRecord +=1;
                d.setDistance(getDriveDistanceInKm(d.origin, d.destination));
                System.out.println("Updating " + thisRecord +  " of " + dataList.size() + " ... \t" + d.vin + " - Origin: " + d.origin + " Destination: " + d.destination + " Distance: " + d.distance + " km");

                if (d.distance != -1) {
                    String sql = "UPDATE DI_ADM.TDIT_TDI_PURCH_TRANSPORT SET TDIT_OVERALL_DISTANCE = :theDistance WHERE TDIT_VEHL_VIN = :theVIN";

                    try (Connection con = sql2o.open()) {

                        con.setRollbackOnException(false);
                        con.createQuery(sql)
                                .addParameter("theDistance", d.distance)
                                .addParameter("theVIN", d.vin)
                                .executeUpdate();

                    } catch (Exception e) {
                    }
                }
            }
        }
        catch(Exception e) {
            System.out.println(e);
        }

        // Close
        System.out.println("\nEnd Process...");

    }


    private static List<DBData> getDBDataList(Sql2o sql2o) {

        System.out.println("Retrieve Records from DB...\n");

        String  sql =   " SELECT  ";
        sql +=  "     TDIT_VEHL_VIN   AS vin, ";
        sql +=  "     SUBSTR(TDIT_PURCHASE_DLR_POSTAL_CDE,0,3) || ' ' || SUBSTR(TDIT_PURCHASE_DLR_POSTAL_CDE,4,6) AS origin, ";
        sql +=  "     SUBSTR(TDIT_PICKUP_POSTAL_CDE,0,3) || ' ' || SUBSTR(TDIT_PICKUP_POSTAL_CDE,4,6)  AS destination ";
        sql +=  " FROM DI_ADM.TDIT_TDI_PURCH_TRANSPORT ";
        sql +=  " WHERE ( TDIT_OVERALL_DISTANCE IS NULL AND TDIT_PURCHASE_DLR_POSTAL_CDE IS NOT NULL AND TDIT_PICKUP_POSTAL_CDE IS NOT NULL)";

        try(Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .executeAndFetch(DBData.class);
        }

        catch(Exception e) {
            System.out.println("getDBDataList: " + e);
        }
        return dataList;
    }


    private static double getDriveDistanceInKm(String origin, String destination) throws Exception {

        //  https://maps.googleapis.com/maps/api/directions/json?origin=48326&destination=48442&mode=driving&key=AIzaSyC48l-sQTMWULJpBpuBTwUxBGk15obiGkQ
        //  https://android.jlelse.eu/google-maps-directions-api-5b2e11dee9b0

        double distanceInKm = -1.00;

        try {

            GeoApiContext context = new GeoApiContext.Builder()
                    .apiKey("AIzaSyC48l-sQTMWULJpBpuBTwUxBGk15obiGkQ")
                    .build();

            DirectionsResult results = DirectionsApi.newRequest(context)
                    .mode(TravelMode.DRIVING)
                    .origin(origin)
                    .destination(destination)
                    .units(Unit.METRIC)
                    .await();

            distanceInKm = results.routes[0].legs[0].distance.inMeters/1000.00;
        }
        catch(Exception e) {
        }

        finally {
            return distanceInKm;
        }
    }


}
