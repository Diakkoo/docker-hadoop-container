package top.actioncode.filter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.Text;

/*
 * 过滤出包含特定操作代码（如“0，1”）的记录
 * 返回结果格式为*原始格式*
 */

public class ActionCodeFilterDriver {

    public static void main(String[] args) throws Exception {
        System.out.println("=== check out ===");
        System.out.println("counts of: " + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("attributes[" + i + "]: " + args[i]);
        }

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
        conf.set("mapreduce.input.keyvaluelinerecordreader.kry.value.separator", "\t");
        conf.set("textinputformat.record.delimiter", "\n");

        Job job = Job.getInstance(conf, "ActionCodeFilter");
        job.setJarByClass(ActionCodeFilterDriver.class);

        job.setMapperClass(ActionCodeFilterMapper.class);
        job.setReducerClass(ActionCodeFilterReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        boolean success = job.waitForCompletion(true);

        if (success) {
            long inputRecords = job.getCounters()
                    .findCounter("org.apache.hadoop.mapreduce.TaskCounter", "MAP_INPUT_RECORDS")
                    .getValue();

            long outputRecords = job.getCounters()
                    .findCounter("STAT", "SEQUENCE_01_FOUND") // 修正计数器组
                    .getValue();

            long validRecords = job.getCounters()
                    .findCounter("DATA_QUALITY", "VALID_RECORDS")
                    .getValue();

            System.out.println("\nFilter Report:");
            System.out.println("Total input records: " + inputRecords);
            System.out.println("Valid records processed: " + validRecords);
            System.out.println("Records with '01' sequence: " + outputRecords);
        }

        System.exit(success ? 0 : 1);
    }
}