package top.analysis;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class HourlyResourceDriver {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Hourly Resource Usage Report ===");
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
            System.err.println("Error: Exactly 2 arguments required - <input> <output>");
            System.exit(1);
            return;
        }

        System.out.println("Input path: " + inputPath);
        System.out.println("Output path: " + outputPath);

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Hourly Resource Usage");
        job.setJarByClass(HourlyResourceDriver.class);

        job.setMapperClass(HourlyResourceMapper.class);
        job.setCombinerClass(HourlyResourceReducer.class);
        job.setReducerClass(HourlyResourceReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(HourlyResourceWritable.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(HourlyResourceWritable.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setNumReduceTasks(6);

        boolean success = job.waitForCompletion(true);

        if (success) {
            long inputRecords = job.getCounters()
                    .findCounter("org.apache.hadoop.mapreduce.TaskCounter", "MAP_INPUT_RECORDS")
                    .getValue();

            long validRecords = job.getCounters()
                    .findCounter("DATA_QUALITY", "VALID_RECORDS")
                    .getValue();

            System.out.println("\nProcessing Report:");
            System.out.println("Total input records: " + inputRecords);
            System.out.println("Valid records processed: " + validRecords);
            System.out.println("Invalid records skipped: " + (inputRecords - validRecords));
        }

        System.exit(success ? 0 : 1);
    }
}