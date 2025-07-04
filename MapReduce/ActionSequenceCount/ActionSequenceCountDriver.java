package top.actioncode.count;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/*
 * 按日期统计包含指定操作代码（如“0，1”）的记录
 * 返回结果格式为：
 * <日期> <记录数>
 */

public class ActionSequenceCountDriver {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Action Sequence Count Job ===");
        System.out.println("Argument count: " + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("Argument[" + i + "]: " + args[i]);
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

        System.out.println("Input path: " + inputPath);
        System.out.println("Output path: " + outputPath);

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Action Sequence Counter");
        job.setJarByClass(ActionSequenceCountDriver.class);

        job.setMapperClass(ActionSequenceCountMapper.class);
        job.setReducerClass(ActionSequenceCountReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        boolean success = job.waitForCompletion(true);

        if (success) {
            long inputRecords = job.getCounters()
                    .findCounter("org.apache.hadoop.mapreduce.TaskCounter", "MAP_INPUT_RECORDS")
                    .getValue();

            long validRecords = job.getCounters()
                    .findCounter("DATA_QUALITY", "VALID_RECORDS")
                    .getValue();

            long sequenceFound = job.getCounters()
                    .findCounter("STAT", "SEQUENCE_01L_FOUND")
                    .getValue();

            System.out.println("\nProcessing Report:");
            System.out.println("Total input records: " + inputRecords);
            System.out.println("Valid records processed: " + validRecords);
            System.out.println("Records with '" + ActionSequenceCountMapper.getTARGET_SEQUENCE() + "' sequence: %n" + sequenceFound);
            System.out.println("Records without '" + ActionSequenceCountMapper.getTARGET_SEQUENCE() + "' sequence: " + (validRecords - sequenceFound));
        }

        System.exit(success ? 0 : 1);
    }
}