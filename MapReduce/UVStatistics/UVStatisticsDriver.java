package top.statistics;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class UVStatisticsDriver {

    public static void main(String[] args) throws Exception {
        System.out.println("=== UV Statistics Job ===");
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
        Job job = Job.getInstance(conf, "Daily UV Statistics");
        job.setJarByClass(UVStatisticsDriver.class);

        job.setMapperClass(UVStatisticsMapper.class);
        job.setReducerClass(UVStatisticsReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        job.setNumReduceTasks(3);

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
