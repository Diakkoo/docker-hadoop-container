package top.cleaner;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ResourceCleanerDriver {

    public static void main(String[] args) throws Exception {
        System.out.println("=== check out ===");
        System.out.println("counts of: " + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("attributes[" + i + "]: " + args[i]);
        }

        // 检查命令行参数的数量
        String inputPath, outputPath;
        if (args.length == 3) {
            inputPath = args[1];
            outputPath = args[2];
        } else if (args.length == 2) {
            inputPath = args[0];
            outputPath = args[1];
        } else {
            System.err.println("wrong, need two attributes");
            System.exit(1);
            return;
        }

        System.out.println("input path: " + inputPath);
        System.out.println("output path: " + outputPath);
        Configuration conf = new Configuration();

        conf.set("mapreduce.input.keyvaluelinerecordreader.key.value.separator", "\t");
        conf.set("textinputformat.record.delimiter", "\n");

        // 创建一个新的MapReduce作业
        Job job = Job.getInstance(conf, "Resource Cleaner");
        job.setJarByClass(ResourceCleanerDriver.class);

        // 设置Mapper和Reducer类
        job.setMapperClass(ResourceCleanerMapper.class);
        job.setReducerClass(ResourceCleanerReducer.class);

        // 设置Mapper输出的键和值类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setNumReduceTasks(3);

        boolean success = job.waitForCompletion(true);

        // 打印作业的执行结果
        // 以及统计数据
        if (success) {
            long inputRecords = job.getCounters()
                    .findCounter("org.apache.hadoop.mapreduce.TaskCounter", "MAP_INPUT_RECORDS")
                    .getValue();

            long outputRecords = job.getCounters()
                    .findCounter("org.apache.hadoop.mapreduce.TaskCounter", "MAP_OUTPUT_RECORDS")
                    .getValue();

            long invalidRatio = job.getCounters()
                    .findCounter("DATA_QUALITY", "INVALID_RESOURCE_RATIO")
                    .getValue();

            System.out.println("\nCleaning Report:");
            System.out.println("Count of input: " + inputRecords);
            System.out.println("Count of output: " + outputRecords);
            System.out.println("Count of filtering: " + (inputRecords - outputRecords));
            System.out.println("Invalid resource ratio record: " + invalidRatio);
            System.out.println("Data retention rate: " +
                    String.format("%.2f%%", (outputRecords * 100.0) / inputRecords));
        }

        System.exit(success ? 0 : 1);
    }
}