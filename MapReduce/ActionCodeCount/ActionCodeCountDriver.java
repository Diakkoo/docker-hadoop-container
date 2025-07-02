package top.actioncode.count;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/*
 * 统计所有ActionCode的次数
 * 返回结果格式为：
 * <ActionCode> <次数>
 */

public class ActionCodeCountDriver {

    public static void main(String[] args) throws Exception {
        System.out.println("=== Action Code Count Job ===");
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
        Job job = Job.getInstance(conf, "Action Code Counter");
        job.setJarByClass(ActionCodeCountDriver.class);

        job.setMapperClass(ActionCodeCountMapper.class);
        job.setCombinerClass(ActionCodeCountReducer.class);
        job.setReducerClass(ActionCodeCountReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}