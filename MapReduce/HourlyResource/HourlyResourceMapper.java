package top.analysis;

import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class HourlyResourceMapper extends Mapper<LongWritable, Text, Text, HourlyResourceWritable> {

    private static final int NUM_FIELDS = 7;
    private static final int DATE_INDEX = 1;
    private static final int TIME_INDEX = 2;
    private static final int USED_RESOURCE_INDEX = 4;
    private static final int RETURNED_RESOURCE_INDEX = 6;

    private Text outputKey = new Text();
    private HourlyResourceWritable outputValue = new HourlyResourceWritable();

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

        String date = fields[DATE_INDEX].trim();
        String time = fields[TIME_INDEX].trim();

        if (date.isEmpty() || time.isEmpty()) {
            context.getCounter("DATA_QUALITY", "MISSING_DATE_TIME").increment(1);
            return;
        }

        String hour;
        try {
            hour = time.split(":")[0];
            if (hour.length() != 2 || Integer.parseInt(hour) < 0 || Integer.parseInt(hour) > 23) {
                context.getCounter("DATA_QUALITY", "INVALID_TIME_FORMAT").increment(1);
                return;
            }
        } catch (Exception e) {
            context.getCounter("DATA_QUALITY", "INVALID_TIME_FORMAT").increment(1);
            return;
        }

        long usedBytes, returnedBytes;
        try {
            usedBytes = Long.parseLong(fields[USED_RESOURCE_INDEX].trim());
            returnedBytes = Long.parseLong(fields[RETURNED_RESOURCE_INDEX].trim());

            if (usedBytes < 0 || returnedBytes < 0) {
                context.getCounter("DATA_QUALITY", "INVALID_RESOURCE_VALUE").increment(1);
                return;
            }
        } catch (NumberFormatException e) {
            context.getCounter("DATA_QUALITY", "NUMBER_FORMAT_ERROR").increment(1);
            return;
        }

        String dateHour = date + "\t" + hour;
        outputKey.set(dateHour);

        outputValue.set(usedBytes, returnedBytes);
        context.write(outputKey, outputValue);
        context.getCounter("DATA_QUALITY", "VALID_RECORDS").increment(1);
    }
}