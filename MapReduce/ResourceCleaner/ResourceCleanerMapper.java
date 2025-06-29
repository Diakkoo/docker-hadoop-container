package top.cleaner;

import java.io.IOException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

public class ResourceCleanerMapper extends Mapper<LongWritable, Text, Text, Text> {

    private static final int NUM_FIELDS = 7;

    @Override
    protected void setup(Context context) {
    }

    @Override
    public void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        // 读取每一行数据
        String line = value.toString();

        // 对于csv文件，每行的分隔符为逗号
        String[] fields = line.split(",");

        // 检查字段数量是否正确
        if (fields.length != NUM_FIELDS) {
            context.getCounter("DATA_QUALITY", "INVALID_FIELD_COUNT").increment(1);
            return;
        }

        try {
            // 获取使用资源量
            int usedKB = Integer.parseInt(fields[4].trim());
            // 获取返还资源量
            int returnedKB = Integer.parseInt(fields[6].trim());

            if (usedKB >= returnedKB) { // 如果使用资源量大于等于返还资源量，写入context
                context.write(new Text(), value);
            } else {  // 如果使用资源量小于返还资源量，记录为无效数据
                context.getCounter("DATA_QUALITY", "INVALID_RESOURCE_RATIO").increment(1);
            }
        } catch (NumberFormatException e) {
            context.getCounter("DATA_QUALITY", "NUMBER_FORMAT_ERROR").increment(1);
        } catch (Exception e) {
            context.getCounter("DATA_QUALITY", "OTHER_ERRORS").increment(1);
        }
    }
}