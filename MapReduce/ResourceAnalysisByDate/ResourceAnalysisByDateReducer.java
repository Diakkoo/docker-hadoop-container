package top.analysis;

import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class ResourceAnalysisByDateReducer extends Reducer<Text, ResourceAnalysisByDateWritable, Text, Text> {

    private Text result = new Text();

    @Override
    protected void reduce(Text key, Iterable<ResourceAnalysisByDateWritable> values, Context context)
            throws IOException, InterruptedException {

        long totalUsage = 0;
        long totalReturn = 0;

        for (ResourceAnalysisByDateWritable val : values) {
            totalUsage += val.getTotalUsage();
            totalReturn += val.getTotalReturn();
        }

        result.set(totalUsage + "\t" + totalReturn);
        context.write(key, result);
    }
}
