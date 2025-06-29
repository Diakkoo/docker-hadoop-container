package top.analysis;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ResourceAnalysisByDateDriver {
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
        Job job = Job.getInstance(conf, "Daily Resource Analysis");

        job.setJarByClass(ResourceAnalysisByDateDriver.class);
        job.setMapperClass(ResourceAnalysisByDateMapper.class);
        job.setReducerClass(ResourceAnalysisByDateReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(ResourceAnalysisByDateWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(inputPath));
        FileOutputFormat.setOutputPath(job, new Path(outputPath));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
