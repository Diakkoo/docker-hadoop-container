package top.actioncode.count;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;

public class ActionCodeCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    private static final int NUM_FIELDS = 7;
    private static final int ACTION_CODE_INDEX = 3;
    private static final int ACTION_CODE_LENGTH = 4;
    private static final Set<Character> VALID_CHAR_CODES = new HashSet<>(
            Arrays.asList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'l', 'm', 'n', 'o', 'p', 'q')
    );

    private final IntWritable one = new IntWritable(1);
    private Text charCode = new Text();

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {

        String line = value.toString().trim();
        if (line.isEmpty()) {
            context.getCounter("DATA_QUALITY", "EMPTY_LINES").increment(1);
            return;
        }

        String[] fields = line.split(",");
        if (fields.length != NUM_FIELDS) {
            context.getCounter("DATA_QUALITY", "INVALID_FIELD_COUNT").increment(1);
            return;
        }

        String fullCode = fields[ACTION_CODE_INDEX].trim();

        if (fullCode.length() != ACTION_CODE_LENGTH) {
            context.getCounter("DATA_QUALITY", "INVALID_CODE_LENGTH").increment(1);
            return;
        }

        boolean hasInvalidChar = false;
        for (char c : fullCode.toCharArray()) {
            if (!VALID_CHAR_CODES.contains(c)) {
                context.getCounter("DATA_QUALITY", "INVALID_CHAR_CODE").increment(1);
                hasInvalidChar = true;
                continue;
            }

            charCode.set(String.valueOf(c));
            context.write(charCode, one);
        }

        if (!hasInvalidChar) {
            context.getCounter("DATA_QUALITY", "VALID_RECORDS").increment(1);
        }
    }
}
