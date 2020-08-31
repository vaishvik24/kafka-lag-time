import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import scala.*;
import scala.collection.*;
import scala.collection.Iterable;
import scala.collection.Seq;
import scala.collection.generic.CanBuildFrom;
import scala.collection.generic.FilterMonadic;
import scala.collection.generic.GenericCompanion;
import scala.collection.immutable.*;
import scala.collection.immutable.IndexedSeq;
import scala.collection.immutable.Map;
import scala.collection.immutable.Set;
import scala.collection.mutable.Buffer;
import scala.collection.mutable.Builder;
import scala.collection.mutable.Queue;
import scala.collection.mutable.StringBuilder;
import scala.collection.parallel.Combiner;
import scala.collection.parallel.ParIterable;
import scala.collection.parallel.ParSeq;
import scala.math.Numeric;
import scala.math.Ordering;
import scala.reflect.ClassTag;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Iterator;
import java.util.List;
import scala.collection.JavaConverters.*;
import sun.awt.X11.XSystemTrayPeer;

class point{
    private long time;
    private double offset;
    private double timelag;
    private double offsetlag;

    point(long t,double o, double ol){
        this.time=t;
        this.offset=o;
        this.offsetlag = ol;
    }
    long getTime(){
        return this.time;
    }
    double getOffset(){
        return this.offset;
    }
    void setTimeLag(double tl){
        this.timelag=tl;
    }
    void printPoint(){
        System.out.println("offset " + this.offset + " and time = " + this.time);
    }
    void printDetailsOfPoint(){
        System.out.println("offset " + this.offset + " and time = " + this.time + " and timeLag = " + this.timelag + " and offset lag = " + this.offsetlag);

    }
}

public class mainApp {

    public static void main(String [] args){
        String databaseURL = "http://95.217.47.85:19009";
        String userName ="acceldata";
        String password = "D@t@Ops";
        InfluxDB influxDB = InfluxDBFactory.connect(databaseURL, userName, password);

        Pong response = influxDB.ping();
        if (response.getVersion().equalsIgnoreCase("unknown")) {
            System.out.println("Error pinging server");
            return;
        }

        String dbName = "acceldata";
        influxDB.setDatabase(dbName);

        Query query = new Query("select * from kafka_consumer_offsets where \"group\"='web-readiness' and  \"topic\"='asset_tracking' and \"partition\"='1'  \n", dbName);
        QueryResult queryResult = influxDB.query(query);

        LinkedList <point> points = new LinkedList<>();
        int length = 0;
        for (QueryResult.Result result : queryResult.getResults()) {

            for (QueryResult.Series series : result.getSeries()) {
                System.out.println("series.getName() = " + series.getName());
                System.out.println("series.getColumns() = " + series.getColumns());
//                System.out.println("series.getValues() = " + series.getValues());
                length = series.getValues().size();
                for(int i=0;i<series.getValues().size();i++){
//                    System.out.println(series.getValues().get(i).get(4) + " = " +
//                            ( (double)series.getValues().get(i).get(5)-(double)series.getValues().get(i).get(6) )
//                    );
                    try {
                        Date date=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(series.getValues().get(i).get(0).toString());
                        long timestamp = date.getTime();
//                        adding point
                        double offsetLag =( (double)series.getValues().get(i).get(5)-(double)series.getValues().get(i).get(6) );
                        points.add(new point(timestamp , (double)series.getValues().get(i).get(5), offsetLag) );
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

        System.out.println("------------------------------------------------------ Estimating lag in time for points ( "  +  length + " ) ----------------------------------------------------------------");
        Iterator<point> it = points.listIterator();
        while(it.hasNext()){
            point next = it.next();
            next.printPoint();

            double timelag  = estimate.lookup( next.getOffset() ,points);

            next.setTimeLag(timelag);
            next.printDetailsOfPoint();
            System.out.println("-------------------------------------------------------------------------------------------------------------------");
        }

        influxDB.close();
    }
}
