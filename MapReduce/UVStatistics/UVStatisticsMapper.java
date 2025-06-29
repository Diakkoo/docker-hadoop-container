package top.statistics;

import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class UVStatisticsMapper extends Mapper<LongWritable, Text, Text, Text> {

    private static final int NUM_FIELDS = 7;

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString().trim();
        if (line.isEmpty()) {
            context.getCounter("DATA_QUALITY", "EMPTY_LINES").increment(1);
            return;
        }

        String[] fields = line.split(",");
        if (fields.length != NUM_FIELDS) {
            context.getCounter("DATA_QUALITY", "INVALID_FIELD_COUNT").increment(1);
            return;
        }

        String userId = fields[0].trim();
        String date = fields[1].trim();

        if (userId.isEmpty() || date.isEmpty()) {
            context.getCounter("DATA_QUALITY", "MISSING_DATA").increment(1);
            return;
        }

        context.write(new Text(date), new Text(userId));
        context.getCounter("DATA_QUALITY", "VALID_RECORDS").increment(1);
    }
}
