package top.analysis;

import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class HourlyResourceReducer extends Reducer<Text, HourlyResourceWritable, Text, HourlyResourceWritable> {

    private HourlyResourceWritable result = new HourlyResourceWritable();

    @Override
    protected void reduce(Text dateHour, Iterable<HourlyResourceWritable> values, Context context)
            throws IOException, InterruptedException {

        long totalUsed = 0;
        long totalReturned = 0;

        for (HourlyResourceWritable val : values) {
            totalUsed += val.getUsedBytes();
            totalReturned += val.getReturnedBytes();
        }

        result.set(totalUsed, totalReturned);
        context.write(dateHour, result);
    }
}