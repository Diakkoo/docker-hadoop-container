package top.analysis;

import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class ResourceAnalysisByDateMapper extends Mapper<LongWritable, Text, Text, ResourceAnalysisByDateWritable> {

    private Text dateKey = new Text();
    private static final int NUM_FIELDS = 7;
    private ResourceAnalysisByDateWritable summary = new ResourceAnalysisByDateWritable();

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString();

        String[] fields = line.split(",");

        if (fields.length != NUM_FIELDS) {
            context.getCounter("DATA_QUALITY", "INVALID_FIELD_COUNT").increment(1);
            return;
        }

        try {
            String date = fields[1].trim();
            long usage = Long.parseLong(fields[4].trim());

            long returned = 0;
            if ("true".equalsIgnoreCase(fields[5].trim())) {
                returned = Long.parseLong(fields[6].trim());
            }

            dateKey.set(date);
            summary = new ResourceAnalysisByDateWritable(usage, returned);
            context.write(dateKey, summary);

        } catch (NumberFormatException e) {
            context.getCounter("STATS", "INVALID_NUMBER").increment(1);
        }
    }
}
