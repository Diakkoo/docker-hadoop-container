package top.cleaner;

import java.io.IOException;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class ResourceCleanerReducer extends Reducer<Text, Text, Text, Text> {

    @Override
    public void reduce(Text key, Iterable<Text> values, Context context)
            throws IOException, InterruptedException {

        // 输出所有的值到输出文件
        // 这里的key是null，因为我们只关心values中的内容
        for (Text value : values) {
            context.write(null, value);
        }
    }
}