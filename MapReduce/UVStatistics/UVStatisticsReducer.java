package top.statistics;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Reducer;

public class UVStatisticsReducer extends Reducer<Text, Text, Text, IntWritable> {

    @Override
    protected void reduce(Text date, Iterable<Text> userIds, Context context)
            throws IOException, InterruptedException {

        Set<String> uniqueUsers = new HashSet<>();

        for (Text userId : userIds) {
            uniqueUsers.add(userId.toString());
        }

        context.write(date, new IntWritable(uniqueUsers.size()));
    }
}
